package com.hiresmart.model;

import java.time.LocalDateTime;

public class MockInterviewQA {
    private Long id;
    private Long candidateId;
    private Long jobId;
    private String questionText;
    private String category; // TECHNICAL, BEHAVIORAL, SYSTEM_DESIGN
    private String candidateAnswer;
    private String aiFeedback;
    private int score;
    private LocalDateTime createdAt;

    public MockInterviewQA() {
        this.category = "TECHNICAL";
        this.createdAt = LocalDateTime.now();
    }

    public MockInterviewQA(Long candidateId, Long jobId, String questionText, String category) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.questionText = questionText;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCandidateAnswer() { return candidateAnswer; }
    public void setCandidateAnswer(String candidateAnswer) { this.candidateAnswer = candidateAnswer; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
    public String getFeedback() { return aiFeedback; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
