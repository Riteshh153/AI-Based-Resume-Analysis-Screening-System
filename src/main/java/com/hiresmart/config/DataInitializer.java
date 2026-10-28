package com.hiresmart.config;

import com.hiresmart.dao.*;
import com.hiresmart.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserDao userDao;
    private final CandidateProfileDao profileDao;
    private final JobDao jobDao;
    private final ResumeDao resumeDao;
    private final ScreeningResultDao screeningResultDao;
    private final ApplicationDao applicationDao;
    private final InterviewDao interviewDao;
    private final MockInterviewDao mockInterviewDao;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.admin.email:admin@hiresmart.com}")
    private String configuredAdminEmail;

    @org.springframework.beans.factory.annotation.Value("${app.admin.password:admin123}")
    private String configuredAdminPassword;

    public DataInitializer(UserDao userDao, CandidateProfileDao profileDao, JobDao jobDao,
                           ResumeDao resumeDao, ScreeningResultDao screeningResultDao,
                           ApplicationDao applicationDao, InterviewDao interviewDao,
                           MockInterviewDao mockInterviewDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.jobDao = jobDao;
        this.resumeDao = resumeDao;
        this.screeningResultDao = screeningResultDao;
        this.applicationDao = applicationDao;
        this.interviewDao = interviewDao;
        this.mockInterviewDao = mockInterviewDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("[HireSmart Seeder] Verifying demo users and database records...");

        // 1. Fixed Administrator from secure configuration / environment
        String adminMail = (configuredAdminEmail != null && !configuredAdminEmail.isBlank()) ? configuredAdminEmail.trim() : "admin@hiresmart.com";
        String adminPass = (configuredAdminPassword != null && !configuredAdminPassword.isBlank()) ? configuredAdminPassword.trim() : "admin123";
        User admin = ensureDemoUser(adminMail, adminPass, "System Administrator", Role.ADMIN, "+91 9876543210");
        Long adminId = admin.getId();

        // 2. Recruiter
        User recruiter = ensureDemoUser("recruiter@hiresmart.com", "password123", "Sarah Jenkins (Lead Recruiter)", Role.RECRUITER, "+91 9876543211");
        Long recruiterId = recruiter.getId();

        // 3. Primary Candidate
        User candidateUser = ensureDemoUser("candidate@hiresmart.com", "password123", "Aditya Verma", Role.CANDIDATE, "+91 9876543212");
        Long candidateUserId = candidateUser.getId();
        Long candProfileId = profileDao.findByUserId(candidateUserId).map(CandidateProfile::getId).orElse(null);

        if (jobDao.countAll() > 0) {
            log.info("[HireSmart Seeder] Database recruitment jobs already seeded. Demo accounts are verified and active.");
            return;
        }

        log.info("[HireSmart Seeder] Seeding jobs, candidates, and recruitment records...");

        // Extra candidates for rich recruiter tables
        createCandidateWithProfile("asha.kumar@example.com", "Asha Kumar", "Senior Backend Architect", "Delhi, India",
                "B.Tech Computer Science", "Java, Spring Boot, Kafka, MySQL, Kubernetes", 4);
        createCandidateWithProfile("rohit.singh@example.com", "Rohit Singh", "Cloud & DevOps Specialist", "Pune, India",
                "MCA Cloud Computing", "AWS, Docker, Kubernetes, CI/CD, Linux, Python", 3);
        createCandidateWithProfile("priya.sharma@example.com", "Priya Sharma", "Full Stack Software Engineer", "Hyderabad, India",
                "B.Tech Information Technology", "Java, React, Spring Boot, PostgreSQL, REST APIs", 2);

        // 4. Jobs
        Job job1 = new Job();
        job1.setRecruiterId(recruiterId);
        job1.setTitle("Senior Java Full Stack Engineer");
        job1.setCompanyName("HireSmart Cloud Systems");
        job1.setDepartment("Core Engineering");
        job1.setLocation("Bangalore / Hybrid");
        job1.setJobType("Full-time");
        job1.setExperienceRequired(3);
        job1.setQualificationRequired("B.Tech / MCA in Computer Science");
        job1.setRequiredSkills("Java, Spring Boot, MySQL, Microservices, REST APIs, Docker, Git");
        job1.setDescription("We are seeking an experienced Senior Java Developer to lead backend services, design resilient microservices, and optimize database queries for high concurrency.");
        job1.setApplicationDeadline(LocalDate.now().plusMonths(2));
        job1.setStatus("ACTIVE");
        Long job1Id = jobDao.save(job1);

        Job job2 = new Job();
        job2.setRecruiterId(recruiterId);
        job2.setTitle("Cloud & DevOps Infrastructure Engineer");
        job2.setCompanyName("Nexis Global Cloud");
        job2.setDepartment("DevOps & SRE");
        job2.setLocation("Remote");
        job2.setJobType("Full-time");
        job2.setExperienceRequired(3);
        job2.setQualificationRequired("B.Tech / MCA / B.Sc IT");
        job2.setRequiredSkills("AWS, Kubernetes, Docker, CI/CD, Linux, Python, Terraform");
        job2.setDescription("Drive automated deployments, container orchestration, zero-downtime CI/CD pipelines, and cloud security compliance across multiregion clusters.");
        job2.setApplicationDeadline(LocalDate.now().plusMonths(1));
        job2.setStatus("ACTIVE");
        Long job2Id = jobDao.save(job2);

        Job job3 = new Job();
        job3.setRecruiterId(recruiterId);
        job3.setTitle("Backend Software Engineer (FinTech)");
        job3.setCompanyName("Apex Wealth Technologies");
        job3.setDepartment("Payment Gateways");
        job3.setLocation("Mumbai / Hybrid");
        job3.setJobType("Full-time");
        job3.setExperienceRequired(2);
        job3.setQualificationRequired("MCA / B.Tech / BCA");
        job3.setRequiredSkills("Java, Spring Boot, MySQL, REST APIs, JUnit, Kafka");
        job3.setDescription("Build mission-critical payment processing engines, high-security transaction pipelines, and event-driven architectures.");
        job3.setApplicationDeadline(LocalDate.now().plusMonths(3));
        job3.setStatus("ACTIVE");
        Long job3Id = jobDao.save(job3);

        // 5. Resume for Candidate
        Resume resume = new Resume();
        resume.setCandidateId(candProfileId);
        resume.setFileName("Aditya_Verma_Java_Resume.pdf");
        resume.setFileType("PDF");
        resume.setFileSizeBytes(1024 * 240);
        resume.setStoragePath("uploads/resumes/seed_aditya_resume.pdf");
        resume.setVersionNumber(1);
        resume.setActive(true);
        resume.setParsedText("Aditya Verma — Senior Java Developer. Skills: Java, Spring Boot, MySQL, REST APIs, Microservices, Docker, Git, JUnit, JavaFX. Experience: 3 years building enterprise web services. Education: Master of Computer Applications (MCA). Projects: HireSmart ATS, Distributed Transaction Gateway.");
        resume.setExtractedSkills("Java, Spring Boot, MySQL, REST APIs, Microservices, Docker, Git, JUnit, JavaFX");
        resume.setExtractedEducation("Master of Computer Applications (MCA) — 8.8 CGPA");
        resume.setExtractedExperience("3+ years developing scalable REST APIs and microservices");
        resume.setExtractedProjects("HireSmart ATS, Enterprise Distributed Transaction Engine");
        Long resumeId = resumeDao.save(resume);

        // 6. Transparent Screening Result
        ScreeningResult sr = new ScreeningResult();
        sr.setResumeId(resumeId);
        sr.setJobId(job1Id);
        sr.setOverallMatchScore(92);
        sr.setAtsCompatibilityScore(90);
        sr.setSkillMatchScore(95);
        sr.setExperienceMatchScore(100);
        sr.setMatchedSkills("Java, Spring Boot, MySQL, Microservices, REST APIs, Docker, Git");
        sr.setMissingSkills("None");
        sr.setRecommendations("Candidate profile strongly matches all required competencies.\n• Quantify database scaling impact with throughput benchmarks.\n• Highlight architectural patterns in upcoming technical interview.");
        sr.setEngineType("BASELINE_ALGORITHMIC");
        screeningResultDao.save(sr);

        // 7. Application
        Application app = new Application();
        app.setCandidateId(candProfileId);
        app.setJobId(job1Id);
        app.setResumeId(resumeId);
        app.setStatus("INTERVIEW_SCHEDULED");
        app.setCoverLetter("I am excited to submit my candidacy for the Senior Java Full Stack Engineer role. My technical background in Java, Spring Boot, and relational databases closely aligns with your requisition.");
        app.setRecruiterNotes("Top screening match (92%). Strong technical pedigree in Spring Boot and MySQL.");
        Long appId = applicationDao.save(app);

        // 8. Interview
        Interview interview = new Interview();
        interview.setApplicationId(appId);
        interview.setCandidateId(candProfileId);
        interview.setJobId(job1Id);
        interview.setRoundType("TECHNICAL");
        interview.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(30));
        interview.setInterviewerName("Vikram Malhotra (Engineering Lead)");
        interview.setInterviewerEmail("vikram.malhotra@hiresmart.com");
        interview.setMeetingLink("https://meet.google.com/hsm-hire-ats");
        interview.setStatus("SCHEDULED");
        interview.setTechnicalScore(88);
        interview.setCommunicationScore(85);
        interview.setProblemSolvingScore(90);
        interview.setFeedbackNotes("Strong performance in core Java concurrency, Spring transaction management, and indexing strategies.");
        interview.setRecommendation("STRONG_HIRE");
        interviewDao.save(interview);

        // 9. Mock Interview Questions
        mockInterviewDao.save(new MockInterviewQA(candProfileId, job1Id,
                "Explain the internal working of Java HashMap and how hash collisions are resolved in Java 8+.", "TECHNICAL"));
        mockInterviewDao.save(new MockInterviewQA(candProfileId, job1Id,
                "How do you design a thread-safe singleton or manage database connection pooling using HikariCP in Spring Boot?", "TECHNICAL"));
        mockInterviewDao.save(new MockInterviewQA(candProfileId, job1Id,
                "Tell me about a time you optimized an SQL query in MySQL that suffered from high disk I/O and slow execution times.", "BEHAVIORAL"));

        log.info("[HireSmart Seeder] Data seeding completed successfully!");
        log.info("  Admin:     admin@hiresmart.com     / admin123");
        log.info("  Recruiter: recruiter@hiresmart.com / password123");
        log.info("  Candidate: candidate@hiresmart.com / password123");
    }

    private User ensureDemoUser(String email, String rawPass, String fullName, Role role, String phone) {
        java.util.Optional<User> existing = userDao.findByEmail(email);
        if (existing.isEmpty()) {
            User u = new User(null, email, passwordEncoder.encode(rawPass), fullName, role, phone);
            Long id = userDao.save(u);
            if (id == null) {
                userDao.findByEmail(email).ifPresent(user -> u.setId(user.getId()));
            } else {
                u.setId(id);
            }
            log.info("[HireSmart Seeder] Created verified demo user {} [{}]", email, role);
            if (role == Role.CANDIDATE && u.getId() != null) {
                ensureCandidateProfile(u.getId());
            }
            return u;
        } else {
            User u = existing.get();
            boolean changed = false;
            if (!u.isActive()) {
                u.setActive(true);
                changed = true;
            }
            if (u.getPasswordHash() == null || !passwordEncoder.matches(rawPass, u.getPasswordHash())) {
                String newHash = passwordEncoder.encode(rawPass);
                userDao.updatePassword(u.getId(), newHash);
                u.setPasswordHash(newHash);
                log.info("[HireSmart Seeder] Synced BCrypt hash for demo user {}", email);
            }
            if (changed) {
                userDao.update(u);
            }
            if (role == Role.CANDIDATE && u.getId() != null) {
                ensureCandidateProfile(u.getId());
            }
            return u;
        }
    }

    private void ensureCandidateProfile(Long userId) {
        if (profileDao.findByUserId(userId).isEmpty()) {
            CandidateProfile cp = new CandidateProfile();
            cp.setUserId(userId);
            cp.setHeadline("Full Stack Java Developer | Spring Boot & Cloud Architecture");
            cp.setLocation("Bangalore, India");
            cp.setQualification("Master of Computer Applications (MCA) — 8.8 CGPA");
            cp.setSkills("Java, Spring Boot, MySQL, REST APIs, Microservices, Docker, Git, JUnit, JavaFX");
            cp.setExperienceYears(3);
            cp.setExperienceSummary("3+ years developing scalable REST APIs, microservices, and high-performance database-driven architectures.");
            cp.setLinkedinUrl("https://linkedin.com/in/adityaverma-demo");
            cp.setGithubUrl("https://github.com/adityaverma-demo");
            cp.calculateCompletion();
            Long pid = profileDao.save(cp);
            if (pid != null) cp.setId(pid);
            log.info("[HireSmart Seeder] Initialized profile for candidate user id {}", userId);
        }
    }

    private void createCandidateWithProfile(String email, String name, String headline, String location,
                                           String qual, String skills, int exp) {
        User u = new User(null, email, passwordEncoder.encode("password123"), name, Role.CANDIDATE, "+91 9800000000");
        Long uid = userDao.save(u);
        CandidateProfile p = new CandidateProfile();
        p.setUserId(uid);
        p.setHeadline(headline);
        p.setLocation(location);
        p.setQualification(qual);
        p.setSkills(skills);
        p.setExperienceYears(exp);
        p.setExperienceSummary(exp + " years working in modern software delivery.");
        p.calculateCompletion();
        profileDao.save(p);
    }
}
