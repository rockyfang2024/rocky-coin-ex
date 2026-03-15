package com.mycoinex.exchange.controller;

import com.mycoinex.exchange.config.AuthContext;
import com.mycoinex.exchange.dto.TransferRequest;
import com.mycoinex.exchange.model.AccountType;
import com.mycoinex.exchange.model.User;
import com.mycoinex.exchange.service.AccountService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/overview")
    public Map<AccountType, Map<String, BigDecimal>> overview() {
        User user = AuthContext.getCurrentUser();
        return accountService.getBalances(user.getId());
    }

    @PostMapping("/transfer")
    public Map<AccountType, Map<String, BigDecimal>> transfer(@Valid @RequestBody TransferRequest request) {
        User user = AuthContext.getCurrentUser();
        accountService.transfer(user.getId(),
                request.getFromAccount(),
                request.getToAccount(),
                request.getCurrency(),
                request.getAmount());
        return accountService.getBalances(user.getId());
    }
}
