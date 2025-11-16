package io.github.cjcool06.safetrade.api.events.trade.transaction.item;

import io.github.cjcool06.safetrade.api.events.trade.transaction.TransactionEvent;
import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public abstract class ItemTransactionEvent extends TransactionEvent {

    protected final ItemStack itemStack;

    public ItemTransactionEvent(Vault vault, ItemStack itemStack) {
        super(vault);
        this.itemStack = itemStack;
    }

    public static class Remove extends Item {

        private Remove(Vault vault, ItemStack itemStack) {
            super(vault, itemStack);
        }

        public static class Pre extends Remove implements Cancellable {
            private boolean cancel = false;

            public Pre(Vault vault, ItemStack itemStack) {
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
        }

        public static class Success extends Remove {
            public Success(Vault vault, ItemStack itemStack) {
                super(vault, itemStack);
            }
        }

        public static class Fail extends Remove {
            public Fail(Vault vault, ItemStack itemStack) {
                super(vault, itemStack);
            }
        }
    }
}
