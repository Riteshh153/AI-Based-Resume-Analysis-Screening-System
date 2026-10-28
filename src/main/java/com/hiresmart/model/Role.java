package com.hiresmart.model;

public enum Role {
    CANDIDATE,
    RECRUITER,
    ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return CANDIDATE;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CANDIDATE;
        }
    }
}
