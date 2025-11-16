package io.github.cjcool06.safetrade.config;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.economy.currency.Currency;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class CurrenciesConfig {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode node;

    public static Set<Currency> currencies = new HashSet<>();

    public static void load() {
        File file = new File(SafeTrade.getPluginDataFolder(), "currencies.conf");
        try {
            loader = HoconConfigurationLoader.builder().file(file).build();
            node = loader.load();

            // If the config file doesn't exist, then presume the dir is missing also.
            if (!file.exists()) {
                Files.createDirectories(file.getParentFile().toPath());
                save();
            }
            else {
                List<Currency> currenciesList = node.node("Currencies").getList(TypeToken.get(Currency.class));
                currencies = currenciesList != null ? new HashSet<>(currenciesList) : new HashSet<>();

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
            node.node("Currencies").comment("List of custom currencies in the server.");
            node.node("Currencies").set(currencies);

            loader.save(node);
        } catch (Exception e) {
            SafeTrade.getPluginLogger().severe("Could not save config.");
        }
    }
}
