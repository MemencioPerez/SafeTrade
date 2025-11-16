package io.github.cjcool06.safetrade.economy.account;

import io.github.cjcool06.safetrade.economy.currency.Currency;
import io.github.cjcool06.safetrade.economy.transaction.ResultType;
import io.github.cjcool06.safetrade.economy.transaction.TransactionResult;
import io.github.cjcool06.safetrade.economy.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.HashMap;

public interface Account {

    HashMap<Currency, BigDecimal> getBalances();

    default TransactionResult deposit(Currency currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new TransactionResult(this, amount, currency, ResultType.FAILED, TransactionType.DEPOSIT);
        }

        setBalance(currency, amount);
        return new TransactionResult(this, amount, currency, ResultType.SUCCESS, TransactionType.DEPOSIT);
    }

    default TransactionResult transfer(Account to, Currency currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new TransactionResult(this, amount, currency, ResultType.FAILED, TransactionType.TRANSFER);
        }

        BigDecimal currentBalance = getBalance(currency);
        if (currentBalance.compareTo(amount) < 0) {
            return new TransactionResult(this, amount, currency, ResultType.ACCOUNT_NO_FUNDS, TransactionType.TRANSFER);
        }

        withdraw(currency, amount);
        to.deposit(currency, amount);
        return new TransactionResult(this, amount, currency, ResultType.SUCCESS, TransactionType.TRANSFER);
    }

    default TransactionResult withdraw(Currency currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new TransactionResult(this, amount, currency, ResultType.FAILED, TransactionType.WITHDRAW);
        }

        BigDecimal currentBalance = getBalance(currency);
        if (currentBalance.compareTo(amount) < 0) {
            return new TransactionResult(this, amount, currency, ResultType.ACCOUNT_NO_FUNDS, TransactionType.WITHDRAW);
        }

        setBalance(currency, amount.negate());
        return new TransactionResult(this, amount, currency, ResultType.SUCCESS, TransactionType.WITHDRAW);
    }

    default BigDecimal getBalance(Currency currency) {
        return getBalances().getOrDefault(currency, BigDecimal.ZERO);
    }

    default void setBalance(Currency currency, BigDecimal amount) {
        getBalances().merge(currency, amount, BigDecimal::add);
    }

    default void resetBalances() {
        getBalances().clear();
    }
}