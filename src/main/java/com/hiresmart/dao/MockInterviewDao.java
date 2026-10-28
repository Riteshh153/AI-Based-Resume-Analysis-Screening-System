package com.hiresmart.dao;

import com.hiresmart.model.MockInterviewQA;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class MockInterviewDao {
    private final JdbcTemplate jdbcTemplate;

    public MockInterviewDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MockInterviewQA> qaRowMapper = (rs, rowNum) -> {
        MockInterviewQA qa = new MockInterviewQA();
        qa.setId(rs.getLong("id"));
        qa.setCandidateId(rs.getLong("candidate_id"));
        long jid = rs.getLong("job_id");
        if (!rs.wasNull()) qa.setJobId(jid);
        qa.setQuestionText(rs.getString("question_text"));
        qa.setCategory(rs.getString("category"));
        qa.setCandidateAnswer(rs.getString("candidate_answer"));
        qa.setAiFeedback(rs.getString("ai_feedback"));
        qa.setScore(rs.getInt("score"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) qa.setCreatedAt(cat.toLocalDateTime());
        return qa;
    };

    public Optional<MockInterviewQA> findById(Long id) {
        String sql = "SELECT * FROM mock_interview_qa WHERE id = ?";
        List<MockInterviewQA> list = jdbcTemplate.query(sql, qaRowMapper, id);
        return list.stream().findFirst();
    }

    public List<MockInterviewQA> findByCandidateId(Long candidateId) {
        String sql = "SELECT * FROM mock_interview_qa WHERE candidate_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, qaRowMapper, candidateId);
    }

    public List<MockInterviewQA> findAll() {
        String sql = "SELECT * FROM mock_interview_qa ORDER BY id ASC";
        return jdbcTemplate.query(sql, qaRowMapper);
    }

    public Long save(MockInterviewQA qa) {
        String sql = "INSERT INTO mock_interview_qa (candidate_id, job_id, question_text, category, candidate_answer, ai_feedback, score, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, qa.getCandidateId());
            if (qa.getJobId() != null) ps.setLong(2, qa.getJobId());
            else ps.setNull(2, Types.BIGINT);
            ps.setString(3, qa.getQuestionText());
            ps.setString(4, qa.getCategory());
            ps.setString(5, qa.getCandidateAnswer());
            ps.setString(6, qa.getAiFeedback());
            ps.setInt(7, qa.getScore());
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            qa.setId(generatedId);
            return qa.getId();
        }
        return null;
    }

    public boolean updateAnswerAndFeedback(Long id, String answer, String feedback, int score) {
        String sql = "UPDATE mock_interview_qa SET candidate_answer = ?, ai_feedback = ?, score = ? WHERE id = ?";
        return jdbcTemplate.update(sql, answer, feedback, score, id) > 0;
    }
}
