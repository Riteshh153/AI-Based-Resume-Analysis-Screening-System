package com.hiresmart.model;

import java.time.LocalDateTime;

public class Resume {
    private Long id;
    private Long candidateId;
    private String fileName;
    private String fileType; // PDF, DOCX
    private long fileSizeBytes;
    private String storagePath;
    private int versionNumber;
    private boolean active;
    private String parsedText;
    private String extractedSkills;
    private String extractedEducation;
    private String extractedExperience;
    private String extractedProjects;
    private LocalDateTime uploadedAt;

    public Resume() {
        this.active = true;
        this.versionNumber = 1;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getParsedText() { return parsedText; }
    public void setParsedText(String parsedText) { this.parsedText = parsedText; }

    public String getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(String extractedSkills) { this.extractedSkills = extractedSkills; }

    public String getExtractedEducation() { return extractedEducation; }
    public void setExtractedEducation(String extractedEducation) { this.extractedEducation = extractedEducation; }

    public String getExtractedExperience() { return extractedExperience; }
    public void setExtractedExperience(String extractedExperience) { this.extractedExperience = extractedExperience; }

    public String getExtractedProjects() { return extractedProjects; }
    public void setExtractedProjects(String extractedProjects) { this.extractedProjects = extractedProjects; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
