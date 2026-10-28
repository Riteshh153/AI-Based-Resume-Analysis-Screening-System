package com.hiresmart.dao;

import com.hiresmart.model.Role;
import com.hiresmart.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setPhone(rs.getString("phone"));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) user.setCreatedAt(cat.toLocalDateTime());
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) user.setUpdatedAt(uat.toLocalDateTime());
        return user;
    };

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> list = jdbcTemplate.query(sql, userRowMapper, id);
        return list.stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        List<User> list = jdbcTemplate.query(sql, userRowMapper, email.trim());
        return list.stream().findFirst();
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public List<User> findByRole(Role role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper, role.name());
    }

    public Long save(User user) {
        String sql = "INSERT INTO users (email, password_hash, full_name, role, phone, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail().toLowerCase().trim());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getPhone());
            ps.setBoolean(6, user.isActive());
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            user.setId(generatedId);
            return user.getId();
        }
        return null;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql, user.getFullName(), user.getPhone(), user.isActive(), user.getId()) > 0;
    }

    public boolean updatePassword(Long userId, String newHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        return jdbcTemplate.update(sql, newHash, userId) > 0;
    }

    public int countByRole(Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, role.name());
        return count != null ? count : 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
