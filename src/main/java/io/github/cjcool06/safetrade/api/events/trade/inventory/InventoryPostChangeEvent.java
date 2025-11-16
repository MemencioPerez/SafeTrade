package io.github.cjcool06.safetrade.api.events.trade.inventory;

import io.github.cjcool06.safetrade.obj.Side;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after a {@link Side} changes inventory.
 */
public class InventoryPostChangeEvent extends InventoryChangeEvent {

    private static final HandlerList handlers = new HandlerList();

    public InventoryPostChangeEvent(Side side) {
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
