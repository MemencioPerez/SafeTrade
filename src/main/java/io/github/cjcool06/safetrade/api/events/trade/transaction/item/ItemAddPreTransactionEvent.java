package io.github.cjcool06.safetrade.api.events.trade.transaction.item;

import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemAddPreTransactionEvent extends ItemTransactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    public ItemAddPreTransactionEvent(Vault vault, ItemStack itemStack) {
        super(vault, itemStack);
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
