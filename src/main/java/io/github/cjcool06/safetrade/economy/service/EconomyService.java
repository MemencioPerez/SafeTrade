package io.github.cjcool06.safetrade.economy.service;

import io.github.cjcool06.safetrade.config.CurrenciesConfig;
import io.github.cjcool06.safetrade.economy.account.Account;
import io.github.cjcool06.safetrade.economy.account.AccountDeletionResultType;
import io.github.cjcool06.safetrade.economy.account.UniqueAccount;
import io.github.cjcool06.safetrade.economy.account.VirtualAccount;
import io.github.cjcool06.safetrade.economy.currency.Currency;

import java.util.*;

public class EconomyService {

    private static EconomyService instance;
    private final Set<Currency> currencies;
    private final Map<String, Account> accounts = new HashMap<>();

    private EconomyService(Set<Currency> currencies) {
        this.currencies = currencies;
    }

    public static EconomyService get() {
        if (instance == null) {
            instance = new EconomyService(CurrenciesConfig.currencies);
        }

        return instance;
    }

    public Set<Currency> getCurrencies() {
        return currencies;
    }

    public Optional<Account> getOrCreateAccount(UUID uuid) {
        return Optional.of(accounts.getOrDefault(uuid.toString(), new UniqueAccount(uuid)));
    }

    public Optional<Account> getOrCreateAccount(String identifier) {
        try {
            UUID uuid = UUID.fromString(identifier);
            return getOrCreateAccount(uuid);
        } catch (IllegalArgumentException ignored) {
        }
        return Optional.of(accounts.getOrDefault(identifier, new VirtualAccount()));
    }

    public AccountDeletionResultType deleteAccount(UUID uuid) {
        try {
            if (accounts.remove(uuid.toString()) == null) {
                return AccountDeletionResultType.ABSENT;
            }
        } catch (Exception e) {
            return AccountDeletionResultType.FAILED;
        }
        return AccountDeletionResultType.SUCCESS;
    }

    public AccountDeletionResultType deleteAccount(String identifier) {
        try {
            UUID uuid = UUID.fromString(identifier);
            return deleteAccount(uuid);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            if (accounts.remove(identifier) == null) {
                return AccountDeletionResultType.ABSENT;
            }
        } catch (Exception e) {
            return AccountDeletionResultType.FAILED;
        }
        return AccountDeletionResultType.SUCCESS;
    }
}
