package com.hiresmart.model;

import java.time.LocalDateTime;

public class CandidateProfile {
    private Long id;
    private Long userId;
    private String headline;
    private String location;
    private String qualification;
    private String skills; // comma separated or tags
    private int experienceYears;
    private String experienceSummary;
    private String linkedinUrl;
    private String githubUrl;
    private int profileCompletion;
    private LocalDateTime updatedAt;

    // Joined fields from User
    private String fullName;
    private String email;
    private String phone;

    public CandidateProfile() {
        this.profileCompletion = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }

    public String getExperienceSummary() { return experienceSummary; }
    public void setExperienceSummary(String experienceSummary) { this.experienceSummary = experienceSummary; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public int getProfileCompletion() { return profileCompletion; }
    public void setProfileCompletion(int profileCompletion) { this.profileCompletion = profileCompletion; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int calculateCompletion() {
        int score = 20; // Basic user account exists
        if (headline != null && !headline.isBlank()) score += 15;
        if (location != null && !location.isBlank()) score += 15;
        if (qualification != null && !qualification.isBlank()) score += 15;
        if (skills != null && !skills.isBlank()) score += 20;
        if (experienceSummary != null && !experienceSummary.isBlank()) score += 15;
        this.profileCompletion = Math.min(100, score);
        return this.profileCompletion;
    }
}
