package com.mycoinex.exchange.controller;

import com.mycoinex.exchange.config.AuthContext;
import com.mycoinex.exchange.dto.ContractOrderRequest;
import com.mycoinex.exchange.dto.SpotOrderRequest;
import com.mycoinex.exchange.model.Order;
import com.mycoinex.exchange.model.Position;
import com.mycoinex.exchange.model.SymbolPair;
import com.mycoinex.exchange.model.TradeRecord;
import com.mycoinex.exchange.model.User;
import com.mycoinex.exchange.service.SymbolService;
import com.mycoinex.exchange.service.TradingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trading")
public class TradingController {
    private final TradingService tradingService;
    private final SymbolService symbolService;

    public TradingController(TradingService tradingService, SymbolService symbolService) {
        this.tradingService = tradingService;
        this.symbolService = symbolService;
    }

    @GetMapping("/symbols")
    public List<SymbolPair> symbols() {
        return symbolService.listAll();
    }

    @PostMapping("/spot/orders")
    public Order placeSpotOrder(@Valid @RequestBody SpotOrderRequest request) {
        User user = AuthContext.getCurrentUser();
        return tradingService.placeSpotOrder(user, request);
    }

    @PostMapping("/spot/orders/{orderId}/cancel")
    public Order cancelSpotOrder(@PathVariable long orderId) {
        User user = AuthContext.getCurrentUser();
        return tradingService.cancelOrder(user, orderId);
    }

    @PostMapping("/contract/orders")
    public Position openContract(@Valid @RequestBody ContractOrderRequest request) {
        User user = AuthContext.getCurrentUser();
        return tradingService.openContractPosition(user, request);
    }

    @PostMapping("/contract/positions/{positionId}/close")
    public Position closeContract(@PathVariable long positionId) {
        User user = AuthContext.getCurrentUser();
        return tradingService.closePosition(user, positionId);
    }

    @GetMapping("/orders")
    public List<Order> orders() {
        User user = AuthContext.getCurrentUser();
        return tradingService.listOrdersForUser(user.getId());
    }

    @GetMapping("/positions")
    public List<Position> positions() {
        User user = AuthContext.getCurrentUser();
        return tradingService.listPositionsForUser(user.getId());
    }

    @GetMapping("/trades")
    public List<TradeRecord> trades() {
        User user = AuthContext.getCurrentUser();
        return tradingService.listTradesForUser(user.getId());
    }

    @GetMapping("/orderbook")
    public Map<String, List<Order>> orderBook(@RequestParam String symbol) {
        return tradingService.getOrderBook(symbol);
    }
}
