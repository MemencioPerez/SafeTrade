package io.github.cjcool06.safetrade.api.events.trade.transaction.money;

import io.github.cjcool06.safetrade.obj.MoneyWrapper;
import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after the item was successfully added.
 */
public class MoneyAddTransactionSuccessEvent extends MoneyTransactionEvent {

    private static final HandlerList handlers = new HandlerList();

    public MoneyAddTransactionSuccessEvent(Vault vault, MoneyWrapper wrapper) {
        super(vault, wrapper);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
