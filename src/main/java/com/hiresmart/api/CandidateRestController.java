package com.hiresmart.api;

import com.hiresmart.model.CandidateProfile;
import com.hiresmart.service.CandidateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "*")
public class CandidateRestController {
    private final CandidateService candidateService;

    public CandidateRestController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @GetMapping
    public ResponseEntity<List<CandidateProfile>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllProfiles());
    }

    @GetMapping("/profile/user/{userId}")
    public ResponseEntity<CandidateProfile> getProfileByUserId(@PathVariable Long userId) {
        return candidateService.getProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<CandidateProfile> getProfileById(@PathVariable Long id) {
        return candidateService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<String> updateProfile(@PathVariable Long id, @RequestBody CandidateProfile profile) {
        profile.setId(id);
        boolean ok = candidateService.updateProfile(profile, profile.getFullName(), profile.getPhone());
        if (ok) {
            return ResponseEntity.ok("Profile updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to update profile.");
        }
    }
}
