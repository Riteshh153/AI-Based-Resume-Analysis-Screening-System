package com.hiresmart.service;

import com.hiresmart.dao.ApplicationDao;
import com.hiresmart.dao.AuditLogDao;
import com.hiresmart.dao.CandidateProfileDao;
import com.hiresmart.dao.JobDao;
import com.hiresmart.dao.ResumeDao;
import com.hiresmart.dao.UserDao;
import com.hiresmart.dto.ApplicationRequest;
import com.hiresmart.model.Application;
import com.hiresmart.model.AuditLog;
import com.hiresmart.model.Job;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    private final ApplicationDao applicationDao;
    private final JobDao jobDao;
    private final EmailNotificationService emailService;
    private final AuditLogDao auditLogDao;

    private final CandidateProfileDao candidateProfileDao;
    private final ResumeDao resumeDao;
    private final UserDao userDao;

    public ApplicationService(ApplicationDao applicationDao, JobDao jobDao,
                              EmailNotificationService emailService, AuditLogDao auditLogDao,
                              CandidateProfileDao candidateProfileDao, ResumeDao resumeDao,
                              UserDao userDao) {
        this.applicationDao = applicationDao;
        this.jobDao = jobDao;
        this.emailService = emailService;
        this.auditLogDao = auditLogDao;
        this.candidateProfileDao = candidateProfileDao;
        this.resumeDao = resumeDao;
        this.userDao = userDao;
    }

    public Application apply(ApplicationRequest request, String candidateEmail, String candidateName) {
        if (request == null || request.getJobId() == null) {
            throw new IllegalArgumentException("Job selection is required to submit an application.");
        }

        // 1. Resolve Candidate Profile
        Long candidateProfileId = request.getCandidateId();
        com.hiresmart.model.CandidateProfile profile = null;

        if (candidateProfileId != null) {
            profile = candidateProfileDao.findById(candidateProfileId).orElse(null);
            if (profile == null) {
                // Check if candidateId was actually user_id
                profile = candidateProfileDao.findByUserId(candidateProfileId).orElse(null);
            }
        }

        if (profile == null && candidateEmail != null && !candidateEmail.isBlank()) {
            java.util.Optional<com.hiresmart.model.User> uOpt = userDao.findByEmail(candidateEmail);
            if (uOpt.isPresent()) {
                profile = candidateProfileDao.findByUserId(uOpt.get().getId()).orElse(null);
            }
        }

        if (profile == null) {
            throw new IllegalArgumentException("Valid candidate profile could not be found for application submission.");
        }

        candidateProfileId = profile.getId();

        // 2. Resolve Resume
        Long resumeId = request.getResumeId();
        if (resumeId == null) {
            var activeResume = resumeDao.findActiveByCandidateId(candidateProfileId);
            if (activeResume.isPresent()) {
                resumeId = activeResume.get().getId();
            } else {
                var allResumes = resumeDao.findByCandidateId(candidateProfileId);
                if (!allResumes.isEmpty()) {
                    resumeId = allResumes.get(0).getId();
                } else {
                    throw new IllegalStateException("Please upload a resume first before applying for this job.");
                }
            }
        }

        // Prevent duplicate application
        Optional<Application> existing = applicationDao.findByCandidateAndJob(candidateProfileId, request.getJobId());
        if (existing.isPresent()) {
            throw new IllegalStateException("You have already submitted an application for this position.");
        }

        Application app = new Application();
        app.setCandidateId(candidateProfileId);
        app.setJobId(request.getJobId());
        app.setResumeId(resumeId);
        app.setStatus("APPLIED");
        app.setCoverLetter(request.getCoverLetter() != null ? request.getCoverLetter() : "Application submitted via HireSmart portal.");
        app.setRecruiterNotes("New application received.");

        Long appId = applicationDao.save(app);
        app.setId(appId);

        String effEmail = (candidateEmail != null && !candidateEmail.isBlank()) ? candidateEmail : profile.getEmail();
        String effName = (candidateName != null && !candidateName.isBlank()) ? candidateName : profile.getFullName();

        // Fetch job details for email
        jobDao.findById(request.getJobId()).ifPresent(job -> {
            app.setJobTitle(job.getTitle());
            app.setCompanyName(job.getCompanyName());
            if (effEmail != null) {
                emailService.sendApplicationConfirmation(effEmail, effName, job.getTitle(), job.getCompanyName());
            }
        });

        auditLogDao.save(new AuditLog(effEmail != null ? effEmail : "candidate",
                "SUBMIT_APPLICATION", "APPLICATION", appId, "Application submitted for Job #" + request.getJobId()));

        return app;
    }

    public boolean updateStatus(Long applicationId, String newStatus, String reason, String notes, Long changedByUserId, String changerEmail) {
        Optional<Application> appOpt = applicationDao.findById(applicationId);
        if (appOpt.isEmpty()) return false;

        Application app = appOpt.get();
        String oldStatus = app.getStatus();

        boolean updated = applicationDao.updateStatus(applicationId, oldStatus, newStatus, reason, notes, changedByUserId);
        if (updated) {
            auditLogDao.save(new AuditLog(changerEmail != null ? changerEmail : "recruiter",
                    "UPDATE_STATUS", "APPLICATION", applicationId,
                    "Changed status from " + oldStatus + " to " + newStatus + ". Reason: " + reason));

            // Notify candidate
            if (app.getCandidateEmail() != null) {
                emailService.sendStatusUpdateNotification(
                        app.getCandidateEmail(),
                        app.getCandidateName(),
                        app.getJobTitle(),
                        newStatus,
                        notes
                );
            }
        }
        return updated;
    }

    public List<Application> getApplicationsByCandidate(Long candidateId) {
        return applicationDao.findByCandidateId(candidateId);
    }

    public List<Application> getApplicationsByJob(Long jobId) {
        return applicationDao.findByJobId(jobId);
    }

    public List<Application> getAllApplications() {
        return applicationDao.findAll();
    }

    public Optional<Application> getApplicationById(Long id) {
        return applicationDao.findById(id);
    }
}
