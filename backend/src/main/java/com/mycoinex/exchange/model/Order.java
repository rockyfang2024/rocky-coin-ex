package com.mycoinex.exchange.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {
    private final long id;
    private final long userId;
    private final TradeCategory category;
    private final String symbol;
    private final String side;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(long id,
                 long userId,
                 TradeCategory category,
                 String symbol,
                 String side,
                 BigDecimal price,
                 BigDecimal quantity,
                 OrderStatus status,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public TradeCategory getCategory() {
        return category;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
