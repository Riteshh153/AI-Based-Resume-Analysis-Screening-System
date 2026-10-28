package com.hiresmart.model;

import java.time.LocalDateTime;

public class Notification {
    private Long id;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String body;
    private String type; // APPLICATION_SUBMITTED, STATUS_UPDATED, INTERVIEW_INVITE, OFFER_LETTER, SYSTEM
    private String status; // SENT, QUEUED, FAILED
    private LocalDateTime createdAt;

    public Notification() {
        this.status = "SENT";
        this.createdAt = LocalDateTime.now();
    }

    public Notification(String recipientEmail, String recipientName, String subject, String body, String type, String status) {
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.subject = subject;
        this.body = body;
        this.type = type;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
