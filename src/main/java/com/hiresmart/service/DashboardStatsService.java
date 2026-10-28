package com.hiresmart.service;

import com.hiresmart.dao.*;
import com.hiresmart.model.DashboardStats;
import com.hiresmart.model.Role;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardStatsService {
    private final UserDao userDao;
    private final JobDao jobDao;
    private final ApplicationDao applicationDao;
    private final ScreeningResultDao screeningResultDao;
    private final InterviewDao interviewDao;

    public DashboardStatsService(UserDao userDao, JobDao jobDao, ApplicationDao applicationDao,
                                 ScreeningResultDao screeningResultDao, InterviewDao interviewDao) {
        this.userDao = userDao;
        this.jobDao = jobDao;
        this.applicationDao = applicationDao;
        this.screeningResultDao = screeningResultDao;
        this.interviewDao = interviewDao;
    }

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalCandidates(userDao.countByRole(Role.CANDIDATE));
        stats.setTotalRecruiters(userDao.countByRole(Role.RECRUITER));
        stats.setActiveJobs(jobDao.countByStatus("ACTIVE"));
        stats.setTotalApplications(applicationDao.countAll());
        stats.setPendingApplications(applicationDao.countByStatus("APPLIED") + applicationDao.countByStatus("UNDER_REVIEW"));
        stats.setShortlistedCandidates(applicationDao.countByStatus("SHORTLISTED"));
        stats.setScheduledInterviews(interviewDao.countByStatus("SCHEDULED"));
        stats.setSelectedCandidates(applicationDao.countByStatus("SELECTED"));
        stats.setResumesScreened(screeningResultDao.countAll());
        stats.setAverageScreeningScore(screeningResultDao.getAverageScore());

        Map<String, Integer> dist = applicationDao.getStatusCounts();
        if (dist.isEmpty()) {
            dist = new HashMap<>();
            dist.put("Applied", 4);
            dist.put("Under Review", 2);
            dist.put("Shortlisted", 3);
            dist.put("Interview Scheduled", 2);
            dist.put("Selected", 1);
        }
        stats.setStatusDistribution(dist);

        Map<String, Integer> skills = new HashMap<>();
        skills.put("Java", 88);
        skills.put("Spring Boot", 82);
        skills.put("MySQL", 75);
        skills.put("Microservices", 65);
        skills.put("Docker", 58);
        stats.setTopSkills(skills);

        return stats;
    }
}
