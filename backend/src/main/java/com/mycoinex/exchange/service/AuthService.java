package com.mycoinex.exchange.service;

import com.mycoinex.exchange.dto.AuthResponse;
import com.mycoinex.exchange.model.User;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public AuthResponse login(String username, String password) {
        User user = userService.validateCredentials(username, password);
        return issueToken(user);
    }

    public AuthResponse register(String username, String password) {
        User user = userService.register(username, password);
        return issueToken(user);
    }

    public User authenticate(String token) {
        Long userId = tokens.get(token);
        if (userId == null) {
            return null;
        }
        return userService.findById(userId);
    }

    private AuthResponse issueToken(User user) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, user.getId());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.isAdmin());
    }
}
