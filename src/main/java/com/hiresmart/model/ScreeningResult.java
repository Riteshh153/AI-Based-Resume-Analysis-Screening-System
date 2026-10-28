package com.hiresmart.model;

import java.time.LocalDateTime;

public class ScreeningResult {
    private Long id;
    private Long resumeId;
    private Long jobId;
    private int overallMatchScore;
    private int atsCompatibilityScore;
    private int skillMatchScore;
    private int experienceMatchScore;
    private String matchedSkills;
    private String missingSkills;
    private String recommendations;
    private String engineType; // BASELINE_ALGORITHMIC, AI_ENHANCED
    private LocalDateTime screenedAt;

    // Joined fields for display
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private String companyName;
    private String resumeFileName;

    public ScreeningResult() {
        this.engineType = "BASELINE_ALGORITHMIC";
        this.screenedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public int getOverallMatchScore() { return overallMatchScore; }
    public void setOverallMatchScore(int overallMatchScore) { this.overallMatchScore = overallMatchScore; }

    public int getAtsCompatibilityScore() { return atsCompatibilityScore; }
    public void setAtsCompatibilityScore(int atsCompatibilityScore) { this.atsCompatibilityScore = atsCompatibilityScore; }

    public int getSkillMatchScore() { return skillMatchScore; }
    public void setSkillMatchScore(int skillMatchScore) { this.skillMatchScore = skillMatchScore; }

    public int getExperienceMatchScore() { return experienceMatchScore; }
    public void setExperienceMatchScore(int experienceMatchScore) { this.experienceMatchScore = experienceMatchScore; }

    public String getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(String matchedSkills) { this.matchedSkills = matchedSkills; }

    public String getMissingSkills() { return missingSkills; }
    public void setMissingSkills(String missingSkills) { this.missingSkills = missingSkills; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getEngineType() { return engineType; }
    public void setEngineType(String engineType) { this.engineType = engineType; }

    public LocalDateTime getScreenedAt() { return screenedAt; }
    public void setScreenedAt(LocalDateTime screenedAt) { this.screenedAt = screenedAt; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }
}
