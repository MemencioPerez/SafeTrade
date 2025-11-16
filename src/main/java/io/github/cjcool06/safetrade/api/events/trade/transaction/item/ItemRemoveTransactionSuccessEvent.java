package io.github.cjcool06.safetrade.api.events.trade.transaction.item;

import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after the item was successfully removed.
 */
public class ItemRemoveTransactionSuccessEvent extends ItemTransactionEvent {

    private static final HandlerList handlers = new HandlerList();

    public ItemRemoveTransactionSuccessEvent(Vault vault, ItemStack itemStack) {
        super(vault, itemStack);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
