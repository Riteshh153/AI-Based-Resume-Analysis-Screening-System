package com.hiresmart.dto;

public class ApplicationRequest {
    private Long candidateId;
    private Long jobId;
    private Long resumeId;
    private String coverLetter;

    public ApplicationRequest() {}

    public ApplicationRequest(Long candidateId, Long jobId, Long resumeId, String coverLetter) {
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.resumeId = resumeId;
        this.coverLetter = coverLetter;
    }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}
