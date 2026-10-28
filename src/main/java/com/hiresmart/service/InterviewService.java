package com.hiresmart.service;

import com.hiresmart.dao.ApplicationDao;
import com.hiresmart.dao.AuditLogDao;
import com.hiresmart.dao.InterviewDao;
import com.hiresmart.dto.InterviewFeedbackRequest;
import com.hiresmart.dto.InterviewScheduleRequest;
import com.hiresmart.model.Application;
import com.hiresmart.model.AuditLog;
import com.hiresmart.model.Interview;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {
    private final InterviewDao interviewDao;
    private final ApplicationDao applicationDao;
    private final EmailNotificationService emailService;
    private final AuditLogDao auditLogDao;

    public InterviewService(InterviewDao interviewDao, ApplicationDao applicationDao,
                            EmailNotificationService emailService, AuditLogDao auditLogDao) {
        this.interviewDao = interviewDao;
        this.applicationDao = applicationDao;
        this.emailService = emailService;
        this.auditLogDao = auditLogDao;
    }

    public Interview scheduleInterview(InterviewScheduleRequest request, String schedulerEmail) {
        if (request == null || request.getApplicationId() == null) {
            throw new IllegalArgumentException("Application ID is required to schedule an interview.");
        }

        Application app = applicationDao.findById(request.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + request.getApplicationId()));

        Long candId = request.getCandidateId() != null ? request.getCandidateId() : app.getCandidateId();
        Long jId = request.getJobId() != null ? request.getJobId() : app.getJobId();

        Interview interview = new Interview();
        interview.setApplicationId(request.getApplicationId());
        interview.setCandidateId(candId);
        interview.setJobId(jId);
        interview.setRoundType(request.getRoundType() != null ? request.getRoundType() : "TECHNICAL");
        interview.setScheduledTime(request.getScheduledTime() != null ? request.getScheduledTime() : LocalDateTime.now().plusDays(2));
        interview.setInterviewerName(request.getInterviewerName() != null ? request.getInterviewerName() : "Hiring Manager");
        interview.setInterviewerEmail(request.getInterviewerEmail() != null ? request.getInterviewerEmail() : schedulerEmail);
        interview.setMeetingLink(request.getMeetingLink() != null ? request.getMeetingLink() : "https://meet.google.com/hsm-hire-ats");
        interview.setStatus("SCHEDULED");
        interview.setRecommendation("PENDING");

        Long id = interviewDao.save(interview);
        interview.setId(id);

        // Update application status to INTERVIEW_SCHEDULED
        applicationDao.updateStatus(app.getId(), app.getStatus(), "INTERVIEW_SCHEDULED",
                "Interview scheduled for " + interview.getRoundType() + " round",
                "Interview scheduled with " + interview.getInterviewerName(), null);

        // Send Email Invitation
        if (app.getCandidateEmail() != null) {
            emailService.sendInterviewInvitation(
                    app.getCandidateEmail(),
                    app.getCandidateName(),
                    app.getJobTitle(),
                    interview.getRoundType(),
                    interview.getScheduledTime().toString(),
                    interview.getInterviewerName(),
                    interview.getMeetingLink()
            );
        }

        auditLogDao.save(new AuditLog(schedulerEmail != null ? schedulerEmail : "recruiter",
                "SCHEDULE_INTERVIEW", "INTERVIEW", id, "Scheduled " + interview.getRoundType() + " interview"));

        return interview;
    }

    public boolean submitFeedback(Long interviewId, InterviewFeedbackRequest request, String reviewerEmail) {
        Optional<Interview> intOpt = interviewDao.findById(interviewId);
        if (intOpt.isEmpty()) return false;

        String status = request.getStatus() != null ? request.getStatus() : "COMPLETED";
        boolean ok = interviewDao.updateFeedback(
                interviewId,
                request.getTechnicalScore(),
                request.getCommunicationScore(),
                request.getProblemSolvingScore(),
                request.getFeedbackNotes(),
                request.getRecommendation(),
                status
        );

        if (ok) {
            Interview interview = intOpt.get();
            auditLogDao.save(new AuditLog(reviewerEmail != null ? reviewerEmail : "interviewer",
                    "EVALUATE_INTERVIEW", "INTERVIEW", interviewId,
                    "Evaluated interview. Recommendation: " + request.getRecommendation()));

            // If Strong Hire or Hire, recruiter can decide on final selection
            if ("STRONG_HIRE".equalsIgnoreCase(request.getRecommendation()) || "HIRE".equalsIgnoreCase(request.getRecommendation())) {
                applicationDao.updateStatus(interview.getApplicationId(), "INTERVIEW_SCHEDULED", "SELECTED",
                        "Recommended for hiring based on interview evaluation",
                        request.getFeedbackNotes(), null);
            }
        }
        return ok;
    }

    public boolean reschedule(Long interviewId, LocalDateTime newTime, String reason) {
        return interviewDao.reschedule(interviewId, Timestamp.valueOf(newTime), reason);
    }

    public boolean cancel(Long interviewId, String reason) {
        return interviewDao.cancel(interviewId, reason);
    }

    public List<Interview> getAllInterviews() {
        return interviewDao.findAll();
    }

    public List<Interview> getInterviewsByCandidate(Long candidateId) {
        return interviewDao.findByCandidateId(candidateId);
    }

    public Optional<Interview> getInterviewById(Long id) {
        return interviewDao.findById(id);
    }
}
