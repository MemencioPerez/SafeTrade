package io.github.cjcool06.safetrade.economy.transaction;

import io.github.cjcool06.safetrade.economy.account.Account;
import io.github.cjcool06.safetrade.economy.currency.Currency;

import java.math.BigDecimal;

public class TransactionResult {
    private final Account account;
    private final BigDecimal amount;
    private final Currency currency;
    private final ResultType resultType;
    private final TransactionType transactionType;

    public TransactionResult(Account account, BigDecimal amount, Currency currency, ResultType resultType, TransactionType transactionType) {
        this.account = account;
        this.amount = amount;
        this.currency = currency;
        this.resultType = resultType;
        this.transactionType = transactionType;
    }

    public Account getAccount() {
        return account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public ResultType getResult() {
        return resultType;
    }

    public TransactionType getType() {
        return transactionType;
    }
}
