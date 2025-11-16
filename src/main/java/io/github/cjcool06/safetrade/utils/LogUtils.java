package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Log;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.obj.Trade;
import org.bukkit.Bukkit;
import org.spongepowered.api.data.key.Keys;
import org.bukkit.OfflinePlayer;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import io.github.cjcool06.safetrade.economy.currency.Currency;
import net.md_5.bungee.api.chat.BaseComponent;
import org.spongepowered.api.text.action.TextActions;
import net.md_5.bungee.api.ChatColor;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class LogUtils {

    private LogUtils() {}

    /**
     * Saves the {@link Log} of a {@link Trade} to the participants' log files.
     *
     * @param log The trade log
     */
    public static void saveLog(Log log) {
        Bukkit.getScheduler().runTaskAsynchronously(SafeTrade.getPlugin(), () -> {
            DataManager.addLog(log.getParticipant(), log);
            DataManager.addLog(log.getOtherParticipant(), log);
        });
    }

    /**
     * Creates and saves the {@link Log} of a {@link Trade} to the participants' log files.
     *
     * @param trade The trade
     */
    @Deprecated
    public static void logAndSave(Trade trade) {
        Log log = new Log(trade);
        Bukkit.getScheduler().runTaskAsynchronously(SafeTrade.getPlugin(), () -> {
            DataManager.addLog(log.getParticipant(), log);
            DataManager.addLog(log.getOtherParticipant(), log);
        });
    }

    /**
     * This method loops through one of the participants, as both participants will have the logs of their trades.
     * If for some reason one of the users has had their logs removed, you can swap the parameters around.
     *
     * @param participant0 - The first participant of the trade
     * @param participant1 - The second participant of the trade
     * @return - List of logs that had both participants
     */
    public static ArrayList<Log> getLogsOf(OfflinePlayer participant0, OfflinePlayer participant1) {
        ArrayList<Log> logs = new ArrayList<>();
        ArrayList<Log> logsParticipant0 = DataManager.getLogs(participant0);
        for (Log log : logsParticipant0) {
            if (log.getParticipant().getUniqueId().equals(participant1.getUniqueId()) || log.getOtherParticipant().getUniqueId().equals(participant1.getUniqueId())) {
                logs.add(log);
            }
        }

        return logs;
    }

    @Deprecated
    public static List<String> createContents(Trade trade) {
        List<String> contents = new ArrayList<>();
        Text[] extentedLogs = getExtendedLogs(trade);
        contents.add(TextSerializers.JSON.serialize(
                Text.builder().append(Text.of(ChatColor.LIGHT_PURPLE, "[" + Log.getFormatter().format(Utils.convertToUTC(LocalDateTime.now())) + " UTC] "))
                        .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Day/Month/Year Hour:Minute"))).build()));

        // Participant 0
        OfflinePlayer p0 = trade.getSides()[0].getOfflinePlayer().get();
        contents.add(TextSerializers.JSON.serialize(
                Text.builder().append(Text.of(ChatColor.AQUA, p0.getName()))
                        .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Click here to see " + p0.getName() + "'s extended log for this trade"))).build()));
        contents.add(TextSerializers.JSON.serialize(
                Text.of(ChatColor.GREEN, p0.getName() + "'s Extended Log ")));
        contents.add(TextSerializers.JSON.serialize(extentedLogs[0]));

        contents.add(TextSerializers.JSON.serialize(Text.of(ChatColor.DARK_AQUA, " & ")));

        // Participant 1
        OfflinePlayer p1 = trade.getSides()[1].getOfflinePlayer().get();
        contents.add(TextSerializers.JSON.serialize(
                Text.builder().append(Text.of(ChatColor.AQUA, p1.getName()))
                        .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Click here to see " + p1.getName() + "'s extended log for this trade"))).build()));
        contents.add(TextSerializers.JSON.serialize(
                Text.of(ChatColor.GREEN, p1.getName() + "'s Extended Log ")));
        contents.add(TextSerializers.JSON.serialize(extentedLogs[1]));


        return contents;
    }

    /**
     * Creates the in-depth log text.
     *
     * @param trade - Trade to log
     * @return - BaseComponent[] array corresponding to trade participant indexes. For example, texts[0] is for trade.participants[0]
     */
    @Deprecated
    private static Text[] getExtendedLogs(Trade trade) {
        Currency currency = SafeTrade.getEcoService().getDefaultCurrency();

        Text.Builder builder1 =  Text.builder();
        Text.Builder builder2 = Text.builder();

        Side side0 = trade.getSides()[0];
        Side side1 = trade.getSides()[1];

        // TODO: Hover over the money to see their balance: before -> after
        builder1.append(Text.of("Money: "))
                .color(ChatColor.DARK_AQUA)
                .append(Text.builder().append(Text.of(ChatColor.AQUA, side0.vault.account.getBalance(currency).intValue()))
                        .onHover(TextActions.showText(Text.of()))
                        .build())
                .build();
        builder2.append(Text.of("Money: "))
                .color(ChatColor.DARK_AQUA)
                .append(Text.builder().append(Text.of(ChatColor.AQUA, side1.vault.account.getBalance(currency).intValue())).build())
                .build();

        builder1.append(Text.of("\n" + "Pokemon:"))
                .color(ChatColor.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Pokemon:"))
                .color(ChatColor.DARK_AQUA)
                .build();
        for (Pokemon pixelmon : side0.vault.getAllPokemon()) {
            Text.Builder pokemonInfo = Text.builder();
            int count = 0;
            List<BaseComponent[]> loreTexts = Utils.getPokemonLore(pixelmon);
            for (BaseComponent[] text : loreTexts) {
                count++;
                pokemonInfo.append(text);
                if (count != loreTexts.size()) {
                    pokemonInfo.append(Text.of("\n"));
                }
            }
            builder1.append(Text.builder()
                    .append(Text.of(ChatColor.AQUA, "\n" + pixelmon.getSpecies().getLocalizedName() + (pixelmon.isEgg() && !Config.showEggStats ? " Egg" : "")))
                    .onHover(TextActions.showText(pokemonInfo.build()))
                    .build())
                    .build();
        }
        for (Pokemon pixelmon : side1.vault.getAllPokemon()) {
            Text.Builder pokemonInfo = Text.builder();
            int count = 0;
            List<BaseComponent[]> loreTexts = Utils.getPokemonLore(pixelmon);
            for (BaseComponent[] text : loreTexts) {
                count++;
                pokemonInfo.append(text);
                if (count != loreTexts.size()) {
                    pokemonInfo.append(Text.of("\n"));
                }
            }
            builder2.append(Text.builder()
                    .append(Text.of(ChatColor.AQUA, "\n" + pixelmon.getSpecies().getLocalizedName() + (pixelmon.isEgg() && !Config.showEggStats ? " Egg" : "")))
                    .onHover(TextActions.showText(pokemonInfo.build()))
                    .build())
                    .build();
        }

        // TODO: Show items stats: durability
        builder1.append(Text.of("\n" + "Items:"))
                .color(ChatColor.DARK_AQUA)
                .build();
        builder2.append(Text.of("\n" + "Items:"))
                .color(ChatColor.DARK_AQUA)
                .build();
        for (ReadWriteNBT snapshot : side0.vault.getAllItems()) {
            Text.Builder builder = Text.builder();
            snapshot.get(Keys.ITEM_ENCHANTMENTS).ifPresent(enchantments -> {
                enchantments.forEach(enchantment -> {
                    builder.append(Text.of(ChatColor.DARK_AQUA, "Enchantments: "));
                    builder.append(Text.of(ChatColor.AQUA, "\n", enchantment.getType(), " ", enchantment.getLevel()));
                });
            });
            builder1.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, snapshot.getQuantity() + "x ", ChatColor.AQUA, snapshot.getTranslation().get()))
                    .onHover(TextActions.showText(builder.build()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder1.append(Text.builder().append(Text.of("  ", ChatColor.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), ChatColor.GOLD, "]")).build()).build();
            }
        }
        for (ReadWriteNBT snapshot : side1.vault.getAllItems()) {
            Text.Builder builder = Text.builder();
            snapshot.get(Keys.ITEM_ENCHANTMENTS).ifPresent(enchantments -> {
                builder.append(Text.of(ChatColor.DARK_AQUA, "Enchantments: "));
                enchantments.forEach(enchantment -> {
                    builder.append(Text.of(ChatColor.AQUA, "\n", enchantment.getType(), " ", enchantment.getLevel()));
                });
            });
            builder2.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, snapshot.getQuantity() + "x ", ChatColor.AQUA, snapshot.getTranslation().get()))
                    .onHover(TextActions.showText(builder.build()))
                    .build()).build();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder2.append(Text.builder().append(Text.of("  ", ChatColor.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), ChatColor.GOLD, "]")).build()).build();
            }
        }

        return new Text[]{builder1.build(), builder2.build()};
    }

}
