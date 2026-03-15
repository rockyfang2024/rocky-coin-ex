package com.mycoinex.exchange.model;

import java.time.Instant;

public class User {
    private final long id;
    private final String username;
    private final String passwordHash;
    private final boolean admin;
    private final Instant createdAt;

    public User(long id, String username, String passwordHash, boolean admin, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.admin = admin;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isAdmin() {
        return admin;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
