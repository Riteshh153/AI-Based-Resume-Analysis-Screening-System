package com.hiresmart.model;

import java.time.LocalDateTime;

public class Interview {
    private Long id;
    private Long applicationId;
    private Long candidateId;
    private Long jobId;
    private String roundType; // TECHNICAL, HR, MANAGERIAL, AI_MOCK
    private LocalDateTime scheduledTime;
    private String interviewerName;
    private String interviewerEmail;
    private String meetingLink;
    private String status; // SCHEDULED, COMPLETED, RESCHEDULED, CANCELLED
    private int technicalScore;
    private int communicationScore;
    private int problemSolvingScore;
    private String feedbackNotes;
    private String recommendation; // STRONG_HIRE, HIRE, HOLD, REJECT, PENDING
    private LocalDateTime createdAt;

    // Display fields
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private String companyName;

    public Interview() {
        this.roundType = "TECHNICAL";
        this.status = "SCHEDULED";
        this.recommendation = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getRoundType() { return roundType; }
    public void setRoundType(String roundType) { this.roundType = roundType; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public String getInterviewerEmail() { return interviewerEmail; }
    public void setInterviewerEmail(String interviewerEmail) { this.interviewerEmail = interviewerEmail; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(int technicalScore) { this.technicalScore = technicalScore; }

    public int getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(int communicationScore) { this.communicationScore = communicationScore; }

    public int getProblemSolvingScore() { return problemSolvingScore; }
    public void setProblemSolvingScore(int problemSolvingScore) { this.problemSolvingScore = problemSolvingScore; }

    public String getFeedbackNotes() { return feedbackNotes; }
    public void setFeedbackNotes(String feedbackNotes) { this.feedbackNotes = feedbackNotes; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getAverageScore() {
        if (technicalScore == 0 && communicationScore == 0 && problemSolvingScore == 0) return 0;
        return (technicalScore + communicationScore + problemSolvingScore) / 3;
    }
}
