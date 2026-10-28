package com.hiresmart;

import com.hiresmart.dto.*;
import com.hiresmart.model.*;
import com.hiresmart.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HireSmartApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ScreeningService screeningService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private MockInterviewService mockInterviewService;

    @Autowired
    private ExcelReportService excelReportService;

    @Autowired
    private DashboardStatsService statsService;

    @Autowired
    private ResumeService resumeService;

    @Test
    @DisplayName("Context Loads & Default Seed Users Verified")
    void contextLoads() {
        assertNotNull(authService);
        assertNotNull(jobService);
        assertNotNull(screeningService);
    }

    @Test
    @DisplayName("Module A: Verify Admin, Recruiter, and Candidate Authentication and Complete Registration Flow")
    void testAuthentication() {
        // 1. Admin login (both admin123 and demo shortcut admin)
        AuthResponse adminRes = authService.login(new AuthRequest("admin@hiresmart.com", "admin123", "ADMIN"));
        assertTrue(adminRes.isSuccess());
        assertEquals(Role.ADMIN, adminRes.getUser().getRole());

        AuthResponse adminShortcutRes = authService.login(new AuthRequest("admin@hiresmart.com", "admin", "ADMIN"));
        assertTrue(adminShortcutRes.isSuccess());
        assertEquals(Role.ADMIN, adminShortcutRes.getUser().getRole());

        // 2. Recruiter login (both password123 and demo shortcut password)
        AuthResponse recRes = authService.login(new AuthRequest("recruiter@hiresmart.com", "password123", "RECRUITER"));
        assertTrue(recRes.isSuccess());
        assertEquals(Role.RECRUITER, recRes.getUser().getRole());

        AuthResponse recShortcutRes = authService.login(new AuthRequest("recruiter@hiresmart.com", "password", "RECRUITER"));
        assertTrue(recShortcutRes.isSuccess());
        assertEquals(Role.RECRUITER, recShortcutRes.getUser().getRole());

        // 3. Candidate login (both password123 and demo shortcut password)
        AuthResponse candRes = authService.login(new AuthRequest("candidate@hiresmart.com", "password123", "CANDIDATE"));
        assertTrue(candRes.isSuccess());
        assertEquals(Role.CANDIDATE, candRes.getUser().getRole());
        assertNotNull(candRes.getCandidateProfile());

        AuthResponse candShortcutRes = authService.login(new AuthRequest("candidate@hiresmart.com", "password", "CANDIDATE"));
        assertTrue(candShortcutRes.isSuccess());
        assertEquals(Role.CANDIDATE, candShortcutRes.getUser().getRole());

        // 4. Invalid password test
        AuthResponse invalidRes = authService.login(new AuthRequest("candidate@hiresmart.com", "wrongpassword"));
        assertFalse(invalidRes.isSuccess());
        assertEquals("Invalid email or password.", invalidRes.getMessage());

        // 5. Enforce: No public admin registration
        RegisterRequest adminReg = new RegisterRequest();
        adminReg.setEmail("rogueadmin@example.com");
        adminReg.setPassword("password123");
        adminReg.setFullName("Rogue Admin");
        adminReg.setRole("ADMIN");
        AuthResponse rogueRes = authService.register(adminReg);
        assertFalse(rogueRes.isSuccess());

        // 6. Test Successful Registration for New Candidate
        RegisterRequest newCandReq = new RegisterRequest();
        newCandReq.setEmail("student.new@example.com");
        newCandReq.setPassword("securepass123");
        newCandReq.setFullName("Pooja Nair");
        newCandReq.setRole("CANDIDATE");
        newCandReq.setPhone("+91 9123456789");
        newCandReq.setHeadline("Junior Cloud Engineer");
        newCandReq.setLocation("Mumbai, India");

        AuthResponse newCandRes = authService.register(newCandReq);
        assertTrue(newCandRes.isSuccess(), "New candidate registration should succeed: " + newCandRes.getMessage());
        assertNotNull(newCandRes.getUser());
        assertEquals("Pooja Nair", newCandRes.getUser().getFullName());
        assertNotNull(newCandRes.getCandidateProfile());
        assertEquals("Junior Cloud Engineer", newCandRes.getCandidateProfile().getHeadline());

        // 7. Test Login with the newly registered Candidate
        AuthResponse newCandLoginRes = authService.login(new AuthRequest("student.new@example.com", "securepass123", "CANDIDATE"));
        assertTrue(newCandLoginRes.isSuccess(), "Newly registered candidate should log in successfully");
        assertEquals(Role.CANDIDATE, newCandLoginRes.getUser().getRole());
        assertNotNull(newCandLoginRes.getCandidateProfile());

        // 8. Test Duplicate Registration Rejection
        AuthResponse dupCandRes = authService.register(newCandReq);
        assertFalse(dupCandRes.isSuccess(), "Duplicate registration must be rejected");

        // 9. Test Successful Registration for New Recruiter
        RegisterRequest newRecReq = new RegisterRequest();
        newRecReq.setEmail("recruiter.new@techcorp.com");
        newRecReq.setPassword("recruiterpass123");
        newRecReq.setFullName("Vikram Mehta");
        newRecReq.setRole("RECRUITER");
        newRecReq.setCompanyName("TechCorp Global");
        newRecReq.setPhone("+91 9811122233");

        AuthResponse newRecRes = authService.register(newRecReq);
        assertTrue(newRecRes.isSuccess(), "New recruiter registration should succeed: " + newRecRes.getMessage());
        assertNotNull(newRecRes.getUser());
        assertEquals(Role.RECRUITER, newRecRes.getUser().getRole());

        // 10. Test Login with the newly registered Recruiter
        AuthResponse newRecLoginRes = authService.login(new AuthRequest("recruiter.new@techcorp.com", "recruiterpass123", "RECRUITER"));
        assertTrue(newRecLoginRes.isSuccess(), "Newly registered recruiter should log in successfully");
        assertEquals(Role.RECRUITER, newRecLoginRes.getUser().getRole());

        // 11. Test Backend Role Enforcement
        AuthResponse candToRecDenied = authService.login(new AuthRequest("student.new@example.com", "securepass123", "RECRUITER"));
        assertFalse(candToRecDenied.isSuccess(), "Candidate accounts must be blocked from accessing recruiter portal");
        assertTrue(candToRecDenied.getMessage().contains("Candidate accounts cannot access the Recruiter portal"));
    }

    @Test
    @DisplayName("Module C & D: Resume Skill Extraction and Transparent ATS Screening")
    void testResumeScreening() {
        String sampleResumeText = "Senior Java Engineer with 4 years experience. Skills: Java, Spring Boot, MySQL, Docker, REST APIs, Microservices, Git. Education: MCA Computer Applications.";
        String skills = resumeService.extractSkills(sampleResumeText);
        assertTrue(skills.contains("Java"));
        assertTrue(skills.contains("Spring Boot"));

        // Screen resume ID 1 against job ID 1
        ScreeningResponse response = screeningService.screenResumeAgainstJob(1L, 1L);
        assertTrue(response.isSuccess());
        assertNotNull(response.getResult());
        assertTrue(response.getResult().getOverallMatchScore() > 50);
        assertNotNull(response.getMatchedSkillsList());
        assertFalse(response.getMatchedSkillsList().isEmpty());
        assertNotNull(response.getRecommendationsList());
    }

    @Test
    @DisplayName("Module E & F: Job Creation, Application Workflow, and Duplicate Check")
    void testJobAndApplicationWorkflow() {
        List<Job> jobs = jobService.getActiveJobs();
        assertFalse(jobs.isEmpty());

        // Test duplicate application rejection
        ApplicationRequest firstApp = new ApplicationRequest(1L, 2L, 1L, "First submission for Job 2");
        applicationService.apply(firstApp, "candidate@hiresmart.com", "Aditya Verma");

        // Second application to same job must throw IllegalStateException
        ApplicationRequest duplicateReq = new ApplicationRequest(1L, 2L, 1L, "Duplicate application test");
        assertThrows(IllegalStateException.class, () -> {
            applicationService.apply(duplicateReq, "candidate@hiresmart.com", "Aditya Verma");
        });

        // Test status update lifecycle
        List<Application> apps = applicationService.getAllApplications();
        assertFalse(apps.isEmpty());
        Application app = apps.get(0);
        boolean updated = applicationService.updateStatus(app.getId(), "SHORTLISTED",
                "High ATS compatibility score", "Candidate cleared automated screening", 1L, "admin@hiresmart.com");
        assertTrue(updated);
    }

    @Test
    @DisplayName("Module G: AI Mock Interview Practice Workflow")
    void testMockInterviewWorkflow() {
        var questions = mockInterviewService.generateQuestionsForCandidate(1L, 1L);
        assertFalse(questions.isEmpty());

        var q = questions.get(0);
        String candidateAnswer = "In Spring Boot, I use HikariCP for connection pooling with max pool size configured based on core metrics. For transaction management, I use @Transactional with appropriate propagation levels and isolation to avoid dirty reads.";
        var evaluated = mockInterviewService.submitAndEvaluateAnswer(q.getId(), candidateAnswer);
        assertTrue(evaluated.getScore() >= 50);
        assertNotNull(evaluated.getAiFeedback());
    }

    @Test
    @DisplayName("Module J: Apache POI Excel Report Generation")
    void testExcelReports() throws IOException {
        File tempExcel = File.createTempFile("HireSmart_Test_Master_", ".xlsx");
        excelReportService.exportMasterRecruitmentReport(tempExcel);
        assertTrue(tempExcel.exists());
        assertTrue(tempExcel.length() > 1000);
        tempExcel.deleteOnExit();
    }

    @Autowired
    private com.hiresmart.dao.UserDao userDao;

    @Autowired
    private com.hiresmart.dao.AuditLogDao auditLogDao;

    @Test
    @DisplayName("Module I: Dashboard Aggregated Statistics")
    void testDashboardStats() {
        DashboardStats stats = statsService.getStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalCandidates() >= 1);
        assertTrue(stats.getActiveJobs() >= 1);
        assertTrue(stats.getTotalApplications() >= 1);
    }

    @Test
    @DisplayName("Module K: Admin Portal User Management & Audit Security Protection")
    void testAdminManagementAndAuditLogs() {
        // 1. Verify admin can query all users and audit logs
        List<User> users = userDao.findAll();
        assertFalse(users.isEmpty());
        assertTrue(users.stream().anyMatch(u -> u.getRole() == Role.ADMIN));

        List<AuditLog> logs = auditLogDao.findRecent(20);
        assertFalse(logs.isEmpty());

        // 2. Test user deactivation and reactivation
        User targetUser = users.stream().filter(u -> u.getRole() == Role.CANDIDATE).findFirst().orElseThrow();
        boolean originalStatus = targetUser.isActive();

        targetUser.setActive(!originalStatus);
        assertTrue(userDao.update(targetUser));

        User updatedUser = userDao.findById(targetUser.getId()).orElseThrow();
        assertEquals(!originalStatus, updatedUser.isActive());

        // 3. Verify deactivated user cannot log in
        AuthResponse blockedLogin = authService.login(new AuthRequest(targetUser.getEmail(), "password123"));
        assertFalse(blockedLogin.isSuccess());
        assertTrue(blockedLogin.getMessage().contains("deactivated"));

        // Restore
        targetUser.setActive(true);
        userDao.update(targetUser);
    }

    @Autowired
    private com.hiresmart.service.AuthTokenService authTokenService;

    @Test
    @DisplayName("Module I: Token & Session Management and Role Authorization Integrity")
    void testAuthTokenServiceAndRoleAuthorization() {
        // 1. Candidate Session
        AuthResponse candRes = authService.login(new AuthRequest("candidate@hiresmart.com", "password123", "CANDIDATE"));
        assertTrue(candRes.isSuccess());
        assertNotNull(candRes.getToken());
        assertTrue(authTokenService.isValid(candRes.getToken()));

        var candSession = authTokenService.getSession(candRes.getToken()).orElseThrow();
        assertEquals(Role.CANDIDATE, candSession.getRole());
        assertEquals("candidate@hiresmart.com", candSession.getUser().getEmail());

        // 2. Recruiter Session
        AuthResponse recRes = authService.login(new AuthRequest("recruiter@hiresmart.com", "password123", "RECRUITER"));
        assertTrue(recRes.isSuccess());
        assertNotNull(recRes.getToken());
        assertTrue(authTokenService.isValid(recRes.getToken()));

        var recSession = authTokenService.getSession(recRes.getToken()).orElseThrow();
        assertEquals(Role.RECRUITER, recSession.getRole());
        assertEquals("recruiter@hiresmart.com", recSession.getUser().getEmail());

        // 3. Admin Session
        AuthResponse adminRes = authService.login(new AuthRequest("admin@hiresmart.com", "admin123", "ADMIN"));
        assertTrue(adminRes.isSuccess());
        assertNotNull(adminRes.getToken());
        assertTrue(authTokenService.isValid(adminRes.getToken()));

        var adminSession = authTokenService.getSession(adminRes.getToken()).orElseThrow();
        assertEquals(Role.ADMIN, adminSession.getRole());
        assertEquals("admin@hiresmart.com", adminSession.getUser().getEmail());

        // 4. Invalidation / Logout
        authTokenService.invalidate(candRes.getToken());
        assertFalse(authTokenService.isValid(candRes.getToken()));
        assertTrue(authTokenService.getSession(candRes.getToken()).isEmpty());

        // 5. Invalid / Non-existent token
        assertFalse(authTokenService.isValid("invalid-token-12345"));
        assertTrue(authTokenService.getSession("invalid-token-12345").isEmpty());
    }
}
