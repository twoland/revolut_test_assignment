package app.model;

import java.math.BigDecimal;
import java.util.Currency;

public class Balance {
    private String accountId;
    private Currency currency;
    private BigDecimal value;

    public Balance(String accountId, Currency currency, BigDecimal value) {
        this.accountId = accountId;
        this.currency = currency;
        this.value = value;
    }

    public BigDecimal add(BigDecimal value) {
        return this.value = this.value.add(value);
    }

    public String getAccountId() {
        return accountId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}

