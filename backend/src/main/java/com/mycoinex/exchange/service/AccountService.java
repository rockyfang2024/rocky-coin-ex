package com.mycoinex.exchange.service;

import com.mycoinex.exchange.model.AccountType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final Map<Long, EnumMap<AccountType, Map<String, BigDecimal>>> accountStore = new ConcurrentHashMap<>();

    public void initializeAccounts(long userId) {
        EnumMap<AccountType, Map<String, BigDecimal>> accounts = new EnumMap<>(AccountType.class);
        for (AccountType type : AccountType.values()) {
            accounts.put(type, new ConcurrentHashMap<>());
        }
        accountStore.put(userId, accounts);
    }

    public Map<AccountType, Map<String, BigDecimal>> getBalances(long userId) {
        EnumMap<AccountType, Map<String, BigDecimal>> accounts = accountStore.get(userId);
        if (accounts == null) {
            throw new IllegalArgumentException("账户不存在。");
        }
        EnumMap<AccountType, Map<String, BigDecimal>> copy = new EnumMap<>(AccountType.class);
        accounts.forEach((type, balances) -> copy.put(type, new ConcurrentHashMap<>(balances)));
        return copy;
    }

    public BigDecimal getBalance(long userId, AccountType type, String currency) {
        Map<String, BigDecimal> balances = getAccountBalances(userId, type);
        return balances.getOrDefault(currency.toUpperCase(), BigDecimal.ZERO);
    }

    public void deposit(long userId, AccountType type, String currency, BigDecimal amount) {
        Map<String, BigDecimal> balances = getAccountBalances(userId, type);
        String symbol = currency.toUpperCase();
        balances.merge(symbol, normalize(amount), BigDecimal::add);
    }

    public void withdraw(long userId, AccountType type, String currency, BigDecimal amount) {
        Map<String, BigDecimal> balances = getAccountBalances(userId, type);
        String symbol = currency.toUpperCase();
        BigDecimal current = balances.getOrDefault(symbol, BigDecimal.ZERO);
        BigDecimal normalized = normalize(amount);
        if (current.compareTo(normalized) < 0) {
            throw new IllegalArgumentException("余额不足，无法完成操作。");
        }
        balances.put(symbol, current.subtract(normalized));
    }

    public void transfer(long userId, AccountType from, AccountType to, String currency, BigDecimal amount) {
        if (from == to) {
            throw new IllegalArgumentException("划转账户不能相同。");
        }
        withdraw(userId, from, currency, amount);
        deposit(userId, to, currency, amount);
    }

    private Map<String, BigDecimal> getAccountBalances(long userId, AccountType type) {
        EnumMap<AccountType, Map<String, BigDecimal>> accounts = accountStore.get(userId);
        if (accounts == null) {
            throw new IllegalArgumentException("账户不存在。");
        }
        return accounts.get(type);
    }

    private BigDecimal normalize(BigDecimal amount) {
        return amount.setScale(8, RoundingMode.HALF_UP);
    }
}
