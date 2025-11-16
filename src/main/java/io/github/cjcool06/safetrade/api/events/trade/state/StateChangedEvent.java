package io.github.cjcool06.safetrade.api.events.trade.state;

import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after the {@link Trade}'s state is changed.
 */
public class StateChangedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    public final Trade trade;
    public final TradeState oldState;
    public final TradeState newState;

    public StateChangedEvent(Trade trade, TradeState oldState, TradeState newState) {
        this.trade = trade;
        this.oldState = oldState;
        this.newState = newState;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
