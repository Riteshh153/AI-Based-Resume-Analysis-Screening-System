package com.hiresmart.service;

import com.hiresmart.dao.AuditLogDao;
import com.hiresmart.dao.JobDao;
import com.hiresmart.model.AuditLog;
import com.hiresmart.model.Job;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {
    private final JobDao jobDao;
    private final AuditLogDao auditLogDao;

    public JobService(JobDao jobDao, AuditLogDao auditLogDao) {
        this.jobDao = jobDao;
        this.auditLogDao = auditLogDao;
    }

    public List<Job> getAllJobs() {
        return jobDao.findAll();
    }

    public List<Job> getActiveJobs() {
        return jobDao.findByStatus("ACTIVE");
    }

    public List<Job> getJobsByRecruiter(Long recruiterId) {
        return jobDao.findByRecruiterId(recruiterId);
    }

    public Optional<Job> getJobById(Long id) {
        return jobDao.findById(id);
    }

    public List<Job> searchJobs(String keyword, String location, String status) {
        return jobDao.searchJobs(keyword, location, status);
    }

    public Long createJob(Job job, String creatorEmail) {
        Long id = jobDao.save(job);
        if (id != null) {
            auditLogDao.save(new AuditLog(creatorEmail != null ? creatorEmail : "system",
                    "CREATE_JOB", "JOB", id, "Created job: " + job.getTitle() + " at " + job.getCompanyName()));
        }
        return id;
    }

    public boolean updateJob(Job job, String updaterEmail) {
        boolean ok = jobDao.update(job);
        if (ok) {
            auditLogDao.save(new AuditLog(updaterEmail != null ? updaterEmail : "system",
                    "UPDATE_JOB", "JOB", job.getId(), "Updated job: " + job.getTitle()));
        }
        return ok;
    }

    public boolean closeJob(Long id, String updaterEmail) {
        Optional<Job> jOpt = jobDao.findById(id);
        if (jOpt.isPresent()) {
            Job j = jOpt.get();
            j.setStatus("CLOSED");
            boolean ok = jobDao.update(j);
            if (ok) {
                auditLogDao.save(new AuditLog(updaterEmail != null ? updaterEmail : "system",
                        "CLOSE_JOB", "JOB", id, "Closed job posting: " + j.getTitle()));
            }
            return ok;
        }
        return false;
    }
}
