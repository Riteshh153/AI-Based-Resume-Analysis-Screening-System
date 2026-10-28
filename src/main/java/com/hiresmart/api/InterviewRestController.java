package com.hiresmart.api;

import com.hiresmart.dto.InterviewFeedbackRequest;
import com.hiresmart.dto.InterviewScheduleRequest;
import com.hiresmart.model.Interview;
import com.hiresmart.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "*")
public class InterviewRestController {
    private final InterviewService interviewService;

    public InterviewRestController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<Interview> schedule(
            @RequestBody InterviewScheduleRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String schedulerEmail) {
        Interview interview = interviewService.scheduleInterview(request, schedulerEmail);
        return ResponseEntity.ok(interview);
    }

    @GetMapping
    public ResponseEntity<List<Interview>> getAllInterviews() {
        return ResponseEntity.ok(interviewService.getAllInterviews());
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Interview>> getByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(interviewService.getInterviewsByCandidate(candidateId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interview> getById(@PathVariable Long id) {
        return interviewService.getInterviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<String> submitFeedback(
            @PathVariable Long id,
            @RequestBody InterviewFeedbackRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String reviewerEmail) {
        boolean ok = interviewService.submitFeedback(id, request, reviewerEmail);
        if (ok) return ResponseEntity.ok("Interview evaluation submitted successfully.");
        return ResponseEntity.badRequest().body("Failed to submit evaluation.");
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelInterview(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Cancelled by recruiter");
        boolean ok = interviewService.cancel(id, reason);
        if (ok) return ResponseEntity.ok("Interview cancelled.");
        return ResponseEntity.badRequest().body("Failed to cancel interview.");
    }
}
