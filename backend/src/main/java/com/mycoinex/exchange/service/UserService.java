package com.mycoinex.exchange.service;

import com.mycoinex.exchange.model.AccountType;
import com.mycoinex.exchange.model.User;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final AtomicLong idGenerator = new AtomicLong(1000);
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<Long, User> usersById = new ConcurrentHashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AccountService accountService;

    public UserService(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostConstruct
    public void initializeAdmin() {
        registerInternal("admin", "admin123", true);
    }

    public User register(String username, String password) {
        if (usersByUsername.containsKey(username)) {
            throw new IllegalArgumentException("用户名已存在。");
        }
        return registerInternal(username, password, false);
    }

    public User validateCredentials(String username, String password) {
        User user = usersByUsername.get(username);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误。");
        }
        return user;
    }

    public User findById(long userId) {
        return usersById.get(userId);
    }

    public List<User> listUsers() {
        return new ArrayList<>(usersById.values());
    }

    private User registerInternal(String username, String password, boolean admin) {
        long id = idGenerator.incrementAndGet();
        User user = new User(id, username, passwordEncoder.encode(password), admin, Instant.now());
        usersByUsername.put(username, user);
        usersById.put(id, user);
        accountService.initializeAccounts(id);
        accountService.deposit(id, AccountType.FUNDING, "USDT", new BigDecimal("10000"));
        return user;
    }
}
