package io.github.cjcool06.safetrade.api.events.trade.connection;

import io.github.cjcool06.safetrade.obj.Side;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted before the {@link Side} connects to the trade inventory.
 */
public class ConnectionPreJoinEvent extends ConnectionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    public ConnectionPreJoinEvent(Side side) {
        super(side);
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
