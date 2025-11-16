package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.events.trade.connection.ConnectionEvent;
import io.github.cjcool06.safetrade.api.events.trade.connection.ConnectionPostJoinEvent;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Text;
import io.github.cjcool06.safetrade.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import io.github.cjcool06.safetrade.obj.PaginationList;
import org.spongepowered.api.text.action.TextActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TradeCommand implements ChildCommandExecutor {
    private static HashMap<OfflinePlayer, ArrayList<OfflinePlayer>> tradeRequests = new HashMap<>();

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Trade with another player"))
                .permission("safetrade.common.trade")
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("target"))))
                .executor(new TradeCommand())
                .child(OpenCommand.getSpec(), "open")
                .child(EndCommand.getSpec(), "end")
                .child(StorageCommand.getSpec(), "storage")
                .child(LogsCommand.getSpec(), "logs")
                .child(ViewCommand.getSpec(), "view")
                //.child(TestCommand.getSpec(), "test")
                .child(ReloadCommand.getSpec(), "reload")
                .child(WikiCommand.getSpec(), "wiki")
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        if (!args.<Player>getOne("target").isPresent()) {
            List<BaseComponent[]> contents = new ArrayList<>();

            contents.add(Text.of(ChatColor.AQUA, "/safetrade <player>", ChatColor.GRAY, " - ", ChatColor.GRAY, "Request/Accept a SafeTrade"));
            contents.add(Text.of(ChatColor.AQUA, "/safetrade open", ChatColor.GRAY, " - ", ChatColor.GRAY, "Open your current SafeTrade"));
            contents.add(Text.of(ChatColor.AQUA, "/safetrade wiki", ChatColor.GRAY, " - ", ChatColor.GRAY, "Gives the wiki link"));
            contents.add(Text.of(ChatColor.AQUA, "/safetrade end <player>", ChatColor.GRAY, " - ", ChatColor.GRAY, "Force end a SafeTrade"));
            contents.add(Text.of(ChatColor.AQUA, "/safetrade view <player>", ChatColor.GRAY, " - ", ChatColor.GRAY, "View a player's SafeTrade"));
            contents.add(Text.of(ChatColor.AQUA, "/safetrade logs <OfflinePlayer> [other OfflinePlayer]", ChatColor.GRAY, " - ", ChatColor.GRAY, "Browse a player's SafeTrade logs"));
            contents.add(Text.of(ChatColor.AQUA, "/safetrade storage <OfflinePlayer> [clear]", ChatColor.GRAY, " - ", ChatColor.GRAY, "Open a OfflinePlayer's SafeTrade storage"));
            contents.add(Text.of(ChatColor.AQUA, "/safetrade reload", ChatColor.GRAY, " - ", ChatColor.GRAY, "Reloads the config"));

            PaginationList.builder()
                    .title(Text.of(ChatColor.GREEN, " SafeTrade "))
                    .contents(contents)
                    .padding(Text.of(ChatColor.AQUA, "-", ChatColor.RESET))
                    .sendTo(sender);

            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player)sender;
            Player target = args.<Player>getOne("target").get();

            if (player.equals(target)) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You can't trade with yourself you banana."));
            }
            else if (Tracker.getActiveTrade(player) != null) {
                Side side = Tracker.getActiveTrade(player).getSide(player.getUniqueId()).get();
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You are already a participant in a SafeTrade.", "\n",
                        Text.of(ChatColor.GOLD, TextActions.executeCallback(dummySender -> Sponge.getCommandManager().process(side.getPlayer().get(), "safetrade open"))), "Click here to open your existing trade."));
            }
            else if (Tracker.getActiveTrade(target) != null) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "That player is currently trading with another player."));
            }
            else if (tradeRequests.containsKey(player) && tradeRequests.get(player).contains(target)) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "There is already a trade request pending with that player. Requests expire after 2 minutes."));
            }
            // Catches if the requestee uses the command to trade instead of using the executable.
            else if (tradeRequests.containsKey(target) && tradeRequests.get(target).contains(player)) {
                acceptInvitation(target, player);
            }
            else {
                requestTrade(player, target);
            }
        }
        else {
            SafeTrade.sendMessageToCommandSource(sender, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You must be a player to do that."));
        }

        return true;
    }

    public static void requestTrade(Player requester, Player requestee) {
        SafeTrade.sendMessageToPlayer(requestee, PrefixType.SAFETRADE, Text.of(ChatColor.GOLD, requester.getName(), ChatColor.GREEN, " has requested a trade. ",
                Text.of(ChatColor.DARK_GREEN, TextActions.executeCallback(dummySender -> acceptInvitation(requester, requestee)), "[Accept]"),
                " ",
                Text.of(ChatColor.RED, TextActions.executeCallback(dummySender -> rejectInvitation(requester, requestee)), "[Decline]")));

        if (!tradeRequests.containsKey(requester)) {
            tradeRequests.put(requester, new ArrayList<>());
        }
        tradeRequests.get(requester).add(requestee);

        SafeTrade.sendMessageToPlayer(requester, PrefixType.SAFETRADE, Text.of(ChatColor.GREEN, "Trade request sent to ", ChatColor.GOLD, requestee.getName(), ChatColor.GREEN, "."));

        // Cancels request after 2 minutes
        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    if (tradeRequests.containsKey(requester) && tradeRequests.get(requester).contains(requestee))
                        tradeRequests.get(requester).remove(requestee);
                })
                .delay(2, TimeUnit.MINUTES)
                .async()
                .submit(SafeTrade.getPlugin());
    }

    public static void rejectInvitation(Player requester, Player requestee) {
        if (tradeRequests.containsKey(requester) && tradeRequests.get(requester).contains(requestee)) {
            tradeRequests.get(requester).remove(requestee);
            SafeTrade.sendMessageToPlayer(requester, PrefixType.SAFETRADE, Text.of(ChatColor.GOLD, requestee.getName(), ChatColor.RED, " rejected your trade request."));
            SafeTrade.sendMessageToPlayer(requestee, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "Rejected ", ChatColor.GOLD, requester.getName() +  "'s ", ChatColor.RED, " trade request."));
        }
    }

    public static void acceptInvitation(Player requester, Player requestee) {
        if (tradeRequests.containsKey(requester) && tradeRequests.get(requester).contains(requestee)) {
            tradeRequests.get(requester).remove(requestee);

            if (Utils.isPlayerOccupied(requester)) {
                SafeTrade.sendMessageToPlayer(requester, PrefixType.SAFETRADE, Text.of(ChatColor.GOLD, requestee.getName(), ChatColor.RED, " has accepted your trade request, but you are otherwise occupied."));
                SafeTrade.sendMessageToPlayer(requestee, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "You have accepted ", ChatColor.GOLD, requester.getName() + "'s", ChatColor.RED, " trade request, but they are otherwise occupied."));
                return;
            }
            if (Utils.isPlayerOccupied(requestee)) {
                requester.spigot().sendMessage(Text.of(ChatColor.GOLD, requestee.getName(), ChatColor.RED, " has accepted your trade request, but they are otherwise occupied."));
                requestee.spigot().sendMessage(Text.of(ChatColor.RED, "You have accepted ", ChatColor.GOLD, requester.getName() + "'s", ChatColor.RED, " trade request, but you are otherwise occupied."));
                return;
            }

            // The initial open needs to be like this, otherwise players will be flagged as paused unless they pause or close inv and resume.
            // This is because no player cause is given to the InteractInventoryEvent.Open event. Not sure why.
            Trade trade = new Trade(requester, requestee);
            Side side0 = trade.getSides()[0];
            Side side1 = trade.getSides()[1];
            side0.getPlayer().ifPresent(player -> {
                side0.setPaused(false);
                trade.reformatInventory();
                side0.changeInventory(InventoryType.MAIN);
                Bukkit.getPluginManager().callEvent(new ConnectionPostJoinEvent(side0));
            });
            side1.getPlayer().ifPresent(player -> {
                side1.setPaused(false);
                trade.reformatInventory();
                side1.changeInventory(InventoryType.MAIN);
                Bukkit.getPluginManager().callEvent(new ConnectionPostJoinEvent(side1));
            });
        }
    }
}
