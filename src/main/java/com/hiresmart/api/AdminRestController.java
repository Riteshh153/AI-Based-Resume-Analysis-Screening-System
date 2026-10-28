package com.hiresmart.api;

import com.hiresmart.dao.AuditLogDao;
import com.hiresmart.dao.UserDao;
import com.hiresmart.model.AuditLog;
import com.hiresmart.model.Role;
import com.hiresmart.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminRestController {
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;
    private final com.hiresmart.service.AuthTokenService authTokenService;

    public AdminRestController(UserDao userDao, AuditLogDao auditLogDao, com.hiresmart.service.AuthTokenService authTokenService) {
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
        this.authTokenService = authTokenService;
    }

    private boolean isAdmin(String authHeader, String tokenHeader, String emailHeader) {
        String token = null;
        if (authHeader != null && !authHeader.isBlank()) {
            String trimmed = authHeader.trim();
            token = trimmed.toLowerCase().startsWith("bearer ") ? trimmed.substring(7).trim() : trimmed;
        } else if (tokenHeader != null && !tokenHeader.isBlank()) {
            token = tokenHeader.trim();
        }

        if (token != null) {
            var sessionOpt = authTokenService.getSession(token);
            if (sessionOpt.isPresent() && sessionOpt.get().getRole() == Role.ADMIN) {
                return true;
            }
        }

        if (emailHeader != null && !emailHeader.isBlank()) {
            Optional<User> u = userDao.findByEmail(emailHeader.trim().toLowerCase());
            return u.isPresent() && u.get().getRole() == Role.ADMIN && u.get().isActive();
        }
        return false;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        if (!isAdmin(authHeader, tokenHeader, emailHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: Administrator privileges required."));
        }
        List<User> users;
        if (role != null && !role.isBlank()) {
            users = userDao.findByRole(Role.fromString(role));
        } else {
            users = userDao.findAll();
        }
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        if (!isAdmin(authHeader, tokenHeader, emailHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: Administrator privileges required."));
        }
        Optional<User> userOpt = userDao.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        user.setActive(active);
        boolean updated = userDao.update(user);
        if (updated) {
            auditLogDao.save(new AuditLog(emailHeader != null ? emailHeader : "admin", "UPDATE_USER_STATUS", "USER", id, "User status set to " + (active ? "ACTIVE" : "INACTIVE")));
            return ResponseEntity.ok(Map.of("message", "User status updated successfully.", "active", active));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to update user status."));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader) {
        if (!isAdmin(authHeader, tokenHeader, emailHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: Administrator privileges required."));
        }
        return ResponseEntity.ok(auditLogDao.findRecent(limit));
    }
}
