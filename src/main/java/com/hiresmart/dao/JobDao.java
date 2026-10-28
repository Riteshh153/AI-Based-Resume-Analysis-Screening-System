package com.hiresmart.dao;

import com.hiresmart.model.Job;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class JobDao {
    private final JdbcTemplate jdbcTemplate;

    public JobDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Job> jobRowMapper = (rs, rowNum) -> {
        Job j = new Job();
        j.setId(rs.getLong("id"));
        j.setRecruiterId(rs.getLong("recruiter_id"));
        j.setTitle(rs.getString("title"));
        j.setCompanyName(rs.getString("company_name"));
        j.setDepartment(rs.getString("department"));
        j.setLocation(rs.getString("location"));
        j.setJobType(rs.getString("job_type"));
        j.setExperienceRequired(rs.getInt("experience_required"));
        j.setQualificationRequired(rs.getString("qualification_required"));
        j.setRequiredSkills(rs.getString("required_skills"));
        j.setDescription(rs.getString("description"));
        Date ad = rs.getDate("application_deadline");
        if (ad != null) j.setApplicationDeadline(ad.toLocalDate());
        j.setStatus(rs.getString("status"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) j.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) j.setUpdatedAt(uat.toLocalDateTime());

        try {
            j.setRecruiterName(rs.getString("recruiter_name"));
            j.setRecruiterEmail(rs.getString("recruiter_email"));
        } catch (SQLException ignored) {}

        return j;
    };

    public Optional<Job> findById(Long id) {
        String sql = "SELECT j.*, u.full_name AS recruiter_name, u.email AS recruiter_email FROM jobs j " +
                     "JOIN users u ON j.recruiter_id = u.id WHERE j.id = ?";
        List<Job> list = jdbcTemplate.query(sql, jobRowMapper, id);
        return list.stream().findFirst();
    }

    public List<Job> findAll() {
        String sql = "SELECT j.*, u.full_name AS recruiter_name, u.email AS recruiter_email FROM jobs j " +
                     "JOIN users u ON j.recruiter_id = u.id ORDER BY j.id DESC";
        return jdbcTemplate.query(sql, jobRowMapper);
    }

    public List<Job> findByStatus(String status) {
        String sql = "SELECT j.*, u.full_name AS recruiter_name, u.email AS recruiter_email FROM jobs j " +
                     "JOIN users u ON j.recruiter_id = u.id WHERE j.status = ? ORDER BY j.id DESC";
        return jdbcTemplate.query(sql, jobRowMapper, status);
    }

    public List<Job> findByRecruiterId(Long recruiterId) {
        String sql = "SELECT j.*, u.full_name AS recruiter_name, u.email AS recruiter_email FROM jobs j " +
                     "JOIN users u ON j.recruiter_id = u.id WHERE j.recruiter_id = ? ORDER BY j.id DESC";
        return jdbcTemplate.query(sql, jobRowMapper, recruiterId);
    }

    public List<Job> searchJobs(String keyword, String location, String status) {
        StringBuilder sql = new StringBuilder("SELECT j.*, u.full_name AS recruiter_name, u.email AS recruiter_email FROM jobs j JOIN users u ON j.recruiter_id = u.id WHERE 1=1 ");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (status != null && !status.isBlank()) {
            sql.append("AND j.status = ? ");
            params.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(j.title) LIKE ? OR LOWER(j.required_skills) LIKE ? OR LOWER(j.company_name) LIKE ?) ");
            String kw = "%" + keyword.toLowerCase().trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (location != null && !location.isBlank()) {
            sql.append("AND LOWER(j.location) LIKE ? ");
            params.add("%" + location.toLowerCase().trim() + "%");
        }
        sql.append("ORDER BY j.id DESC");

        return jdbcTemplate.query(sql.toString(), jobRowMapper, params.toArray());
    }

    public Long save(Job job) {
        String sql = "INSERT INTO jobs (recruiter_id, title, company_name, department, location, job_type, " +
                     "experience_required, qualification_required, required_skills, description, " +
                     "application_deadline, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, job.getRecruiterId() != null ? job.getRecruiterId() : 2L);
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getCompanyName());
            ps.setString(4, job.getDepartment());
            ps.setString(5, job.getLocation());
            ps.setString(6, job.getJobType());
            ps.setInt(7, job.getExperienceRequired());
            ps.setString(8, job.getQualificationRequired());
            ps.setString(9, job.getRequiredSkills());
            ps.setString(10, job.getDescription());
            if (job.getApplicationDeadline() != null) {
                ps.setDate(11, Date.valueOf(job.getApplicationDeadline()));
            } else {
                ps.setNull(11, Types.DATE);
            }
            ps.setString(12, job.getStatus() != null ? job.getStatus() : "ACTIVE");
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            job.setId(generatedId);
            return job.getId();
        }
        return null;
    }

    public boolean update(Job job) {
        String sql = "UPDATE jobs SET title = ?, company_name = ?, department = ?, location = ?, job_type = ?, " +
                     "experience_required = ?, qualification_required = ?, required_skills = ?, description = ?, " +
                     "application_deadline = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Date deadline = job.getApplicationDeadline() != null ? Date.valueOf(job.getApplicationDeadline()) : null;
        return jdbcTemplate.update(sql,
                job.getTitle(),
                job.getCompanyName(),
                job.getDepartment(),
                job.getLocation(),
                job.getJobType(),
                job.getExperienceRequired(),
                job.getQualificationRequired(),
                job.getRequiredSkills(),
                job.getDescription(),
                deadline,
                job.getStatus(),
                job.getId()) > 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM jobs WHERE status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM jobs";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
