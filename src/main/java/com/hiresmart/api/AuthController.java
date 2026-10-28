package com.hiresmart.api;

import com.hiresmart.dto.AuthRequest;
import com.hiresmart.dto.AuthResponse;
import com.hiresmart.dto.RegisterRequest;
import com.hiresmart.service.AuthService;
import com.hiresmart.service.AuthTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    private final AuthTokenService authTokenService;

    public AuthController(AuthService authService, AuthTokenService authTokenService) {
        this.authService = authService;
        this.authTokenService = authTokenService;
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<AuthResponse> login(@RequestBody(required = false) AuthRequest request) {
        if (request == null) {
            return ResponseEntity.ok(AuthResponse.failure("Email and password are required."));
        }
        AuthResponse res = authService.login(request);
        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/register", produces = "application/json")
    public ResponseEntity<AuthResponse> register(@RequestBody(required = false) RegisterRequest request) {
        if (request == null) {
            return ResponseEntity.ok(AuthResponse.failure("Registration data is required."));
        }
        AuthResponse res = authService.register(request);
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = {"/validate", "/me"}, produces = "application/json")
    public ResponseEntity<?> validateSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader,
            @RequestParam(value = "token", required = false) String paramToken) {

        String token = resolveToken(authHeader, tokenHeader, paramToken);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "No authentication token provided."));
        }

        Optional<AuthTokenService.AuthSession> sessionOpt = authTokenService.getSession(token);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Invalid or expired session. Please log in again."));
        }

        AuthTokenService.AuthSession session = sessionOpt.get();
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "token", token,
                "user", session.getUser(),
                "profile", session.getProfile() != null ? session.getProfile() : Map.of()
        ));
    }

    @PostMapping(value = "/logout", produces = "application/json")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader,
            @RequestParam(value = "token", required = false) String paramToken) {

        String token = resolveToken(authHeader, tokenHeader, paramToken);
        if (token != null) {
            authTokenService.invalidate(token);
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully. Session terminated."));
    }

    private String resolveToken(String authHeader, String tokenHeader, String paramToken) {
        if (authHeader != null && !authHeader.isBlank()) {
            String trimmed = authHeader.trim();
            if (trimmed.toLowerCase().startsWith("bearer ")) {
                return trimmed.substring(7).trim();
            }
            return trimmed;
        }
        if (tokenHeader != null && !tokenHeader.isBlank()) {
            return tokenHeader.trim();
        }
        if (paramToken != null && !paramToken.isBlank()) {
            return paramToken.trim();
        }
        return null;
    }
}
