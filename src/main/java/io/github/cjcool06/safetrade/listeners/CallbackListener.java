package io.github.cjcool06.safetrade.listeners;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CallbackListener implements Listener {
    private final static HashMap<UUID, Consumer<Player>> callbacks = new HashMap<>();

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e){
        if(e.getMessage().startsWith("/safetrade:callback")){
            String[] args = e.getMessage().split(" ");
            if(args.length == 2){
                if(args[1].split("-").length == 5){
                    UUID uuid = UUID.fromString(args[1]);
                    Consumer<Player> c = callbacks.remove(uuid);
                    if(c != null){
                        c.accept(e.getPlayer());
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    public static String createCommand(Consumer<Player> consumer){
        UUID uuid = UUID.randomUUID();
        callbacks.put(uuid, consumer);
        return "/spigot:callback " + uuid;
    }
}