package com.mycoinex.exchange.config;

import com.mycoinex.exchange.model.User;

public final class AuthContext {
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }

    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
