package com.hiresmart.client;

import com.hiresmart.model.CandidateProfile;
import com.hiresmart.model.Role;
import com.hiresmart.model.User;

public class UserSession {
    private static UserSession instance;

    private User currentUser;
    private CandidateProfile currentProfile;
    private String token;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setSession(User user, CandidateProfile profile, String token) {
        this.currentUser = user;
        this.currentProfile = profile;
        this.token = token;
    }

    public void clear() {
        this.currentUser = null;
        this.currentProfile = null;
        this.token = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getUser() {
        return currentUser;
    }

    public CandidateProfile getProfile() {
        return currentProfile;
    }

    public void setProfile(CandidateProfile profile) {
        this.currentProfile = profile;
    }

    public String getToken() {
        return token;
    }

    public Role getRole() {
        return currentUser != null ? currentUser.getRole() : Role.CANDIDATE;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public boolean isRecruiter() {
        return currentUser != null && currentUser.getRole() == Role.RECRUITER;
    }

    public boolean isCandidate() {
        return currentUser != null && currentUser.getRole() == Role.CANDIDATE;
    }

    public String getDisplayName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }

    public String getEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }
}
