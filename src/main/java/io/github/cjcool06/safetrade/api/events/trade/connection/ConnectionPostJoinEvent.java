package io.github.cjcool06.safetrade.api.events.trade.connection;

import io.github.cjcool06.safetrade.obj.Side;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after the {@link Side} connects to the trade inventory.
 */
public class ConnectionPostJoinEvent extends ConnectionEvent {

    private static final HandlerList handlers = new HandlerList();

    public ConnectionPostJoinEvent(Side side) {
        super(side);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
