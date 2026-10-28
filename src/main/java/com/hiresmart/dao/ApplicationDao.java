package com.hiresmart.dao;

import com.hiresmart.model.Application;
import com.hiresmart.model.ApplicationHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ApplicationDao {
    private final JdbcTemplate jdbcTemplate;

    public ApplicationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Application> applicationRowMapper = (rs, rowNum) -> {
        Application a = new Application();
        a.setId(rs.getLong("id"));
        a.setCandidateId(rs.getLong("candidate_id"));
        a.setJobId(rs.getLong("job_id"));
        a.setResumeId(rs.getLong("resume_id"));
        a.setStatus(rs.getString("status"));
        a.setCoverLetter(rs.getString("cover_letter"));
        a.setRecruiterNotes(rs.getString("recruiter_notes"));
        Timestamp aat = rs.getTimestamp("applied_at");
        if (aat != null) a.setAppliedAt(aat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) a.setUpdatedAt(uat.toLocalDateTime());

        try {
            a.setCandidateName(rs.getString("candidate_name"));
            a.setCandidateEmail(rs.getString("candidate_email"));
            a.setCandidatePhone(rs.getString("candidate_phone"));
            a.setJobTitle(rs.getString("job_title"));
            a.setCompanyName(rs.getString("company_name"));
            a.setResumeFileName(rs.getString("resume_file_name"));
            a.setMatchScore(rs.getInt("match_score"));
        } catch (SQLException ignored) {}

        return a;
    };

    private final RowMapper<ApplicationHistory> historyRowMapper = (rs, rowNum) -> {
        ApplicationHistory h = new ApplicationHistory();
        h.setId(rs.getLong("id"));
        h.setApplicationId(rs.getLong("application_id"));
        h.setPreviousStatus(rs.getString("previous_status"));
        h.setNewStatus(rs.getString("new_status"));
        h.setChangeReason(rs.getString("change_reason"));
        h.setChangedByUserId(rs.getLong("changed_by_user_id"));
        Timestamp cat = rs.getTimestamp("changed_at");
        if (cat != null) h.setChangedAt(cat.toLocalDateTime());
        return h;
    };

    public Optional<Application> findById(Long id) {
        String sql = "SELECT a.*, u.full_name AS candidate_name, u.email AS candidate_email, u.phone AS candidate_phone, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name, " +
                     "COALESCE(sr.overall_match_score, 0) AS match_score " +
                     "FROM applications a " +
                     "JOIN candidate_profiles cp ON a.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "JOIN resumes r ON a.resume_id = r.id " +
                     "LEFT JOIN screening_results sr ON (sr.resume_id = a.resume_id AND sr.job_id = a.job_id) " +
                     "WHERE a.id = ?";
        List<Application> list = jdbcTemplate.query(sql, applicationRowMapper, id);
        return list.stream().findFirst();
    }

    public Optional<Application> findByCandidateAndJob(Long candidateId, Long jobId) {
        String sql = "SELECT a.*, u.full_name AS candidate_name, u.email AS candidate_email, u.phone AS candidate_phone, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name, " +
                     "COALESCE(sr.overall_match_score, 0) AS match_score " +
                     "FROM applications a " +
                     "JOIN candidate_profiles cp ON a.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "JOIN resumes r ON a.resume_id = r.id " +
                     "LEFT JOIN screening_results sr ON (sr.resume_id = a.resume_id AND sr.job_id = a.job_id) " +
                     "WHERE a.candidate_id = ? AND a.job_id = ?";
        List<Application> list = jdbcTemplate.query(sql, applicationRowMapper, candidateId, jobId);
        return list.stream().findFirst();
    }

    public List<Application> findByCandidateId(Long candidateId) {
        String sql = "SELECT a.*, u.full_name AS candidate_name, u.email AS candidate_email, u.phone AS candidate_phone, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name, " +
                     "COALESCE(sr.overall_match_score, 0) AS match_score " +
                     "FROM applications a " +
                     "JOIN candidate_profiles cp ON a.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "JOIN resumes r ON a.resume_id = r.id " +
                     "LEFT JOIN screening_results sr ON (sr.resume_id = a.resume_id AND sr.job_id = a.job_id) " +
                     "WHERE a.candidate_id = ? ORDER BY a.id DESC";
        return jdbcTemplate.query(sql, applicationRowMapper, candidateId);
    }

    public List<Application> findByJobId(Long jobId) {
        String sql = "SELECT a.*, u.full_name AS candidate_name, u.email AS candidate_email, u.phone AS candidate_phone, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name, " +
                     "COALESCE(sr.overall_match_score, 0) AS match_score " +
                     "FROM applications a " +
                     "JOIN candidate_profiles cp ON a.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "JOIN resumes r ON a.resume_id = r.id " +
                     "LEFT JOIN screening_results sr ON (sr.resume_id = a.resume_id AND sr.job_id = a.job_id) " +
                     "WHERE a.job_id = ? ORDER BY match_score DESC, a.id DESC";
        return jdbcTemplate.query(sql, applicationRowMapper, jobId);
    }

    public List<Application> findAll() {
        String sql = "SELECT a.*, u.full_name AS candidate_name, u.email AS candidate_email, u.phone AS candidate_phone, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name, " +
                     "COALESCE(sr.overall_match_score, 0) AS match_score " +
                     "FROM applications a " +
                     "JOIN candidate_profiles cp ON a.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "JOIN resumes r ON a.resume_id = r.id " +
                     "LEFT JOIN screening_results sr ON (sr.resume_id = a.resume_id AND sr.job_id = a.job_id) " +
                     "ORDER BY a.id DESC";
        return jdbcTemplate.query(sql, applicationRowMapper);
    }

    public Long save(Application app) {
        String sql = "INSERT INTO applications (candidate_id, job_id, resume_id, status, cover_letter, recruiter_notes, applied_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, app.getCandidateId());
            ps.setLong(2, app.getJobId());
            if (app.getResumeId() != null) {
                ps.setLong(3, app.getResumeId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setString(4, app.getStatus() != null ? app.getStatus() : "APPLIED");
            ps.setString(5, app.getCoverLetter());
            ps.setString(6, app.getRecruiterNotes());
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            app.setId(generatedId);
            // Record initial history
            recordStatusHistory(app.getId(), null, app.getStatus(), "Initial application submitted", null);
            return app.getId();
        }
        return null;
    }

    public boolean updateStatus(Long id, String previousStatus, String newStatus, String reason, String notes, Long changedByUserId) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            newStatus = "UNDER_REVIEW";
        }
        String sql = "UPDATE applications SET status = ?, recruiter_notes = COALESCE(?, recruiter_notes), updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int updated = jdbcTemplate.update(sql, newStatus, notes, id);
        if (updated > 0) {
            recordStatusHistory(id, previousStatus, newStatus, reason, changedByUserId);
            return true;
        }
        return false;
    }

    public void recordStatusHistory(Long applicationId, String previousStatus, String newStatus, String reason, Long changedByUserId) {
        String sql = "INSERT INTO application_status_history (application_id, previous_status, new_status, change_reason, changed_by_user_id, changed_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql, applicationId, previousStatus, newStatus, reason, changedByUserId);
    }

    public List<ApplicationHistory> findHistoryByApplicationId(Long applicationId) {
        String sql = "SELECT * FROM application_status_history WHERE application_id = ? ORDER BY changed_at ASC";
        return jdbcTemplate.query(sql, historyRowMapper, applicationId);
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM applications";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    public Map<String, Integer> getStatusCounts() {
        String sql = "SELECT status, COUNT(*) AS cnt FROM applications GROUP BY status";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String st = rs.getString("status");
            if (st == null || st.trim().isEmpty()) {
                st = "APPLIED";
            }
            map.put(st, rs.getInt("cnt"));
        });
        return map;
    }
}
