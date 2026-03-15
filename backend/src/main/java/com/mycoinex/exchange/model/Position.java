package com.mycoinex.exchange.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Position {
    private final long id;
    private final long userId;
    private final String symbol;
    private final String side;
    private final BigDecimal entryPrice;
    private final BigDecimal quantity;
    private final BigDecimal margin;
    private final Instant openedAt;
    private Instant closedAt;

    public Position(long id,
                    long userId,
                    String symbol,
                    String side,
                    BigDecimal entryPrice,
                    BigDecimal quantity,
                    BigDecimal margin,
                    Instant openedAt) {
        this.id = id;
        this.userId = userId;
        this.symbol = symbol;
        this.side = side;
        this.entryPrice = entryPrice;
        this.quantity = quantity;
        this.margin = margin;
        this.openedAt = openedAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSide() {
        return side;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public boolean isClosed() {
        return closedAt != null;
    }
}
