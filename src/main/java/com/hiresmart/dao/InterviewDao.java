package com.hiresmart.dao;

import com.hiresmart.model.Interview;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class InterviewDao {
    private final JdbcTemplate jdbcTemplate;

    public InterviewDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Interview> interviewRowMapper = (rs, rowNum) -> {
        Interview i = new Interview();
        i.setId(rs.getLong("id"));
        i.setApplicationId(rs.getLong("application_id"));
        i.setCandidateId(rs.getLong("candidate_id"));
        i.setJobId(rs.getLong("job_id"));
        i.setRoundType(rs.getString("round_type"));
        Timestamp st = rs.getTimestamp("scheduled_time");
        if (st != null) i.setScheduledTime(st.toLocalDateTime());
        i.setInterviewerName(rs.getString("interviewer_name"));
        i.setInterviewerEmail(rs.getString("interviewer_email"));
        i.setMeetingLink(rs.getString("meeting_link"));
        i.setStatus(rs.getString("status"));
        i.setTechnicalScore(rs.getInt("technical_score"));
        i.setCommunicationScore(rs.getInt("communication_score"));
        i.setProblemSolvingScore(rs.getInt("problem_solving_score"));
        i.setFeedbackNotes(rs.getString("feedback_notes"));
        i.setRecommendation(rs.getString("recommendation"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) i.setCreatedAt(cat.toLocalDateTime());

        try {
            i.setCandidateName(rs.getString("candidate_name"));
            i.setCandidateEmail(rs.getString("candidate_email"));
            i.setJobTitle(rs.getString("job_title"));
            i.setCompanyName(rs.getString("company_name"));
        } catch (SQLException ignored) {}

        return i;
    };

    public Optional<Interview> findById(Long id) {
        String sql = "SELECT i.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name " +
                     "FROM interviews i " +
                     "JOIN candidate_profiles cp ON i.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON i.job_id = j.id WHERE i.id = ?";
        List<Interview> list = jdbcTemplate.query(sql, interviewRowMapper, id);
        return list.stream().findFirst();
    }

    public List<Interview> findAll() {
        String sql = "SELECT i.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name " +
                     "FROM interviews i " +
                     "JOIN candidate_profiles cp ON i.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON i.job_id = j.id ORDER BY i.scheduled_time ASC";
        return jdbcTemplate.query(sql, interviewRowMapper);
    }

    public List<Interview> findByCandidateId(Long candidateId) {
        String sql = "SELECT i.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name " +
                     "FROM interviews i " +
                     "JOIN candidate_profiles cp ON i.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON i.job_id = j.id WHERE i.candidate_id = ? ORDER BY i.scheduled_time ASC";
        return jdbcTemplate.query(sql, interviewRowMapper, candidateId);
    }

    public List<Interview> findByApplicationId(Long applicationId) {
        String sql = "SELECT i.*, u.full_name AS candidate_name, u.email AS candidate_email, " +
                     "j.title AS job_title, j.company_name " +
                     "FROM interviews i " +
                     "JOIN candidate_profiles cp ON i.candidate_id = cp.id " +
                     "JOIN users u ON cp.user_id = u.id " +
                     "JOIN jobs j ON i.job_id = j.id WHERE i.application_id = ? ORDER BY i.scheduled_time ASC";
        return jdbcTemplate.query(sql, interviewRowMapper, applicationId);
    }

    public Long save(Interview interview) {
        String sql = "INSERT INTO interviews (application_id, candidate_id, job_id, round_type, scheduled_time, " +
                     "interviewer_name, interviewer_email, meeting_link, status, technical_score, " +
                     "communication_score, problem_solving_score, feedback_notes, recommendation, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (interview.getApplicationId() != null) ps.setLong(1, interview.getApplicationId()); else ps.setNull(1, Types.BIGINT);
            if (interview.getCandidateId() != null) ps.setLong(2, interview.getCandidateId()); else ps.setNull(2, Types.BIGINT);
            if (interview.getJobId() != null) ps.setLong(3, interview.getJobId()); else ps.setNull(3, Types.BIGINT);
            ps.setString(4, interview.getRoundType());
            ps.setTimestamp(5, Timestamp.valueOf(interview.getScheduledTime()));
            ps.setString(6, interview.getInterviewerName());
            ps.setString(7, interview.getInterviewerEmail());
            ps.setString(8, interview.getMeetingLink());
            ps.setString(9, interview.getStatus() != null ? interview.getStatus() : "SCHEDULED");
            ps.setInt(10, interview.getTechnicalScore());
            ps.setInt(11, interview.getCommunicationScore());
            ps.setInt(12, interview.getProblemSolvingScore());
            ps.setString(13, interview.getFeedbackNotes());
            ps.setString(14, interview.getRecommendation() != null ? interview.getRecommendation() : "PENDING");
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            interview.setId(generatedId);
            return interview.getId();
        }
        return null;
    }

    public boolean updateFeedback(Long id, int tech, int comm, int prob, String feedback, String recommendation, String status) {
        String sql = "UPDATE interviews SET technical_score = ?, communication_score = ?, problem_solving_score = ?, " +
                     "feedback_notes = ?, recommendation = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, tech, comm, prob, feedback, recommendation, status, id) > 0;
    }

    public boolean reschedule(Long id, Timestamp newTime, String reason) {
        String sql = "UPDATE interviews SET scheduled_time = ?, status = 'RESCHEDULED', feedback_notes = " +
                     "CONCAT(COALESCE(feedback_notes, ''), '\nRescheduled: ', ?) WHERE id = ?";
        return jdbcTemplate.update(sql, newTime, reason, id) > 0;
    }

    public boolean cancel(Long id, String reason) {
        String sql = "UPDATE interviews SET status = 'CANCELLED', feedback_notes = " +
                     "CONCAT(COALESCE(feedback_notes, ''), '\nCancelled: ', ?) WHERE id = ?";
        return jdbcTemplate.update(sql, reason, id) > 0;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM interviews WHERE status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM interviews";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
