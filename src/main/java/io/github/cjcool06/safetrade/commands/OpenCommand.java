package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Text;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
// import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

public class OpenCommand implements ChildCommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Open a SafeTrade"))
                .permission("safetrade.common.open")
                .executor(new OpenCommand())
                .build();
    }


    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Trade trade = Tracker.getActiveTrade(player);

            if (trade != null) {
                // Will never be null
                Side side = trade.getSide(player.getUniqueId()).get();
                side.changeInventory(InventoryType.MAIN);
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.GREEN, "Opening trade with ", ChatColor.GOLD, side.getOtherSide().getOfflinePlayer().get().getName(), ChatColor.GREEN, "."));
            } else {
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You are not currently participating in a trade."));
            }
        }
        else {
            sender.sendMessage(Text.of(ChatColor.RED, "You must be a player to do that."));
        }

        return true;
    }
}
