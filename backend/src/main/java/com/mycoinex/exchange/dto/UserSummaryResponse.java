package com.mycoinex.exchange.dto;

import com.mycoinex.exchange.model.AccountType;
import java.math.BigDecimal;
import java.util.Map;

public class UserSummaryResponse {
    private final long userId;
    private final String username;
    private final boolean admin;
    private final Map<AccountType, Map<String, BigDecimal>> balances;

    public UserSummaryResponse(long userId,
                               String username,
                               boolean admin,
                               Map<AccountType, Map<String, BigDecimal>> balances) {
        this.userId = userId;
        this.username = username;
        this.admin = admin;
        this.balances = balances;
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

    public Map<AccountType, Map<String, BigDecimal>> getBalances() {
        return balances;
    }
}
