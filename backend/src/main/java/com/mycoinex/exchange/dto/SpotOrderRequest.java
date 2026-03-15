package com.mycoinex.exchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class SpotOrderRequest {
    @NotBlank
    private String symbol;

    @NotBlank
    private String side;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private BigDecimal quantity;

    private Boolean executeImmediately;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Boolean getExecuteImmediately() {
        return executeImmediately;
    }

    public void setExecuteImmediately(Boolean executeImmediately) {
        this.executeImmediately = executeImmediately;
    }
}
