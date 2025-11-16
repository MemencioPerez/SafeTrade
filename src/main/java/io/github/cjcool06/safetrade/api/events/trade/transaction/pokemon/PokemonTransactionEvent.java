package io.github.cjcool06.safetrade.api.events.trade.transaction.pokemon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.api.events.trade.transaction.TransactionEvent;
import io.github.cjcool06.safetrade.obj.Vault;

public abstract class PokemonTransactionEvent extends TransactionEvent {

    protected final Pokemon pokemon;

    public PokemonTransactionEvent(Vault vault, Pokemon pokemon) {
        super(vault);
        this.pokemon = pokemon;
    }
}
