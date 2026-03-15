package com.mycoinex.exchange.model;

import java.time.Instant;

public class SymbolPair {
    private final long id;
    private final String symbol;
    private final String baseCurrency;
    private final String quoteCurrency;
    private boolean spotEnabled;
    private boolean contractEnabled;
    private final Instant createdAt;

    public SymbolPair(long id,
                      String symbol,
                      String baseCurrency,
                      String quoteCurrency,
                      boolean spotEnabled,
                      boolean contractEnabled,
                      Instant createdAt) {
        this.id = id;
        this.symbol = symbol;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.spotEnabled = spotEnabled;
        this.contractEnabled = contractEnabled;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public boolean isSpotEnabled() {
        return spotEnabled;
    }

    public void setSpotEnabled(boolean spotEnabled) {
        this.spotEnabled = spotEnabled;
    }

    public boolean isContractEnabled() {
        return contractEnabled;
    }

    public void setContractEnabled(boolean contractEnabled) {
        this.contractEnabled = contractEnabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
