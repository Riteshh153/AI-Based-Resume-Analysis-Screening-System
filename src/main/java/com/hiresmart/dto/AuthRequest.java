package com.hiresmart.dto;

public class AuthRequest {
    private String email;
    private String password;

    private String expectedRole;

    public AuthRequest() {}
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public AuthRequest(String email, String password, String expectedRole) {
        this.email = email;
        this.password = password;
        this.expectedRole = expectedRole;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getExpectedRole() { return expectedRole; }
    public void setExpectedRole(String expectedRole) { this.expectedRole = expectedRole; }
}
