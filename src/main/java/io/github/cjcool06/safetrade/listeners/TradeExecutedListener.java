package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.events.trade.TradeEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TradeExecutedListener implements Listener {


    @EventHandler
    public void onExecuted(TradeEvent.Executed.Success event) {

        // Sends a log notification to every online player with the "safetrade.admin.overview" permission node.
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission("safetrade.admin.overview")) {
                SafeTrade.sendMessageToPlayer(player, PrefixType.LOG, event.tradeResult.getTradeLog().getDisplayText());
            }
        }

        // Sends the overview to the trade participants, if online.
        for (OfflinePlayer offlinePlayer : event.getTrade().getParticipants()) {
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                SafeTrade.sendMessageToPlayer(offlinePlayer.getPlayer(), PrefixType.OVERVIEW, event.tradeResult.getTradeLog().getDisplayText());
            }
        }
    }
}
