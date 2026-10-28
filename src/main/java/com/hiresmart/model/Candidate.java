package com.hiresmart.model;

public class Candidate {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String appliedPosition;
    private int yearsExperience;
    private int aiScore;
    private String status;
    private String appliedDate; // ISO date or formatted date

    // Constructors, getters, setters

    public Candidate() {}

    public Candidate(Long id, String fullName, String email, String appliedPosition, int aiScore, String status, String appliedDate) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.appliedPosition = appliedPosition;
        this.aiScore = aiScore;
        this.status = status;
        this.appliedDate = appliedDate;
    }

    // getters and setters omitted for brevity

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAppliedPosition() { return appliedPosition; }
    public void setAppliedPosition(String appliedPosition) { this.appliedPosition = appliedPosition; }
    public int getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(int yearsExperience) { this.yearsExperience = yearsExperience; }
    public int getAiScore() { return aiScore; }
    public void setAiScore(int aiScore) { this.aiScore = aiScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAppliedDate() { return appliedDate; }
    public void setAppliedDate(String appliedDate) { this.appliedDate = appliedDate; }
}