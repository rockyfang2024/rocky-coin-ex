package com.mycoinex.exchange.model;

import java.math.BigDecimal;
import java.time.Instant;

public class TradeRecord {
    private final long id;
    private final long userId;
    private final TradeCategory category;
    private final String symbol;
    private final String side;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final BigDecimal notional;
    private final Instant executedAt;

    public TradeRecord(long id,
                       long userId,
                       TradeCategory category,
                       String symbol,
                       String side,
                       BigDecimal price,
                       BigDecimal quantity,
                       BigDecimal notional,
                       Instant executedAt) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.notional = notional;
        this.executedAt = executedAt;
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

    public BigDecimal getNotional() {
        return notional;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
