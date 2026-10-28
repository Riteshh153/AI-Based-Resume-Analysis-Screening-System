package com.hiresmart.model;

import java.time.LocalDateTime;

public class ApplicationHistory {
    private Long id;
    private Long applicationId;
    private String previousStatus;
    private String newStatus;
    private String changeReason;
    private Long changedByUserId;
    private LocalDateTime changedAt;

    public ApplicationHistory() {
        this.changedAt = LocalDateTime.now();
    }

    public ApplicationHistory(Long applicationId, String previousStatus, String newStatus, String changeReason, Long changedByUserId) {
        this.applicationId = applicationId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changeReason = changeReason;
        this.changedByUserId = changedByUserId;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public Long getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(Long changedByUserId) { this.changedByUserId = changedByUserId; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
