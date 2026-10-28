package com.hiresmart.api;

import com.hiresmart.dto.MockAnswerRequest;
import com.hiresmart.model.MockInterviewQA;
import com.hiresmart.service.MockInterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/mock-interview", "/api/mock"})
@CrossOrigin(origins = "*")
public class MockInterviewRestController {
    private final MockInterviewService mockInterviewService;

    public MockInterviewRestController(MockInterviewService mockInterviewService) {
        this.mockInterviewService = mockInterviewService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<MockInterviewQA>> generateQuestions(@RequestBody Map<String, Long> payload) {
        Long candidateId = payload.get("candidateId");
        Long jobId = payload.get("jobId");
        if (candidateId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(mockInterviewService.generateQuestionsForCandidate(candidateId, jobId));
    }

    @PostMapping("/submit")
    public ResponseEntity<MockInterviewQA> submitAnswer(@RequestBody MockAnswerRequest request) {
        MockInterviewQA evaluated = mockInterviewService.submitAndEvaluateAnswer(request.getQuestionId(), request.getAnswerText());
        return ResponseEntity.ok(evaluated);
    }

    @GetMapping({"/questions", ""})
    public ResponseEntity<List<MockInterviewQA>> getAllQuestions() {
        return ResponseEntity.ok(mockInterviewService.getAllQuestions());
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<MockInterviewQA>> getCandidateQuestions(@PathVariable Long candidateId) {
        return ResponseEntity.ok(mockInterviewService.getCandidateMockInterviews(candidateId));
    }
}
