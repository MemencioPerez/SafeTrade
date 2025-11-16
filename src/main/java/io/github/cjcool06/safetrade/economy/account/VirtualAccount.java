package io.github.cjcool06.safetrade.economy.account;

import io.github.cjcool06.safetrade.economy.currency.Currency;

import java.math.BigDecimal;
import java.util.HashMap;

public class VirtualAccount implements Account {

    private final HashMap<Currency, BigDecimal> balances = new HashMap<>();

    @Override
    public HashMap<Currency, BigDecimal> getBalances() {
        return balances;
    }
}
