package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.events.trade.viewer.ViewerEvent;
import io.github.cjcool06.safetrade.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ViewerConnectionListener implements Listener {

    @EventHandler
    public void onViewer(ViewerEvent event) {
        if (event instanceof ViewerEvent.Add) {
            event.trade.sendChannelMessage(Text.of(ChatColor.GOLD, event.viewer.getName(), ChatColor.GREEN, " is viewing the trade."));
        }
        else if (event instanceof ViewerEvent.Remove) {
            event.trade.sendChannelMessage(Text.of(ChatColor.GOLD, event.viewer.getName(), ChatColor.RED, " is no longer viewing the trade."));
        }
    }
}