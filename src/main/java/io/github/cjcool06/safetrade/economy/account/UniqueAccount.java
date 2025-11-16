package io.github.cjcool06.safetrade.economy.account;

import io.github.cjcool06.safetrade.economy.currency.Currency;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class UniqueAccount implements Account {

    private final HashMap<Currency, BigDecimal> balances = new HashMap<>();
    private final UUID ownerUUID;

    public UniqueAccount(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getUniqueId() {
        return ownerUUID;
    }

    @Override
    public HashMap<Currency, BigDecimal> getBalances() {
        return balances;
    }
}
