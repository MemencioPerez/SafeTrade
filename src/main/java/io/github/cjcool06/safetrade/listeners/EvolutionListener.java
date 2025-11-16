package io.github.cjcool06.safetrade.listeners;

import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import io.github.cjcool06.safetrade.SafeTrade;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EvolutionListener implements Listener {
    public static List<UUID> ongoingEvolutions = new ArrayList<>();

    @SubscribeEvent
    public void onEvolve(EvolveEvent.Post event) {
        Pokemon pokemon = event.getPokemon();
        if (pokemon != null) {
            if (ongoingEvolutions.contains(pokemon.getUUID())) {
                ongoingEvolutions.remove(pokemon.getUUID());
                Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> pokemon.getPixelmonEntity().ifPresent(PixelmonEntity::unloadEntity));
            }
        }
    }
}
