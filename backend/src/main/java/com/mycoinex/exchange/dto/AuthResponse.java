package com.mycoinex.exchange.dto;

public class AuthResponse {
    private final String token;
    private final long userId;
    private final String username;
    private final boolean admin;

    public AuthResponse(String token, long userId, String username, boolean admin) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.admin = admin;
    }

    public String getToken() {
        return token;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return admin;
    }
}
