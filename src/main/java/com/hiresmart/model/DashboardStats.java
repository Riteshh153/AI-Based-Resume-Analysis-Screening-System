package com.hiresmart.model;

import java.util.Map;

public class DashboardStats {
    private int totalCandidates;
    private int totalRecruiters;
    private int activeJobs;
    private int totalApplications;
    private int pendingApplications;
    private int shortlistedCandidates;
    private int scheduledInterviews;
    private int selectedCandidates;
    private int resumesScreened;
    private double averageScreeningScore;
    private Map<String, Integer> statusDistribution;
    private Map<String, Integer> topSkills;

    public DashboardStats() {}

    public int getTotalCandidates() { return totalCandidates; }
    public void setTotalCandidates(int totalCandidates) { this.totalCandidates = totalCandidates; }

    public int getTotalRecruiters() { return totalRecruiters; }
    public void setTotalRecruiters(int totalRecruiters) { this.totalRecruiters = totalRecruiters; }

    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }

    public int getTotalApplications() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

    public int getPendingApplications() { return pendingApplications; }
    public void setPendingApplications(int pendingApplications) { this.pendingApplications = pendingApplications; }

    public int getShortlistedCandidates() { return shortlistedCandidates; }
    public void setShortlistedCandidates(int shortlistedCandidates) { this.shortlistedCandidates = shortlistedCandidates; }

    public int getScheduledInterviews() { return scheduledInterviews; }
    public void setScheduledInterviews(int scheduledInterviews) { this.scheduledInterviews = scheduledInterviews; }

    public int getSelectedCandidates() { return selectedCandidates; }
    public void setSelectedCandidates(int selectedCandidates) { this.selectedCandidates = selectedCandidates; }

    public int getResumesScreened() { return resumesScreened; }
    public void setResumesScreened(int resumesScreened) { this.resumesScreened = resumesScreened; }

    public double getAverageScreeningScore() { return averageScreeningScore; }
    public void setAverageScreeningScore(double averageScreeningScore) { this.averageScreeningScore = averageScreeningScore; }

    public Map<String, Integer> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Integer> statusDistribution) { this.statusDistribution = statusDistribution; }

    public Map<String, Integer> getTopSkills() { return topSkills; }
    public void setTopSkills(Map<String, Integer> topSkills) { this.topSkills = topSkills; }
}
