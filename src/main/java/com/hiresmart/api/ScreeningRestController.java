package com.hiresmart.api;

import com.hiresmart.dto.ScreeningRequest;
import com.hiresmart.dto.ScreeningResponse;
import com.hiresmart.model.ScreeningResult;
import com.hiresmart.service.ScreeningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screening")
@CrossOrigin(origins = "*")
public class ScreeningRestController {
    private final ScreeningService screeningService;

    public ScreeningRestController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @PostMapping({"/analyze", "/screen"})
    public ResponseEntity<ScreeningResponse> analyzeResume(@RequestBody ScreeningRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(new ScreeningResponse(false, "Screening request cannot be empty.", null));
        }

        ScreeningResponse response;
        if (request.getResumeId() != null) {
            response = screeningService.screenResumeAgainstJob(request.getResumeId(), request.getJobId());
        } else if (request.getResumeText() != null && !request.getResumeText().isBlank()) {
            response = screeningService.screenResumeTextAgainstJob(request.getJobId(), request.getResumeText(), request.getCandidateId());
        } else {
            return ResponseEntity.badRequest().body(new ScreeningResponse(false, "Resume ID or Resume Text is required.", null));
        }

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ScreeningResult>> getResultsByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(screeningService.getResultsByJob(jobId));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<ScreeningResult>> getResultsByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(screeningService.getResultsByCandidate(candidateId));
    }

    @GetMapping
    public ResponseEntity<List<ScreeningResult>> getAllResults() {
        return ResponseEntity.ok(screeningService.getAllResults());
    }
}
