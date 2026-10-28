package com.hiresmart.dto;

import com.hiresmart.model.CandidateProfile;
import com.hiresmart.model.User;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private User user;
    private CandidateProfile candidateProfile;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message, String token, User user, CandidateProfile candidateProfile) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
        this.candidateProfile = candidateProfile;
    }

    public static AuthResponse success(String message, String token, User user, CandidateProfile candidateProfile) {
        return new AuthResponse(true, message, token, user, candidateProfile);
    }

    public static AuthResponse failure(String message) {
        AuthResponse res = new AuthResponse();
        res.setSuccess(false);
        res.setMessage(message);
        return res;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CandidateProfile getCandidateProfile() { return candidateProfile; }
    public void setCandidateProfile(CandidateProfile candidateProfile) { this.candidateProfile = candidateProfile; }

    public CandidateProfile getProfile() { return candidateProfile; }
    public void setProfile(CandidateProfile profile) { this.candidateProfile = profile; }
}
