package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.connection.ConnectionLeftEvent;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.utils.Text;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TradeConnectionListener implements Listener {

    @EventHandler
    public void onLeave(ConnectionLeftEvent event) {
        Side side = event.getSide();
        if (!side.parentTrade.getState().equals(TradeState.ENDED)) {
            side.getPlayer().ifPresent(player -> player.spigot().sendMessage(Text.builder().append(Text.of(ChatColor.GOLD, "You can resume the trade by clicking here or typing /safetrade open"))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/safetrade open")).create()));
        }
    }
}
