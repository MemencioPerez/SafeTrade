package io.github.cjcool06.safetrade.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public final class BukkitEventManagerUtil {

    private BukkitEventManagerUtil() {}

    public static <T extends Event & Cancellable> boolean post(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }
}
