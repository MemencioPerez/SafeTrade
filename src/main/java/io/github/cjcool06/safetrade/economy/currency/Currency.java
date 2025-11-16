package io.github.cjcool06.safetrade.economy.currency;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Currency {

    @Setting
    private final String id;
    @Setting
    private final String symbol;
    @Setting
    private final String displayName;
    @Setting
    private final String pluralDisplayName;
    @Setting
    private final String balancePlaceholder;
    @Setting
    private final String balanceAddCommand;
    @Setting
    private final String balanceRemoveCommand;

    public Currency(String id, String symbol, String displayName, String pluralDisplayName, String balancePlaceholder, String balanceAddCommand, String balanceRemoveCommand) {
        this.id = id;
        this.symbol = symbol;
        this.displayName = displayName;
        this.pluralDisplayName = pluralDisplayName;
        this.balancePlaceholder = balancePlaceholder;
        this.balanceAddCommand = balanceAddCommand;
        this.balanceRemoveCommand = balanceRemoveCommand;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPluralDisplayName() {
        return pluralDisplayName;
    }

    public String getBalancePlaceholder() {
        return balancePlaceholder;
    }

    public String getBalanceAddCommand() {
        return balanceAddCommand;
    }

    public String getBalanceRemoveCommand() {
        return balanceRemoveCommand;
    }
}
