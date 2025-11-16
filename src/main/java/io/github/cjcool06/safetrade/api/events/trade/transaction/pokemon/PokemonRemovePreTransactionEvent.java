package io.github.cjcool06.safetrade.api.events.trade.transaction.pokemon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted before the Pokemon is attempted to be removed.
 */
public class PokemonRemovePreTransactionEvent extends PokemonTransactionEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    public PokemonRemovePreTransactionEvent(Vault vault, Pokemon pokemon) {
        super(vault, pokemon);
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
