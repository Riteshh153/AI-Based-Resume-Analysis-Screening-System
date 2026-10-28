package com.hiresmart.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Job {
    private Long id;
    private Long recruiterId;
    private String title;
    private String companyName;
    private String department;
    private String location;
    private String jobType; // Full-time, Part-time, Remote, Contract
    private int experienceRequired;
    private String qualificationRequired;
    private String requiredSkills;
    private String description;
    private LocalDate applicationDeadline;
    private String status; // ACTIVE, CLOSED, DRAFT
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional Recruiter details
    private String recruiterName;
    private String recruiterEmail;

    public Job() {
        this.status = "ACTIVE";
        this.jobType = "Full-time";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecruiterId() { return recruiterId; }
    public void setRecruiterId(Long recruiterId) { this.recruiterId = recruiterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public int getExperienceRequired() { return experienceRequired; }
    public void setExperienceRequired(int experienceRequired) { this.experienceRequired = experienceRequired; }

    public String getQualificationRequired() { return qualificationRequired; }
    public void setQualificationRequired(String qualificationRequired) { this.qualificationRequired = qualificationRequired; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRecruiterName() { return recruiterName; }
    public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }

    public String getRecruiterEmail() { return recruiterEmail; }
    public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }
}
