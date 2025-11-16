package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted when a new {@link Trade} has been created.
 *
 * <p>Specifically, it is fired at the end of the constructor.</p>
 */
public class TradeCreationEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public final Trade trade;

    public TradeCreationEvent(Trade trade) {
        this.trade = trade;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
