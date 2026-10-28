package com.hiresmart.dao;

import com.hiresmart.model.CandidateProfile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class CandidateProfileDao {
    private final JdbcTemplate jdbcTemplate;

    public CandidateProfileDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<CandidateProfile> profileRowMapper = (rs, rowNum) -> {
        CandidateProfile p = new CandidateProfile();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setHeadline(rs.getString("headline"));
        p.setLocation(rs.getString("location"));
        p.setQualification(rs.getString("qualification"));
        p.setSkills(rs.getString("skills"));
        p.setExperienceYears(rs.getInt("experience_years"));
        p.setExperienceSummary(rs.getString("experience_summary"));
        p.setLinkedinUrl(rs.getString("linkedin_url"));
        p.setGithubUrl(rs.getString("github_url"));
        p.setProfileCompletion(rs.getInt("profile_completion"));
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) p.setUpdatedAt(uat.toLocalDateTime());

        // Joined user fields if present
        try {
            p.setFullName(rs.getString("full_name"));
            p.setEmail(rs.getString("email"));
            p.setPhone(rs.getString("phone"));
        } catch (SQLException ignored) {}

        return p;
    };

    public Optional<CandidateProfile> findById(Long id) {
        String sql = "SELECT cp.*, u.full_name, u.email, u.phone FROM candidate_profiles cp " +
                     "JOIN users u ON cp.user_id = u.id WHERE cp.id = ?";
        List<CandidateProfile> list = jdbcTemplate.query(sql, profileRowMapper, id);
        return list.stream().findFirst();
    }

    public Optional<CandidateProfile> findByUserId(Long userId) {
        String sql = "SELECT cp.*, u.full_name, u.email, u.phone FROM candidate_profiles cp " +
                     "JOIN users u ON cp.user_id = u.id WHERE cp.user_id = ?";
        List<CandidateProfile> list = jdbcTemplate.query(sql, profileRowMapper, userId);
        return list.stream().findFirst();
    }

    public List<CandidateProfile> findAll() {
        String sql = "SELECT cp.*, u.full_name, u.email, u.phone FROM candidate_profiles cp " +
                     "JOIN users u ON cp.user_id = u.id ORDER BY cp.id DESC";
        return jdbcTemplate.query(sql, profileRowMapper);
    }

    public Long save(CandidateProfile profile) {
        profile.calculateCompletion();
        String sql = "INSERT INTO candidate_profiles (user_id, headline, location, qualification, skills, " +
                     "experience_years, experience_summary, linkedin_url, github_url, profile_completion, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, profile.getUserId());
            ps.setString(2, profile.getHeadline());
            ps.setString(3, profile.getLocation());
            ps.setString(4, profile.getQualification());
            ps.setString(5, profile.getSkills());
            ps.setInt(6, profile.getExperienceYears());
            ps.setString(7, profile.getExperienceSummary());
            ps.setString(8, profile.getLinkedinUrl());
            ps.setString(9, profile.getGithubUrl());
            ps.setInt(10, profile.getProfileCompletion());
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            profile.setId(generatedId);
            return profile.getId();
        }
        return null;
    }

    public boolean update(CandidateProfile profile) {
        profile.calculateCompletion();
        String sql = "UPDATE candidate_profiles SET headline = ?, location = ?, qualification = ?, skills = ?, " +
                     "experience_years = ?, experience_summary = ?, linkedin_url = ?, github_url = ?, " +
                     "profile_completion = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql,
                profile.getHeadline(),
                profile.getLocation(),
                profile.getQualification(),
                profile.getSkills(),
                profile.getExperienceYears(),
                profile.getExperienceSummary(),
                profile.getLinkedinUrl(),
                profile.getGithubUrl(),
                profile.getProfileCompletion(),
                profile.getId()) > 0;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM candidate_profiles";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
