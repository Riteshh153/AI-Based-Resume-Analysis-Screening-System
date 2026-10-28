package com.hiresmart.dto;

import com.hiresmart.model.ScreeningResult;
import java.util.List;

public class ScreeningRequest {
    private Long resumeId;
    private Long jobId;
    private Long candidateId;
    private String resumeText;

    public ScreeningRequest() {}
    public ScreeningRequest(Long resumeId, Long jobId) {
        this.resumeId = resumeId;
        this.jobId = jobId;
    }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }
}
