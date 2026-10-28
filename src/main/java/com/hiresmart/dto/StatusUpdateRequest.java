package com.hiresmart.dto;

public class StatusUpdateRequest {
    private String newStatus;
    private String changeReason;
    private String recruiterNotes;
    private Long changedByUserId;

    public StatusUpdateRequest() {}

    public String getStatus() { return newStatus; }
    public void setStatus(String status) { this.newStatus = status; }

    public String getNotes() { return recruiterNotes; }
    public void setNotes(String notes) { this.recruiterNotes = notes; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public String getRecruiterNotes() { return recruiterNotes; }
    public void setRecruiterNotes(String recruiterNotes) { this.recruiterNotes = recruiterNotes; }

    public Long getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(Long changedByUserId) { this.changedByUserId = changedByUserId; }
}
