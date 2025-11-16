package io.github.cjcool06.safetrade.api.events.trade.transaction.pokemon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.obj.Vault;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Posted after the Pokemon was successfully added.
 */
public class PokemonAddTransactionSuccessEvent extends PokemonTransactionEvent {

    private static final HandlerList handlers = new HandlerList();

    public PokemonAddTransactionSuccessEvent(Vault vault, Pokemon pokemon) {
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
