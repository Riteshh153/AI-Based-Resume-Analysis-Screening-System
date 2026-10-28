package com.hiresmart.dao;

import com.hiresmart.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
public class AuditLogDao {
    private final JdbcTemplate jdbcTemplate;

    public AuditLogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AuditLog> logRowMapper = (rs, rowNum) -> {
        AuditLog al = new AuditLog();
        al.setId(rs.getLong("id"));
        al.setUserEmail(rs.getString("user_email"));
        al.setAction(rs.getString("action"));
        al.setEntityType(rs.getString("entity_type"));
        long eid = rs.getLong("entity_id");
        if (!rs.wasNull()) al.setEntityId(eid);
        al.setDetails(rs.getString("details"));
        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) al.setCreatedAt(cat.toLocalDateTime());
        return al;
    };

    public List<AuditLog> findRecent(int limit) {
        String sql = "SELECT * FROM audit_logs ORDER BY id DESC LIMIT ?";
        return jdbcTemplate.query(sql, logRowMapper, limit);
    }

    public Long save(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_email, action, entity_type, entity_id, details, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, log.getUserEmail());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntityType());
            if (log.getEntityId() != null) ps.setLong(4, log.getEntityId());
            else ps.setNull(4, Types.BIGINT);
            ps.setString(5, log.getDetails());
            return ps;
        }, keyHolder);

        Long generatedId = com.hiresmart.util.JdbcUtils.getGeneratedId(keyHolder);
        if (generatedId != null) {
            log.setId(generatedId);
            return log.getId();
        }
        return null;
    }
}
