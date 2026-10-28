package com.hiresmart.service;

import com.hiresmart.model.CandidateProfile;
import com.hiresmart.model.Role;
import com.hiresmart.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {
    private static final Logger log = LoggerFactory.getLogger(AuthTokenService.class);

    public static class AuthSession {
        private final String token;
        private final User user;
        private final CandidateProfile profile;
        private final Instant createdAt;
        private final Instant expiresAt;

        public AuthSession(String token, User user, CandidateProfile profile, Instant expiresAt) {
            this.token = token;
            this.user = user;
            this.profile = profile;
            this.createdAt = Instant.now();
            this.expiresAt = expiresAt;
        }

        public String getToken() { return token; }
        public User getUser() { return user; }
        public CandidateProfile getProfile() { return profile; }
        public Role getRole() { return user != null ? user.getRole() : Role.CANDIDATE; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getExpiresAt() { return expiresAt; }
        public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }

    private final Map<String, AuthSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Create an active server-side session for an authenticated user with a 24-hour expiration.
     */
    public String createSession(User user, CandidateProfile profile) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
        AuthSession session = new AuthSession(token, user, profile, expiresAt);
        activeSessions.put(token, session);
        log.info("Created authenticated session for user {} [{}] with token {}", user.getEmail(), user.getRole(), token);
        return token;
    }

    /**
     * Retrieve and validate an active session by token.
     */
    public Optional<AuthSession> getSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String cleanToken = token.trim();
        AuthSession session = activeSessions.get(cleanToken);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired()) {
            log.info("Session expired for user {} with token {}", session.getUser().getEmail(), cleanToken);
            activeSessions.remove(cleanToken);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    /**
     * Invalidate/revoke an active session on logout.
     */
    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            AuthSession removed = activeSessions.remove(token.trim());
            if (removed != null) {
                log.info("Invalidated session for user {} with token {}", removed.getUser().getEmail(), token.trim());
            }
        }
    }

    /**
     * Check if a token represents a currently valid session.
     */
    public boolean isValid(String token) {
        return getSession(token).isPresent();
    }

    /**
     * Get the number of currently active sessions.
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
