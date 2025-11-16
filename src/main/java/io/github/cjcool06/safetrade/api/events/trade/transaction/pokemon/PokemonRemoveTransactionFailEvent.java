package io.github.cjcool06.safetrade.api.events.trade.transaction.pokemon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after the Pokemon failed to be removed.
 *
 * <p>If #Add is cancelled, this will be posted.</p>
 */
public class PokemonRemoveTransactionFailEvent extends PokemonTransactionEvent {

    private static final HandlerList handlers = new HandlerList();

    public PokemonRemoveTransactionFailEvent(Vault vault, Pokemon pokemon) {
        super(vault, pokemon);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
