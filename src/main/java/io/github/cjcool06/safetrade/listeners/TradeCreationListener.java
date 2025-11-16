package io.github.cjcool06.safetrade.listeners;

import io.github.cjcool06.safetrade.api.events.trade.TradeCreationEvent;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.utils.Text;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TradeCreationListener implements Listener {

    @EventHandler
    public void onHandshake(TradeCreationEvent event) {
        Trade trade = event.trade;
        trade.sendChannelMessage(Text.of(ChatColor.GREEN, "Trade channel initialised."));
        trade.sendChannelMessage(Text.builder().append(Text.of(ChatColor.GOLD, "If you're unsure about this chat or how to conduct a SafeTrade, click here.")).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/CJcool06/SafeTrade/wiki")).create());
    }
}
