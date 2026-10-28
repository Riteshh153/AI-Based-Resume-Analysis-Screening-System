package com.hiresmart.api;

import com.hiresmart.model.Job;
import com.hiresmart.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobRestController {
    private final JobService jobService;

    public JobRestController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs(@RequestParam(required = false) String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(jobService.getActiveJobs());
        }
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Job>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(jobService.searchJobs(keyword, location, status));
    }

    @PostMapping
    public ResponseEntity<Job> createJob(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestBody Job job,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        String effectiveEmail = userEmail;
        com.hiresmart.model.User authUser = (com.hiresmart.model.User) request.getAttribute("authenticatedUser");
        if (authUser != null) {
            if (effectiveEmail == null || effectiveEmail.isBlank()) {
                effectiveEmail = authUser.getEmail();
            }
            if (job.getRecruiterId() == null) {
                job.setRecruiterId(authUser.getId());
            }
        }
        Long id = jobService.createJob(job, effectiveEmail);
        job.setId(id);
        return ResponseEntity.ok(job);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateJob(@PathVariable Long id, @RequestBody Job job,
                                           @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        job.setId(id);
        boolean ok = jobService.updateJob(job, userEmail);
        if (ok) return ResponseEntity.ok("Job updated successfully.");
        return ResponseEntity.badRequest().body("Failed to update job.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> closeJob(@PathVariable Long id, @RequestHeader(value = "X-User-Email", required = false) String userEmail) {
        boolean ok = jobService.closeJob(id, userEmail);
        if (ok) return ResponseEntity.ok("Job closed successfully.");
        return ResponseEntity.badRequest().body("Failed to close job.");
    }
}
