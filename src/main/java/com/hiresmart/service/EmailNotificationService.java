package com.hiresmart.service;

import com.hiresmart.dao.NotificationDao;
import com.hiresmart.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final NotificationDao notificationDao;

    @Value("${spring.mail.username:noreply@hiresmart.local}")
    private String fromEmail;

    @Value("${SMTP_PASSWORD:}")
    private String smtpPassword;

    public EmailNotificationService(JavaMailSender mailSender, NotificationDao notificationDao) {
        this.mailSender = mailSender;
        this.notificationDao = notificationDao;
    }

    public void sendEmail(String to, String recipientName, String subject, String body, String type) {
        if (to == null || to.isBlank()) return;

        Notification n = new Notification(to, recipientName, subject, body, type, "QUEUED");
        Long notifId = notificationDao.save(n);
        n.setId(notifId);

        // Check if real SMTP password is provided
        boolean hasRealCredentials = smtpPassword != null && !smtpPassword.isBlank() && !smtpPassword.equals("secret");

        if (hasRealCredentials) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(fromEmail);
                msg.setTo(to);
                msg.setSubject(subject);
                msg.setText(body);
                mailSender.send(msg);

                notificationDao.updateStatus(notifId, "SENT");
                log.info("[HireSmart Email] Dispatched live email to: {} | Subject: {}", to, subject);
            } catch (Exception ex) {
                log.error("[HireSmart Email] SMTP delivery failed for {}: {}. Recorded as FAILED.", to, ex.getMessage());
                notificationDao.updateStatus(notifId, "FAILED");
            }
        } else {
            // Documented Development Mode
            log.info("[HireSmart Email - Dev Mode] Simulated Email Notification:");
            log.info("  To: {} ({})", to, recipientName);
            log.info("  Subject: {}", subject);
            log.info("  Type: {}", type);
            log.info("  (Configure SMTP_USERNAME & SMTP_PASSWORD in .env for live dispatch)");
            notificationDao.updateStatus(notifId, "SENT (Dev Mode)");
        }
    }

    public void sendApplicationConfirmation(String to, String name, String jobTitle, String company) {
        String subject = "Application Received: " + jobTitle + " at " + company;
        String body = "Dear " + name + ",\n\n" +
                "Thank you for applying for the position of " + jobTitle + " at " + company + ".\n" +
                "Our hiring team has received your application and resume. Your profile is currently under review by our ATS system.\n\n" +
                "You can monitor your application status in real time via your HireSmart ATS Candidate Dashboard.\n\n" +
                "Best regards,\n" +
                "Recruitment Team | " + company + "\n" +
                "Powered by HireSmart ATS";
        sendEmail(to, name, subject, body, "APPLICATION_SUBMITTED");
    }

    public void sendStatusUpdateNotification(String to, String name, String jobTitle, String newStatus, String notes) {
        String subject = "Update on your application for " + jobTitle + ": " + newStatus;
        String body = "Dear " + name + ",\n\n" +
                "Your application status for the position of " + jobTitle + " has been updated to: " + newStatus + ".\n\n" +
                (notes != null && !notes.isBlank() ? "Recruiter Notes:\n" + notes + "\n\n" : "") +
                "Please log in to your HireSmart ATS dashboard for further details and next steps.\n\n" +
                "Best regards,\n" +
                "HireSmart Talent Acquisition";
        sendEmail(to, name, subject, body, "STATUS_UPDATED");
    }

    public void sendInterviewInvitation(String to, String name, String jobTitle, String round,
                                       String scheduledTime, String interviewer, String link) {
        String subject = "Interview Invitation: " + jobTitle + " (" + round + " Round)";
        String body = "Dear " + name + ",\n\n" +
                "Congratulations! You have been shortlisted for an interview for the role of " + jobTitle + ".\n\n" +
                "Interview Details:\n" +
                "• Round: " + round + "\n" +
                "• Date & Time: " + scheduledTime + "\n" +
                "• Interviewer: " + interviewer + "\n" +
                (link != null && !link.isBlank() ? "• Meeting Link: " + link + "\n\n" : "\n") +
                "Please ensure you join on time and have your environment prepared.\n\n" +
                "Best regards,\n" +
                "HireSmart ATS Recruitment";
        sendEmail(to, name, subject, body, "INTERVIEW_INVITE");
    }

    public List<Notification> getAllNotifications() {
        return notificationDao.findAll();
    }

    public List<Notification> getNotificationsForUser(String email) {
        return notificationDao.findByRecipient(email);
    }
}
