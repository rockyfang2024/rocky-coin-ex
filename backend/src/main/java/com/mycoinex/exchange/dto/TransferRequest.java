package com.mycoinex.exchange.dto;

import com.mycoinex.exchange.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class TransferRequest {
    @NotNull
    private AccountType fromAccount;

    @NotNull
    private AccountType toAccount;

    @NotBlank
    private String currency;

    @NotNull
    @Positive
    private BigDecimal amount;

    public AccountType getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(AccountType fromAccount) {
        this.fromAccount = fromAccount;
    }

    public AccountType getToAccount() {
        return toAccount;
    }

    public void setToAccount(AccountType toAccount) {
        this.toAccount = toAccount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
