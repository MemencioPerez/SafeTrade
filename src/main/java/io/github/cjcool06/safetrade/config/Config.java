package io.github.cjcool06.safetrade.config;

import io.github.cjcool06.safetrade.SafeTrade;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Config {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode node;

    public static boolean showEggStats = false;
    public static boolean showEggName = true;

    public static boolean gcLogsEnabled = true;
    public static int gcLogsExpiryTime = 31;

    public static boolean gcStoragesEnabled = true;

    public static boolean asyncStoragesEnabled = true;
    public static int asyncStoragesInterval = 1;

    public static List<String> blacklistedCurrencies = new ArrayList<>();
    public static List<String> blacklistedPokemon = new ArrayList<>();
    public static List<String> blacklistedItems = new ArrayList<>();

    public static void load() {
        File file = new File(SafeTrade.getPluginDataFolder(), "safetrade.conf");
        try {
            loader = HoconConfigurationLoader.builder().file(file).build();
            node = loader.load();

            // If the config file doesn't exist, then presume the dir is missing also.
            if (!file.exists()) {
                Files.createDirectories(file.getParentFile().toPath());
                save();
            }
            else {
                showEggStats = node.node("ShowEggStats").getBoolean(false);
                showEggName = node.node("ShowEggName").getBoolean(true);
                gcLogsEnabled = node.node("GarbageCollector", "Logs", "Enabled").getBoolean(true);
                gcLogsExpiryTime = node.node("GarbageCollector", "Logs", "ExpiryTime").getInt(31);
                gcStoragesEnabled = node.node("GarbageCollector", "Storages").getBoolean(true);
                asyncStoragesEnabled = node.node("AsyncSaving", "Storages", "Enabled").getBoolean(true);
                asyncStoragesInterval = node.node("AsyncSaving", "Storages", "Interval").getInt(1);

                blacklistedCurrencies = node.node("Blacklists", "Currencies").childrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
                blacklistedItems = node.node("Blacklists", "Items").childrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
                blacklistedPokemon = node.node("Blacklists", "Pokemon").childrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());

                // This will load all values that are being using into the config at runtime, as well as
                // ensure old configs will have any new nodes that are added.
                save();
            }
        } catch (Exception e) {
            SafeTrade.getPluginLogger().severe("Could not load config.");
        }
    }

    public static void save() {
        try {
            node.node("ShowEggStats").comment("Show the stats of the Pokemon inside the egg.");
            node.node("ShowEggStats").set(showEggStats);

            node.node("ShowEggName").comment("Show the name of the Pokemon inside the egg.");
            node.node("ShowEggName").set(showEggName);

            node.node("GarbageCollector").comment("The GC improves the efficiency of SafeTrade." +
                    "\n" +
                    "\nRefer to the wiki if you don't know what you're doing.");

            node.node("GarbageCollector", "Logs").comment("Quicken log checkups by deleting old logs.");

            node.node("GarbageCollector", "Logs", "Enabled").comment("Enables the GC to handle logs.");
            node.node("GarbageCollector", "Logs", "Enabled").set(gcLogsEnabled);

            node.node("GarbageCollector", "Logs", "ExpiryTime").comment("The age a log must be to be deleted, in days.");
            node.node("GarbageCollector", "Logs", "ExpiryTime").set(gcLogsExpiryTime);

            node.node("GarbageCollector", "Storages").comment("Quickens storage saving & loading by deleting empty storage files.");

            node.node("GarbageCollector", "Storages", "Enabled").comment("Enables the GC to handle storages.");
            node.node("GarbageCollector", "Storages", "Enabled").set(gcStoragesEnabled);

            node.node("AsyncSaving").comment("Asynchronous saving improves the efficiency of SafeTrade." +
                    "\n" +
                    "\nRefer to the wiki if you don't know what you're doing.");

            node.node("AsyncSaving", "Storages").comment("Quickens shutdown saving and prevents loss of data in case of crash.");

            node.node("AsyncSaving", "Storages", "Enabled").comment("Enables asynchronous storage saving.");
            node.node("AsyncSaving", "Storages", "Enabled").set(asyncStoragesEnabled);

            node.node("AsyncSaving", "Storages", "Interval").comment("The interval of asynchronous storage saving, in hours.");
            node.node("AsyncSaving", "Storages", "Interval").set(asyncStoragesInterval);

            node.node("Blacklists").set("Prevents players from trading certain things.");

            node.node("Blacklists", "Currencies").comment("Prevents players from trading certain currencies. " +
                    "\nUse the currency ID. Eg. \"economylite:coin\" (notice the quotations)");
            node.node("Blacklists", "Currencies").set(blacklistedCurrencies);

            node.node("Blacklists", "Items").comment("Prevents players from trading certain items." +
                    "\nUse the item ID. Eg. \"minecraft:paper\" (notice the quotations)");
            node.node("Blacklists", "Items").set(blacklistedItems);

            node.node("Blacklists", "Pokemon").comment("Prevents players from trading certain pokemon." +
                    "\nUse pokemon species. Eg. magikarp (notice the lack of quotations");
            node.node("Blacklists", "Pokemon").set(blacklistedPokemon);

            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getPluginLogger().severe("Could not save config.");
        }
    }
}
