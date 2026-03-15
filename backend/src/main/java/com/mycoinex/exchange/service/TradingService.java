package com.mycoinex.exchange.service;

import com.mycoinex.exchange.dto.ContractOrderRequest;
import com.mycoinex.exchange.dto.SpotOrderRequest;
import com.mycoinex.exchange.model.AccountType;
import com.mycoinex.exchange.model.Order;
import com.mycoinex.exchange.model.OrderStatus;
import com.mycoinex.exchange.model.Position;
import com.mycoinex.exchange.model.SymbolPair;
import com.mycoinex.exchange.model.TradeCategory;
import com.mycoinex.exchange.model.TradeRecord;
import com.mycoinex.exchange.model.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TradingService {
    private final AtomicLong orderIdGenerator = new AtomicLong(3000);
    private final AtomicLong tradeIdGenerator = new AtomicLong(5000);
    private final AtomicLong positionIdGenerator = new AtomicLong(7000);
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final Map<Long, Position> positions = new ConcurrentHashMap<>();
    private final List<TradeRecord> trades = new CopyOnWriteArrayList<>();
    private final AccountService accountService;
    private final SymbolService symbolService;

    public TradingService(AccountService accountService, SymbolService symbolService) {
        this.accountService = accountService;
        this.symbolService = symbolService;
    }

    public Order placeSpotOrder(User user, SpotOrderRequest request) {
        SymbolPair pair = requireSpotSymbol(request.getSymbol());
        String side = request.getSide().toUpperCase();
        boolean executeImmediately = request.getExecuteImmediately() == null || request.getExecuteImmediately();
        BigDecimal notional = calculateNotional(request.getPrice(), request.getQuantity());
        if ("BUY".equals(side)) {
            accountService.withdraw(user.getId(), AccountType.SPOT, pair.getQuoteCurrency(), notional);
            if (executeImmediately) {
                accountService.deposit(user.getId(), AccountType.SPOT, pair.getBaseCurrency(), request.getQuantity());
            }
        } else if ("SELL".equals(side)) {
            accountService.withdraw(user.getId(), AccountType.SPOT, pair.getBaseCurrency(), request.getQuantity());
            if (executeImmediately) {
                accountService.deposit(user.getId(), AccountType.SPOT, pair.getQuoteCurrency(), notional);
            }
        } else {
            throw new IllegalArgumentException("现货方向必须为 BUY 或 SELL。");
        }

        OrderStatus status = executeImmediately ? OrderStatus.FILLED : OrderStatus.OPEN;
        Order order = new Order(orderIdGenerator.incrementAndGet(),
                user.getId(),
                TradeCategory.SPOT,
                pair.getSymbol(),
                side,
                request.getPrice(),
                request.getQuantity(),
                status,
                Instant.now(),
                Instant.now());
        orders.put(order.getId(), order);

        if (executeImmediately) {
            trades.add(new TradeRecord(tradeIdGenerator.incrementAndGet(),
                    user.getId(),
                    TradeCategory.SPOT,
                    pair.getSymbol(),
                    side,
                    request.getPrice(),
                    request.getQuantity(),
                    notional,
                    Instant.now()));
        }
        return order;
    }

    public Order cancelOrder(User user, long orderId) {
        Order order = requireOrder(orderId, user.getId());
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new IllegalArgumentException("该订单已成交或已撤销。");
        }
        SymbolPair pair = symbolService.getBySymbol(order.getSymbol());
        if (pair == null) {
            throw new IllegalArgumentException("币对不存在。");
        }
        BigDecimal notional = calculateNotional(order.getPrice(), order.getQuantity());
        if ("BUY".equals(order.getSide())) {
            accountService.deposit(user.getId(), AccountType.SPOT, pair.getQuoteCurrency(), notional);
        } else {
            accountService.deposit(user.getId(), AccountType.SPOT, pair.getBaseCurrency(), order.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(Instant.now());
        return order;
    }

    public Position openContractPosition(User user, ContractOrderRequest request) {
        SymbolPair pair = requireContractSymbol(request.getSymbol());
        String side = request.getSide().toUpperCase();
        if (!"LONG".equals(side) && !"SHORT".equals(side)) {
            throw new IllegalArgumentException("合约方向必须为 LONG 或 SHORT。");
        }
        accountService.withdraw(user.getId(), AccountType.CONTRACT, pair.getQuoteCurrency(), request.getMargin());
        Position position = new Position(positionIdGenerator.incrementAndGet(),
                user.getId(),
                pair.getSymbol(),
                side,
                request.getPrice(),
                request.getQuantity(),
                request.getMargin(),
                Instant.now());
        positions.put(position.getId(), position);

        trades.add(new TradeRecord(tradeIdGenerator.incrementAndGet(),
                user.getId(),
                TradeCategory.CONTRACT,
                pair.getSymbol(),
                side,
                request.getPrice(),
                request.getQuantity(),
                calculateNotional(request.getPrice(), request.getQuantity()),
                Instant.now()));
        return position;
    }

    public Position closePosition(User user, long positionId) {
        Position position = positions.get(positionId);
        if (position == null || position.getUserId() != user.getId()) {
            throw new IllegalArgumentException("未找到可平仓的持仓。");
        }
        if (position.isClosed()) {
            throw new IllegalArgumentException("持仓已平仓。");
        }
        SymbolPair pair = symbolService.getBySymbol(position.getSymbol());
        if (pair == null) {
            throw new IllegalArgumentException("币对不存在。");
        }
        accountService.deposit(user.getId(), AccountType.CONTRACT, pair.getQuoteCurrency(), position.getMargin());
        position.setClosedAt(Instant.now());
        return position;
    }

    public List<Order> listOrdersForUser(long userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId() == userId)
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Position> listPositionsForUser(long userId) {
        return positions.values().stream()
                .filter(position -> position.getUserId() == userId)
                .sorted(Comparator.comparing(Position::getOpenedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<TradeRecord> listTradesForUser(long userId) {
        return trades.stream()
                .filter(trade -> trade.getUserId() == userId)
                .sorted(Comparator.comparing(TradeRecord::getExecutedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<TradeRecord> listAllTrades() {
        return trades.stream()
                .sorted(Comparator.comparing(TradeRecord::getExecutedAt).reversed())
                .collect(Collectors.toList());
    }

    public Map<String, List<Order>> getOrderBook(String symbol) {
        List<Order> openOrders = orders.values().stream()
                .filter(order -> order.getSymbol().equalsIgnoreCase(symbol))
                .filter(order -> order.getStatus() == OrderStatus.OPEN)
                .collect(Collectors.toList());
        Map<String, List<Order>> book = new ConcurrentHashMap<>();
        book.put("bids", openOrders.stream().filter(order -> "BUY".equals(order.getSide()))
                .sorted(Comparator.comparing(Order::getPrice).reversed())
                .collect(Collectors.toList()));
        book.put("asks", openOrders.stream().filter(order -> "SELL".equals(order.getSide()))
                .sorted(Comparator.comparing(Order::getPrice))
                .collect(Collectors.toList()));
        return book;
    }

    private Order requireOrder(long orderId, long userId) {
        Order order = orders.get(orderId);
        if (order == null || order.getUserId() != userId) {
            throw new IllegalArgumentException("订单不存在。");
        }
        return order;
    }

    private SymbolPair requireSpotSymbol(String symbol) {
        SymbolPair pair = symbolService.getBySymbol(symbol);
        if (pair == null || !pair.isSpotEnabled()) {
            throw new IllegalArgumentException("现货币对不可用。");
        }
        return pair;
    }

    private SymbolPair requireContractSymbol(String symbol) {
        SymbolPair pair = symbolService.getBySymbol(symbol);
        if (pair == null || !pair.isContractEnabled()) {
            throw new IllegalArgumentException("合约币对不可用。");
        }
        return pair;
    }

    private BigDecimal calculateNotional(BigDecimal price, BigDecimal quantity) {
        return price.multiply(quantity).setScale(8, RoundingMode.HALF_UP);
    }
}
