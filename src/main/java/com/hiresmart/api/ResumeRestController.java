package com.hiresmart.api;

import com.hiresmart.model.Resume;
import com.hiresmart.service.ResumeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
public class ResumeRestController {
    private final ResumeService resumeService;

    public ResumeRestController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResume(
            @RequestParam("candidateId") Long candidateId,
            @RequestParam("file") MultipartFile multipartFile) {
        try {
            File tempFile = File.createTempFile("resume_", "_" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);
            Resume resume = resumeService.uploadAndParseResume(candidateId, tempFile, multipartFile.getOriginalFilename());
            tempFile.deleteOnExit();
            return ResponseEntity.ok(resume);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to parse resume: " + e.getMessage()));
        }
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Resume>> getResumesByCandidate(@PathVariable Long candidateId) {
        return ResponseEntity.ok(resumeService.getResumesByCandidateId(candidateId));
    }

    @GetMapping("/candidate/{candidateId}/active")
    public ResponseEntity<Resume> getActiveResume(@PathVariable Long candidateId) {
        return resumeService.getActiveResume(candidateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResumeById(@PathVariable Long id) {
        return resumeService.getResumeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
