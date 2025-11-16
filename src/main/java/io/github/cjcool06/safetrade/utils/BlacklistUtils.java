package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.config.Config;
import org.bukkit.inventory.ItemStack;
import io.github.cjcool06.safetrade.economy.currency.Currency;

public final class BlacklistUtils {

    public static boolean isBlacklisted(Currency currency) {
        for (String currencyStr : Config.blacklistedCurrencies) {
            if (currency.getId().equalsIgnoreCase(currencyStr)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBlacklisted(ItemStack itemStack) {
        for (String itemStr : Config.blacklistedItems) {
            if (itemStr.equalsIgnoreCase(String.valueOf(itemStack.getType()))) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBlacklisted(Pokemon pokemon) {
        for (String pokemonStr : Config.blacklistedPokemon) {
            if (pokemonStr.equalsIgnoreCase(pokemon.getSpecies().getName())) {
                return true;
            }
        }

        return false;
    }

}
