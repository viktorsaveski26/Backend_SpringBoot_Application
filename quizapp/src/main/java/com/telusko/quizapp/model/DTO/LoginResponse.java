package com.telusko.quizapp.model.DTO;

public class LoginResponse {
    private String token;
    private String roles;
    private String email;

    public LoginResponse() {
    }

    public LoginResponse(String token, String roles, String email) {
        this.token = token;
        this.roles = roles;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}