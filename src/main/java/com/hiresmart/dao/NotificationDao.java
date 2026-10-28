package com.hiresmart.dao;

import com.hiresmart.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
public class NotificationDao {
    private final JdbcTemplate jdbcTemplate;

    public NotificationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> notifRowMapper = (rs, rowNum) -> {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setRecipientEmail(rs.getString("recipient_email"));
        n.setRecipientName(rs.getString("recipient_name"));
        n.setSubject(rs.getString("subject"));
        n.setBody(rs.getString("body"));
        n.setType(rs.getString("type"));
        n.setStatus(rs.getString("status"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) n.setCreatedAt(cat.toLocalDateTime());
        return n;
    };

    public List<Notification> findByRecipient(String email) {
        String sql = "SELECT * FROM notifications WHERE LOWER(recipient_email) = LOWER(?) ORDER BY id DESC";
        return jdbcTemplate.query(sql, notifRowMapper, email.trim());
    }

    public List<Notification> findAll() {
        String sql = "SELECT * FROM notifications ORDER BY id DESC";
        return jdbcTemplate.query(sql, notifRowMapper);
    }

    public Long save(Notification n) {
        String sql = "INSERT INTO notifications (recipient_email, recipient_name, subject, body, type, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, n.getRecipientEmail());
            ps.setString(2, n.getRecipientName());
            ps.setString(3, n.getSubject());
            ps.setString(4, n.getBody());
            ps.setString(5, n.getType());
            ps.setString(6, n.getStatus() != null ? n.getStatus() : "SENT");
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            n.setId(generatedId);
            return n.getId();
        }
        return null;
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE notifications SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }
}
