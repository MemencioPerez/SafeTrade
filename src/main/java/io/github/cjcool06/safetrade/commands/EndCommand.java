package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Text;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

public class EndCommand implements ChildCommandExecutor {
    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("End a trade"))
                .permission("safetrade.admin.end")
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .executor(new EndCommand())
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        Trade trade;
        if (!args.<Player>getOne("target").isPresent()) {
            if (sender instanceof Player) {
                trade = Tracker.getActiveTrade((Player) sender);

                if (trade != null) {
                    trade.sendMessage(Text.of(ChatColor.GRAY, "Trade ended by " + sender.getName() + "."));
                    trade.forceEnd();
                } else {
                    SafeTrade.sendMessageToPlayer((Player) sender, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You are not currently participating in a trade."));
                }
            }
            else {
                SafeTrade.sendMessageToCommandSource(sender, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You must be a player to do that."));
            }
        }
        else {
            Player target = args.<Player>getOne("target").get();
            trade = Tracker.getActiveTrade(target);

            if (trade == null) {
                SafeTrade.sendMessageToCommandSource(sender, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "That player is not currently participating in a trade."));
                return true;
            }
            trade.sendChannelMessage(Text.of(ChatColor.GRAY, "Trade force ended by " + sender.getName() + "."));
            trade.forceEnd();

            SafeTrade.sendMessageToCommandSource(sender, PrefixType.SAFETRADE, Text.of(
                    ChatColor.GREEN, "Force ended ", ChatColor.GOLD, trade.getSides()[0].getOfflinePlayer().get().getName() + "'s & " +
                            trade.getSides()[1].getOfflinePlayer().get().getName(), ChatColor.GREEN, "'s SafeTrade."
            ));
        }

        return true;
    }
}
