package com.hiresmart.dao;

import com.hiresmart.model.ScreeningResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class ScreeningResultDao {
    private final JdbcTemplate jdbcTemplate;

    public ScreeningResultDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ScreeningResult> resultRowMapper = (rs, rowNum) -> {
        ScreeningResult sr = new ScreeningResult();
        sr.setId(rs.getLong("id"));
        sr.setResumeId(rs.getLong("resume_id"));
        sr.setJobId(rs.getLong("job_id"));
        sr.setOverallMatchScore(rs.getInt("overall_match_score"));
        sr.setAtsCompatibilityScore(rs.getInt("ats_compatibility_score"));
        sr.setSkillMatchScore(rs.getInt("skill_match_score"));
        sr.setExperienceMatchScore(rs.getInt("experience_match_score"));
        sr.setMatchedSkills(rs.getString("matched_skills"));
        sr.setMissingSkills(rs.getString("missing_skills"));
        sr.setRecommendations(rs.getString("recommendations"));
        sr.setEngineType(rs.getString("engine_type"));
        Timestamp sat = rs.getTimestamp("screened_at");
        if (sat != null) sr.setScreenedAt(sat.toLocalDateTime());

        try {
            sr.setCandidateName(rs.getString("candidate_name"));
            sr.setCandidateEmail(rs.getString("candidate_email"));
            sr.setJobTitle(rs.getString("job_title"));
            sr.setCompanyName(rs.getString("company_name"));
            sr.setResumeFileName(rs.getString("resume_file_name"));
        } catch (SQLException ignored) {}

        return sr;
    };

    public Optional<ScreeningResult> findById(Long id) {
        String sql = "SELECT sr.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name " +
                     "FROM screening_results sr " +
                     "JOIN resumes r ON sr.resume_id = r.id " +
                     "JOIN candidate_profiles cp ON r.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON sr.job_id = j.id WHERE sr.id = ?";
        List<ScreeningResult> list = jdbcTemplate.query(sql, resultRowMapper, id);
        return list.stream().findFirst();
    }

    public Optional<ScreeningResult> findLatestByResumeAndJob(Long resumeId, Long jobId) {
        String sql = "SELECT sr.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name " +
                     "FROM screening_results sr " +
                     "JOIN resumes r ON sr.resume_id = r.id " +
                     "JOIN candidate_profiles cp ON r.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON sr.job_id = j.id WHERE sr.resume_id = ? AND sr.job_id = ? " +
                     "ORDER BY sr.id DESC LIMIT 1";
        List<ScreeningResult> list = jdbcTemplate.query(sql, resultRowMapper, resumeId, jobId);
        return list.stream().findFirst();
    }

    public List<ScreeningResult> findByJobId(Long jobId) {
        String sql = "SELECT sr.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name " +
                     "FROM screening_results sr " +
                     "JOIN resumes r ON sr.resume_id = r.id " +
                     "JOIN candidate_profiles cp ON r.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON sr.job_id = j.id WHERE sr.job_id = ? ORDER BY sr.overall_match_score DESC";
        return jdbcTemplate.query(sql, resultRowMapper, jobId);
    }

    public List<ScreeningResult> findByCandidateId(Long candidateId) {
        String sql = "SELECT sr.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name " +
                     "FROM screening_results sr " +
                     "JOIN resumes r ON sr.resume_id = r.id " +
                     "JOIN candidate_profiles cp ON r.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON sr.job_id = j.id WHERE cp.id = ? ORDER BY sr.id DESC";
        return jdbcTemplate.query(sql, resultRowMapper, candidateId);
    }

    public List<ScreeningResult> findAll() {
        String sql = "SELECT sr.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name, r.file_name AS resume_file_name " +
                     "FROM screening_results sr " +
                     "JOIN resumes r ON sr.resume_id = r.id " +
                     "JOIN candidate_profiles cp ON r.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON sr.job_id = j.id ORDER BY sr.id DESC";
        return jdbcTemplate.query(sql, resultRowMapper);
    }

    public Long save(ScreeningResult result) {
        String sql = "INSERT INTO screening_results (resume_id, job_id, overall_match_score, ats_compatibility_score, " +
                     "skill_match_score, experience_match_score, matched_skills, missing_skills, recommendations, " +
                     "engine_type, screened_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (result.getResumeId() != null) {
                ps.setLong(1, result.getResumeId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, result.getJobId());
            ps.setInt(3, result.getOverallMatchScore());
            ps.setInt(4, result.getAtsCompatibilityScore());
            ps.setInt(5, result.getSkillMatchScore());
            ps.setInt(6, result.getExperienceMatchScore());
            ps.setString(7, result.getMatchedSkills());
            ps.setString(8, result.getMissingSkills());
            ps.setString(9, result.getRecommendations());
            ps.setString(10, result.getEngineType());
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            result.setId(generatedId);
            return result.getId();
        }
        return null;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM screening_results";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public double getAverageScore() {
        String sql = "SELECT COALESCE(AVG(overall_match_score), 0) FROM screening_results";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }
}
