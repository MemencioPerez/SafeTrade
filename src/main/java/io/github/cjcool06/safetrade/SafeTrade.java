package io.github.cjcool06.safetrade;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.pixelmonmod.pixelmon.Pixelmon;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.commands.TradeCommandExecutor;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.config.CurrenciesConfig;
import io.github.cjcool06.safetrade.economy.service.EconomyService;
import io.github.cjcool06.safetrade.listeners.*;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Trade;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.Text;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileReader;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.guice.GuiceObjectMapperProvider;
import org.bukkit.Bukkit;

import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.BaseComponent;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class SafeTrade extends JavaPlugin {

    private static SafeTrade plugin;
    private EconomyService economyService;

    private GuiceObjectMapperProvider factory;

    private Logger logger;

    private File dataFolder;

    private BukkitAudiences adventure;

    @NonNull
    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onLoad() {
        plugin = this;
        this.adventure = BukkitAudiences.create(this);
        Injector injector = Guice.createInjector();
        factory = injector.getInstance(GuiceObjectMapperProvider.class);
        logger = getLogger();
        dataFolder = new File(getDataFolder().getAbsolutePath());

        Pixelmon.EVENT_BUS.register(new EvolutionListener());

        Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new TradeCreationListener(), this);
        Bukkit.getPluginManager().registerEvents(new ViewerConnectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new TradeExecutedListener(), this);
        Bukkit.getPluginManager().registerEvents(new TradeConnectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new CallbackListener(), this);
        logger.info("Listeners registered.");

        try {
            Commodore commodore = CommodoreProvider.getCommodore(this);
            LiteralCommandNode<?> safeTradeBrigadierCommandNode = CommodoreFileReader.INSTANCE.parse(plugin.getResource("safetrade.commodore"));
            PluginCommand safeTradeBukkitCommand = Bukkit.getServer().getPluginCommand("safetrade");
            commodore.register(safeTradeBukkitCommand, safeTradeBrigadierCommandNode);
            Objects.requireNonNull(safeTradeBukkitCommand).setExecutor(new TradeCommandExecutor());
            logger.info("Commands registered.");
        } catch (IOException | NullPointerException e) {
            logger.severe("Failed to load safetrade command.");
        }

        Config.load();
        logger.info("Config loaded.");

        CurrenciesConfig.load();
        logger.info("Currencies config loaded.");
    }

    @Override
    public void onEnable() {
        economyService = EconomyService.get();

        Bukkit.getScheduler()
                .runTaskTimerAsynchronously(this, () -> {
                    if (Config.gcLogsEnabled) {
                        int num = DataManager.recycleLogs();
                        if (num > 0) {
                            logger.info("Garbage Collector >> Removed " + num + " old logs.");
                        }
                    }
                }, 20L * 60L * 5L, 20L * 60L * 60L);

        DataManager.load();
        logger.info("Data loaded.");
    }

    @Override
    public void onDisable() {
        logger.info("Executing shutdown tasks.");
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        for (Trade trade : Tracker.getAllActiveTrades()) {
            trade.unloadToStorages();
        }
        DataManager.save();
        logger.info("Shutdown tasks completed.");
    }

    /**
     * Gets SafeTrade's current {@link EconomyService}.
     *
     * @return The service
     */
    public static EconomyService getEcoService() {
        return getPlugin().economyService;
    }

    /**
     * Gets the instance of this plugin.
     *
     * @return The instance
     */
    public static SafeTrade getPlugin() {
        return plugin;
    }

    /**
     * Gets the data folder of this plugin.
     *
     * @return The instance
     */
    public static File getPluginDataFolder() {
        return plugin.dataFolder;
    }

    /**
     * Gets the {@link Logger} of this plugin.
     *
     * @return The logger
     */
    public static Logger getPluginLogger() {
        return plugin.logger;
    }

    /**
     * Gets the {@link GuiceObjectMapperProvider} of this plugin.
     *
     * @return The factory
     */
    public static GuiceObjectMapperProvider getFactory() {
        return plugin.factory;
    }

    /**
     * Sends a {@link BaseComponent} message to a {@link Player} adhering to SafeTrade's chat style.
     *
     * @param player The player to send to
     * @param prefixType The type of prefix to send the text with
     * @param text The message
     */
    public static void sendMessageToPlayer(Player player, PrefixType prefixType, BaseComponent[] text) {
        player.spigot().sendMessage(Text.of(prefixType.getPrefix(), text));
    }

    /**
     * Sends a {@link BaseComponent} message to a {@link Player} adhering to SafeTrade's chat style.
     *
     * <p>If the {@link org.bukkit.command.CommandSender} is not a player, a prefix will not be sent.</p>
     *
     * @param src The source to send to
     * @param prefixType The type of prefix to send the text with
     * @param text The message
     */
    public static void sendMessageToCommandSource(CommandSender src, PrefixType prefixType, BaseComponent[] text) {
        if (src instanceof Player) {
            sendMessageToPlayer((Player)src, prefixType, text);
        }
        else {
            src.spigot().sendMessage(text);
        }
    }

    public static void sendMessageToAll(PrefixType prefixType, BaseComponent[] text) {
        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(Text.of(prefixType.getPrefix(), text)));
    }
}
