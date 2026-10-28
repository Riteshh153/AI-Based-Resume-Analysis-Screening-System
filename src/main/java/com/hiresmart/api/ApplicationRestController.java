package com.hiresmart.api;

import com.hiresmart.dto.ApplicationRequest;
import com.hiresmart.dto.StatusUpdateRequest;
import com.hiresmart.model.Application;
import com.hiresmart.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationRestController {
    private final ApplicationService applicationService;

    public ApplicationRestController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<?> apply(
            jakarta.servlet.http.HttpServletRequest httpRequest,
            @RequestBody ApplicationRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String candidateEmail,
            @RequestHeader(value = "X-User-Name", required = false) String candidateName) {
        String effEmail = candidateEmail;
        String effName = candidateName;
        com.hiresmart.model.User authUser = (com.hiresmart.model.User) httpRequest.getAttribute("authenticatedUser");
        if (authUser != null) {
            if (effEmail == null || effEmail.isBlank()) effEmail = authUser.getEmail();
            if (effName == null || effName.isBlank()) effName = authUser.getFullName();
        }
        try {
            Application app = applicationService.apply(request, effEmail, effName);
            return ResponseEntity.ok(app);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Application>> getByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(applicationService.getApplicationsByCandidate(candidateId));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(jobId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getById(@PathVariable Long id) {
        return applicationService.getApplicationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String changerEmail) {
        boolean ok = applicationService.updateStatus(
                id,
                request.getNewStatus(),
                request.getChangeReason(),
                request.getRecruiterNotes(),
                request.getChangedByUserId(),
                changerEmail
        );
        if (ok) return ResponseEntity.ok("Application status updated.");
        return ResponseEntity.badRequest().body("Failed to update application status.");
    }
}
