package io.github.cjcool06.safetrade.api.events.trade.transaction;

import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.Event;

public abstract class TransactionEvent extends Event {

    protected final Vault vault;

    public TransactionEvent(Vault vault) {
        this.vault = vault;
    }

    public Vault getVault() {
        return vault;
    }
}
