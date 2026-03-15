package com.mycoinex.exchange.controller;

import com.mycoinex.exchange.dto.AirdropRequest;
import com.mycoinex.exchange.dto.SymbolRequest;
import com.mycoinex.exchange.dto.UserSummaryResponse;
import com.mycoinex.exchange.model.AccountType;
import com.mycoinex.exchange.model.SymbolPair;
import com.mycoinex.exchange.model.TradeRecord;
import com.mycoinex.exchange.model.User;
import com.mycoinex.exchange.service.AccountService;
import com.mycoinex.exchange.service.SymbolService;
import com.mycoinex.exchange.service.TradingService;
import com.mycoinex.exchange.service.UserService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final SymbolService symbolService;
    private final TradingService tradingService;
    private final UserService userService;
    private final AccountService accountService;

    public AdminController(SymbolService symbolService,
                           TradingService tradingService,
                           UserService userService,
                           AccountService accountService) {
        this.symbolService = symbolService;
        this.tradingService = tradingService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping("/symbols")
    public List<SymbolPair> symbols() {
        return symbolService.listAll();
    }

    @PostMapping("/symbols")
    public SymbolPair createSymbol(@Valid @RequestBody SymbolRequest request) {
        return symbolService.createOrUpdate(request.getSymbol(),
                request.getBaseCurrency(),
                request.getQuoteCurrency(),
                request.isSpotEnabled(),
                request.isContractEnabled());
    }

    @GetMapping("/trades")
    public List<TradeRecord> trades(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            return tradingService.listAllTrades();
        }
        return tradingService.listTradesForUser(userId);
    }

    @PostMapping("/airdrop")
    public Map<AccountType, Map<String, BigDecimal>> airdrop(@Valid @RequestBody AirdropRequest request) {
        accountService.deposit(request.getUserId(), AccountType.FUNDING, request.getCurrency(), request.getAmount());
        return accountService.getBalances(request.getUserId());
    }

    @GetMapping("/users")
    public List<UserSummaryResponse> users() {
        return userService.listUsers().stream()
                .map(user -> new UserSummaryResponse(user.getId(),
                        user.getUsername(),
                        user.isAdmin(),
                        accountService.getBalances(user.getId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/balances")
    public Map<AccountType, Map<String, BigDecimal>> balances(@PathVariable long userId) {
        return accountService.getBalances(userId);
    }
}
