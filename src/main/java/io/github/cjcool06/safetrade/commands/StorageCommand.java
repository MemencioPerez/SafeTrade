package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.ImmutableMap;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.trackers.Tracker;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.bukkit.command.CommandSender;
// import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.bukkit.command.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import net.md_5.bungee.api.ChatColor;

public class StorageCommand implements ChildCommandExecutor {
    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(BaseComponentUtils.of("Opens a player's storage"))
                .permission("safetrade.common.storage.view")
                .arguments(
                        GenericArguments.optional(
                                GenericArguments.user(BaseComponentUtils.of("target"))
                        ),
                        GenericArguments.optional(
                                GenericArguments.choices(BaseComponentUtils.of("options"),
                                        ImmutableMap.<String, String>builder()
                                                .put("clear", "clear")
                                                .build())
                        ))
                .executor(new StorageCommand())
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        OfflinePlayer target = args.<OfflinePlayer>getOne("target").isPresent() ? args.<OfflinePlayer>getOne("target").get() : (User) sender;
        PlayerStorage storage;

        if (args.<String>getOne("options").isPresent()) {
            String operation = args.<String>getOne("options").get();

            if (operation.equalsIgnoreCase("clear")) {
                if (!sender.hasPermission("safetrade.admin.storage.clear")) {
                    SafeTrade.sendMessageToCommandSource(sender, PrefixType.STORAGE, BaseComponentUtils.of(ChatColor.RED, "You do not have permission to clear storages."));
                    return true;
                }

                storage = Tracker.getStorage(target);
                if (storage != null && !storage.isEmpty()) {
                    storage.clearAll();
                    SafeTrade.sendMessageToCommandSource(sender, PrefixType.STORAGE, BaseComponentUtils.of(ChatColor.GOLD, target.getName() + "'s", ChatColor.GREEN, " storage has been cleared."));
                }
                else {
                    SafeTrade.sendMessageToCommandSource(sender, PrefixType.STORAGE, BaseComponentUtils.of(ChatColor.GOLD, target.getName() + "'s", ChatColor.RED, " storage is already empty."));
                }
            }

            return true;
        }

        else if (sender instanceof Player) {
            Player player = (Player)sender;

            if (!player.getUniqueId().equals(target.getUniqueId()) && !player.hasPermission("safetrade.admin.storage.view")) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, BaseComponentUtils.of(ChatColor.RED, "You do not have permission to open another player's storage"));
            }

            storage = Tracker.getOrCreateStorage(target);
            storage.open(player);
            SafeTrade.sendMessageToCommandSource(sender, PrefixType.STORAGE, BaseComponentUtils.of(ChatColor.GREEN, "Opening ", player.getUniqueId().equals(storage.getPlayerUUID()) ? BaseComponentUtils.of("your") : BaseComponentUtils.of(ChatColor.GOLD, target.getName() + "'s"), ChatColor.GREEN, " storage."));
        }
        else {
            SafeTrade.sendMessageToCommandSource(sender, PrefixType.STORAGE, BaseComponentUtils.of(ChatColor.RED, "You must be a player to do that."));
        }

        return true;
    }
}
