package io.github.cjcool06.safetrade.api.events.trade.inventory;

import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.obj.Side;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted before a {@link Side} changes inventory.
 */
public class InventoryPreChangeEvent extends InventoryChangeEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    public final InventoryType newInventory;

    public InventoryPreChangeEvent(Side side, InventoryType newInventory) {
        super(side);
        this.newInventory = newInventory;
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
