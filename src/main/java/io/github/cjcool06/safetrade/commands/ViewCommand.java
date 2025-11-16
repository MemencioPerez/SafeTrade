package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Text;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
// import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import net.md_5.bungee.api.ChatColor;

public class ViewCommand implements ChildCommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("View an ongoing SafeTrade"))
                .permission("safetrade.admin.view")
                .executor(new OpenCommand())
                .arguments(GenericArguments.user(Text.of("target")))
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        if (sender instanceof Player) {
            OfflinePlayer user = args.<OfflinePlayer>getOne("target").get();
            Trade trade = Tracker.getActiveTrade(user);

            if (trade != null) {
                trade.addViewer((Player)sender, true);
                SafeTrade.sendMessageToPlayer((Player)sender, PrefixType.SAFETRADE, Text.of(ChatColor.GREEN, "Opening trade between ", ChatColor.GOLD, trade.getSides()[0].getOfflinePlayer().get().getName(), ChatColor.GREEN, " & ", ChatColor.GOLD, trade.getSides()[1].getOfflinePlayer().get().getName(), ChatColor.GREEN, "."));
            } else {
                SafeTrade.sendMessageToPlayer((Player)sender, PrefixType.SAFETRADE, Text.of(ChatColor.GOLD, user.getName(), ChatColor.RED, " is not currently participating in a SafeTrade."));
            }
        }
        else {
            SafeTrade.sendMessageToCommandSource(sender, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You must be a player to do that."));
        }

        return true;
    }
}
