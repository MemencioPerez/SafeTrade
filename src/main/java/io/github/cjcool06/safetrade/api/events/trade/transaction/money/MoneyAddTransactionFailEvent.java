package io.github.cjcool06.safetrade.api.events.trade.transaction.money;

import io.github.cjcool06.safetrade.obj.MoneyWrapper;
import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after the item failed to be added.
 *
 * <p>If #Add is cancelled, this will be posted.</p>
 */
public class MoneyAddTransactionFailEvent extends MoneyTransactionEvent {

    private static final HandlerList handlers = new HandlerList();

    public MoneyAddTransactionFailEvent(Vault vault, MoneyWrapper wrapper) {
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
