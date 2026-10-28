package com.hiresmart.dao;

import com.hiresmart.model.Resume;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class ResumeDao {
    private final JdbcTemplate jdbcTemplate;

    public ResumeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Resume> resumeRowMapper = (rs, rowNum) -> {
        Resume r = new Resume();
        r.setId(rs.getLong("id"));
        r.setCandidateId(rs.getLong("candidate_id"));
        r.setFileName(rs.getString("file_name"));
        r.setFileType(rs.getString("file_type"));
        r.setFileSizeBytes(rs.getLong("file_size_bytes"));
        r.setStoragePath(rs.getString("storage_path"));
        r.setVersionNumber(rs.getInt("version_number"));
        r.setActive(rs.getBoolean("is_active"));
        r.setParsedText(rs.getString("parsed_text"));
        r.setExtractedSkills(rs.getString("extracted_skills"));
        r.setExtractedEducation(rs.getString("extracted_education"));
        r.setExtractedExperience(rs.getString("extracted_experience"));
        r.setExtractedProjects(rs.getString("extracted_projects"));
        Timestamp uat = rs.getTimestamp("uploaded_at");
        if (uat != null) r.setUploadedAt(uat.toLocalDateTime());
        return r;
    };

    public Optional<Resume> findById(Long id) {
        String sql = "SELECT * FROM resumes WHERE id = ?";
        List<Resume> list = jdbcTemplate.query(sql, resumeRowMapper, id);
        return list.stream().findFirst();
    }

    public List<Resume> findByCandidateId(Long candidateId) {
        String sql = "SELECT * FROM resumes WHERE candidate_id = ? ORDER BY version_number DESC";
        return jdbcTemplate.query(sql, resumeRowMapper, candidateId);
    }

    public Optional<Resume> findActiveByCandidateId(Long candidateId) {
        String sql = "SELECT * FROM resumes WHERE candidate_id = ? AND is_active = TRUE ORDER BY version_number DESC";
        List<Resume> list = jdbcTemplate.query(sql, resumeRowMapper, candidateId);
        return list.stream().findFirst();
    }

    public int getNextVersionNumber(Long candidateId) {
        String sql = "SELECT COALESCE(MAX(version_number), 0) + 1 FROM resumes WHERE candidate_id = ?";
        Integer nextVer = jdbcTemplate.queryForObject(sql, Integer.class, candidateId);
        return nextVer != null ? nextVer : 1;
    }

    public void deactivatePreviousResumes(Long candidateId) {
        String sql = "UPDATE resumes SET is_active = FALSE WHERE candidate_id = ?";
        jdbcTemplate.update(sql, candidateId);
    }

    public Long save(Resume resume) {
        String sql = "INSERT INTO resumes (candidate_id, file_name, file_type, file_size_bytes, storage_path, " +
                     "version_number, is_active, parsed_text, extracted_skills, extracted_education, " +
                     "extracted_experience, extracted_projects, uploaded_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, resume.getCandidateId());
            ps.setString(2, resume.getFileName());
            ps.setString(3, resume.getFileType());
            ps.setLong(4, resume.getFileSizeBytes());
            ps.setString(5, resume.getStoragePath());
            ps.setInt(6, resume.getVersionNumber());
            ps.setBoolean(7, resume.isActive());
            ps.setString(8, resume.getParsedText());
            ps.setString(9, resume.getExtractedSkills());
            ps.setString(10, resume.getExtractedEducation());
            ps.setString(11, resume.getExtractedExperience());
            ps.setString(12, resume.getExtractedProjects());
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            resume.setId(generatedId);
            return resume.getId();
        }
        return null;
    }

    public boolean updateExtractedDetails(Long id, String skills, String education, String experience, String projects) {
        String sql = "UPDATE resumes SET extracted_skills = ?, extracted_education = ?, extracted_experience = ?, extracted_projects = ? WHERE id = ?";
        return jdbcTemplate.update(sql, skills, education, experience, projects, id) > 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM resumes";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
