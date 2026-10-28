package com.hiresmart.service;

import com.hiresmart.dao.JobDao;
import com.hiresmart.dao.MockInterviewDao;
import com.hiresmart.model.Job;
import com.hiresmart.model.MockInterviewQA;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MockInterviewService {
    private final MockInterviewDao mockInterviewDao;
    private final JobDao jobDao;

    public MockInterviewService(MockInterviewDao mockInterviewDao, JobDao jobDao) {
        this.mockInterviewDao = mockInterviewDao;
        this.jobDao = jobDao;
    }

    public List<MockInterviewQA> generateQuestionsForCandidate(Long candidateId, Long jobId) {
        String jobTitle = "Software Engineer";
        String requiredSkills = "Java, Spring Boot, MySQL, REST APIs";

        if (jobId != null) {
            Optional<Job> jobOpt = jobDao.findById(jobId);
            if (jobOpt.isPresent()) {
                jobTitle = jobOpt.get().getTitle();
                requiredSkills = jobOpt.get().getRequiredSkills();
            }
        }

        List<MockInterviewQA> questions = new ArrayList<>();

        // Question 1: Core Architecture
        questions.add(new MockInterviewQA(candidateId, jobId,
                "In a high-throughput " + jobTitle + " environment, how would you design a scalable REST API using Spring Boot with connection pooling and caching?",
                "TECHNICAL"));

        // Question 2: Database & Data Integrity
        questions.add(new MockInterviewQA(candidateId, jobId,
                "Explain how you manage database transactions, race conditions, and optimistic vs pessimistic locking in relational databases like MySQL.",
                "DATABASE"));

        // Question 3: Behavioral / Problem Solving
        questions.add(new MockInterviewQA(candidateId, jobId,
                "Describe a situation where a critical production bug occurred or system latency spiked. How did you diagnose the root cause and implement a fix?",
                "BEHAVIORAL"));

        // Question 4: Skills Deep Dive
        questions.add(new MockInterviewQA(candidateId, jobId,
                "Regarding " + requiredSkills.split("[,;]")[0] + ", what are the core design patterns you utilize to ensure clean code, maintainability, and testability?",
                "SYSTEM_DESIGN"));

        List<MockInterviewQA> saved = new ArrayList<>();
        for (MockInterviewQA q : questions) {
            Long id = mockInterviewDao.save(q);
            q.setId(id);
            saved.add(q);
        }
        return saved;
    }

    public MockInterviewQA submitAndEvaluateAnswer(Long questionId, String candidateAnswer) {
        Optional<MockInterviewQA> qaOpt = mockInterviewDao.findById(questionId);
        if (qaOpt.isEmpty()) {
            throw new IllegalArgumentException("Question not found with ID: " + questionId);
        }

        MockInterviewQA qa = qaOpt.get();
        qa.setCandidateAnswer(candidateAnswer);

        // Algorithmic answer evaluation
        int score = evaluateAnswerContent(candidateAnswer, qa.getCategory());
        String feedback = generateFeedback(score, candidateAnswer, qa.getCategory());

        qa.setScore(score);
        qa.setAiFeedback(feedback);

        mockInterviewDao.updateAnswerAndFeedback(questionId, candidateAnswer, feedback, score);
        return qa;
    }

    private int evaluateAnswerContent(String answer, String category) {
        if (answer == null || answer.trim().length() < 20) {
            return 30; // Very brief response
        }

        int score = 50;
        String lower = answer.toLowerCase();

        // Technical keyword richness
        List<String> strongKeywords = List.of(
                "architecture", "transaction", "acid", "index", "latency", "cache", "redis",
                "thread", "concurrency", "test", "junit", "spring", "pool", "hikari",
                "scalability", "metric", "debug", "log", "exception", "validation", "security"
        );

        long matchCount = strongKeywords.stream().filter(lower::contains).count();
        score += (int) (matchCount * 5);

        // Length & structure bonus
        if (answer.length() > 150) score += 10;
        if (answer.length() > 300) score += 10;
        if (lower.contains("for example") || lower.contains("specifically") || lower.contains("in my experience")) {
            score += 10;
        }

        return Math.min(96, Math.max(35, score));
    }

    private String generateFeedback(int score, String answer, String category) {
        StringBuilder fb = new StringBuilder();
        if (score >= 85) {
            fb.append("Excellent response! You demonstrated strong technical articulation, structured thought process, and practical architectural depth.");
        } else if (score >= 70) {
            fb.append("Good answer. You covered the primary concepts clearly. To elevate your response, consider citing real-world production metrics or trade-offs between alternative designs.");
        } else {
            fb.append("Foundational response. We recommend elaborating on specific design patterns, edge case handling, and hands-on implementation details to show mastery.");
        }

        fb.append("\nTip: Structure your answers using the STAR method (Situation, Task, Action, Result) for maximum recruiter impact.");
        return fb.toString();
    }

    public List<MockInterviewQA> getCandidateMockInterviews(Long candidateId) {
        return mockInterviewDao.findByCandidateId(candidateId);
    }

    public List<MockInterviewQA> getAllQuestions() {
        List<MockInterviewQA> list = mockInterviewDao.findAll();
        if (list.isEmpty()) {
            return generateQuestionsForCandidate(1L, null);
        }
        return list;
    }
}
