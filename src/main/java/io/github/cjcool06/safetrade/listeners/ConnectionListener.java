package io.github.cjcool06.safetrade.listeners;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.obj.CommandWrapper;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ConnectionListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
            Player player = event.getPlayer();
            PlayerStorage storage = Tracker.getOrCreateStorage(player);
            List<CommandWrapper> commandsExecuted = storage.executeCommands();
            List<ReadWriteNBT> itemsGiven = storage.giveItems();
            List<Pokemon> pokemonGiven = storage.givePokemon();

            if (!commandsExecuted.isEmpty()) {
                player.spigot().sendMessage(Text.of(ChatColor.DARK_AQUA, "SafeTrade ", ChatColor.GREEN, "executed ", ChatColor.DARK_AQUA, commandsExecuted.size(), ChatColor.GREEN, " commands on your login:"));
                for (CommandWrapper wrapper : commandsExecuted) {
                    player.spigot().sendMessage(Text.of(ChatColor.GREEN, "- ", ChatColor.AQUA, wrapper.cmd));
                }
            }
            if (!itemsGiven.isEmpty()) {
                player.spigot().sendMessage(Text.of(ChatColor.DARK_AQUA, "SafeTrade ", ChatColor.GREEN, "has placed ", ChatColor.DARK_AQUA, itemsGiven.size(), ChatColor.GREEN, " items in to your inventory:"));
                for (ReadWriteNBT item : itemsGiven) {
                    ItemStack itemStack = NBT.itemStackFromNBT(item);
                    if (itemStack == null || itemStack.getItemMeta() == null) continue;
                    player.spigot().sendMessage(Text.of(ChatColor.GREEN, itemStack.getAmount() + "x ", ChatColor.AQUA, itemStack.getItemMeta().getLocalizedName()));
                }
            }
            if (!pokemonGiven.isEmpty()) {
                player.spigot().sendMessage(Text.of(ChatColor.DARK_AQUA, "SafeTrade ", ChatColor.GREEN, "has placed ", ChatColor.DARK_AQUA, pokemonGiven.size(), ChatColor.GREEN, " Pokemon in to your PC:"));
                for (Pokemon pokemon : pokemonGiven) {
                    player.spigot().sendMessage(Text.of(ChatColor.GREEN, "- ", ChatColor.AQUA, pokemon.getLocalizedName()));
                }
            }

            if (!storage.getCommands().isEmpty()) {
                player.spigot().sendMessage(Text.of(ChatColor.GREEN, "You have ", ChatColor.DARK_AQUA, storage.getCommands().size(), ChatColor.GREEN, " commands waiting to be executed."));
            }
            if (!storage.getItems().isEmpty()) {
                player.spigot().sendMessage(Text.of(ChatColor.GREEN, "You have ", ChatColor.DARK_AQUA, storage.getItems().size(), ChatColor.GREEN, " items in your storage."));
            }
            if (!storage.getPokemons().isEmpty()) {
                player.spigot().sendMessage(Text.of(ChatColor.GREEN, "You have ", ChatColor.DARK_AQUA, storage.getPokemons().size(), ChatColor.GREEN, " Pokemon in your storage."));
            }
        });
    }
}
