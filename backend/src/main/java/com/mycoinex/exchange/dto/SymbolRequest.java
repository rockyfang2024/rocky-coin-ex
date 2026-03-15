package com.mycoinex.exchange.dto;

import jakarta.validation.constraints.NotBlank;

public class SymbolRequest {
    @NotBlank
    private String symbol;

    @NotBlank
    private String baseCurrency;

    @NotBlank
    private String quoteCurrency;

    private boolean spotEnabled;
    private boolean contractEnabled;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
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
}
