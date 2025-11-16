package io.github.cjcool06.safetrade.obj;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.utils.Text;
import io.github.cjcool06.safetrade.utils.GsonUtils;
import io.github.cjcool06.safetrade.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.OfflinePlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.bukkit.Bukkit;
import org.spongepowered.api.data.persistence.DataFormats;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.action.TextActions;
import net.md_5.bungee.api.ChatColor;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A Log represents the information saved by a {@link Trade} when it's successfully executed.
 *
 * This object is immutable.
 */
@ConfigSerializable
public class Log {
    @Setting
    private UUID uniqueID;
    @Setting
    private String timestamp;

    // Side
    @Setting
    private UUID participant;
    @Setting
    private List<String> sidePokemonStrings;
    @Setting
    private List<String> sideItemStrings;
    @Setting
    private List<String> sideMoneyStrings;

    // Other Side
    @Setting
    private UUID otherParticipant;
    @Setting
    private List<String> otherSidePokemonStrings;
    @Setting
    private List<String> otherSideItemStrings;
    @Setting
    private List<String> otherSideMoneyStrings;

    @Deprecated
    private List<String> jsonTexts;
    @Deprecated
    private Integer sideMoney;
    @Deprecated
    private Integer otherSideMoney;

    // Caches
    private List<Pokemon> sideCachedPokemon = null;
    private List<ReadWriteNBT> sideCachedItems = null;
    private List<MoneyWrapper> sideCachedMoney = null;

    private List<Pokemon> otherSideCachedPokemon = null;
    private List<ReadWriteNBT> otherSideCachedItems = null;
    private List<MoneyWrapper> otherSideCachedMoney = null;

    /**
     * Used for deserialisation purposes. Do NOT use this constructor
     */
    @Deprecated
    public Log() {}

    public Log(Trade trade) {
        uniqueID = UUID.randomUUID();
        timestamp = getFormatter().format(LocalDateTime.now().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());

        // Side
        Side side = trade.getSides()[0];
        participant = side.sideOwnerUUID;
        sidePokemonStrings = serialisePokemon(side.vault.getAllPokemon());
        sideItemStrings = serialiseItemSnapshots(side.vault.getAllItems());
        sideMoneyStrings = serialiseMoneyWrappers(side.vault.getAllMoney());

        // Other Side
        Side otherSide = trade.getSides()[1];
        otherParticipant = otherSide.sideOwnerUUID;
        otherSidePokemonStrings = serialisePokemon(otherSide.vault.getAllPokemon());
        otherSideItemStrings = serialiseItemSnapshots(otherSide.vault.getAllItems());
        otherSideMoneyStrings = serialiseMoneyWrappers(otherSide.vault.getAllMoney());
    }

    /**
     * Gets the {@link UUID} of the log.
     *
     * @return The unique id
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    /**
     * Gets the {@link LocalDateTime} of when the log was created.
     *
     * @return The timestamp
     */
    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp, getFormatter());
    }

    /**
     * Gets the first participant of the logged trade's {@link UUID}.
     *
     * @return The first participant's uuid
     */
    public UUID getParticipantUUID() {
        return participant;
    }

    /**
     * Gets the other participant of the logged trade's {@link UUID}.
     *
     * @return The other participant's uuid
     */
    public UUID getOtherParticipantUUID() {
        return otherParticipant;
    }

    /**
     * Gets the first participant of the logged trade as a {@link OfflinePlayer}.
     *
     * @return The first participant
     */
    public OfflinePlayer getParticipant() {
        return Utils.getOfflinePlayer(participant).get();
    }

    /**
     * Gets the other participant of the logged trade as a {@link OfflinePlayer}.
     *
     * @return The other participant
     */
    public OfflinePlayer getOtherParticipant() {
        return Utils.getOfflinePlayer(otherParticipant).get();
    }

    /**
     * Gets the first participant's logged {@link Pokemon}.
     *
     * @return The pokemon
     */
    public List<Pokemon> getSidesPokemon() {
        if (sideCachedPokemon != null) {
            return Collections.unmodifiableList(sideCachedPokemon);
        }

        List<Pokemon> pokemon = new ArrayList<>();
        for (String pokemonStr : sidePokemonStrings) {
            pokemon.add(Pixelmon.pokemonFactory.create(GsonUtils.deserialize(pokemonStr)));
        }

        sideCachedPokemon = pokemon;
        return pokemon;
    }

    /**
     * Gets the other participant's logged {@link Pokemon}.
     *
     * @return The pokemon
     */
    public List<Pokemon> getOtherSidesPokemon() {
        if (otherSideCachedPokemon != null) {
            return Collections.unmodifiableList(otherSideCachedPokemon);
        }
        List<Pokemon> pokemon = new ArrayList<>();
        for (String pokemonStr : otherSidePokemonStrings) {
            pokemon.add(Pixelmon.pokemonFactory.create(GsonUtils.deserialize(pokemonStr)));
        }

        otherSideCachedPokemon = pokemon;
        return pokemon;
    }

    /**
     * Gets the first participant's logged {@link ReadWriteNBT}s.
     *
     * @return The items
     */
    @SuppressWarnings("all")
    public List<ReadWriteNBT> getSidesItems() {
        if (sideCachedItems != null) {
            return Collections.unmodifiableList(sideCachedItems);
        }

        List<ReadWriteNBT> items = new ArrayList<>();
        for (String itemStr : sideItemStrings) {
            try {
                items.add(Sponge.getDataManager().deserialize(ReadWriteNBT.class, DataFormats.JSON.read(itemStr)).get());
            } catch (Exception e) {
                continue;
            }
        }

        sideCachedItems = items;
        return items;
    }

    /**
     * Gets the first participant's logged {@link ReadWriteNBT}s.
     *
     * @return The items
     */
    @SuppressWarnings("all")
    public List<ReadWriteNBT> getOtherSidesItems() {
        if (otherSideCachedItems != null) {
            return Collections.unmodifiableList(otherSideCachedItems);
        }

        List<ReadWriteNBT> items = new ArrayList<>();
        for (String itemStr : otherSideItemStrings) {
            try {
                items.add(Sponge.getDataManager().deserialize(ReadWriteNBT.class, DataFormats.JSON.read(itemStr)).get());
            } catch (Exception e) {
                continue;
            }
        }

        otherSideCachedItems = items;
        return items;
    }

    /**
     * Gets the first participant's logged money.
     *
     * @return The money
     */
    @Deprecated
    public Integer getSidesMoney() {
        return sideMoney;
    }

    /**
     * Gets the other participant's logged money.
     *
     * @return The money
     */
    @Deprecated
    public Integer getOtherSidesMoney() {
        return otherSideMoney;
    }

    /**
     * Gets the first participant's logged {@link MoneyWrapper}s.
     *
     * @return The money wrappers
     */
    public List<MoneyWrapper> getSidesMoneyWrappers() {
        if (sideCachedMoney != null) {
            return sideCachedMoney;
        }

        List<MoneyWrapper> moneyWrappers = new ArrayList<>();
        for (String wrapperStr : sideMoneyStrings) {
            JsonObject jsonObject = new JsonParser().parse(wrapperStr).getAsJsonObject();
            MoneyWrapper wrapper = MoneyWrapper.fromContainer(jsonObject);
            if (wrapper != null) {
                moneyWrappers.add(wrapper);
            }
        }

        sideCachedMoney = moneyWrappers;
        return moneyWrappers;
    }

    /**
     * Gets the other participant's logged {@link MoneyWrapper}s.
     *
     * @return The money wrappers
     */
    public List<MoneyWrapper> getOtherSidesMoneyWrappers() {
        if (otherSideCachedMoney != null) {
            return otherSideCachedMoney;
        }

        List<MoneyWrapper> moneyWrappers = new ArrayList<>();
        for (String wrapperStr : otherSideMoneyStrings) {
            JsonObject jsonObject = new JsonParser().parse(wrapperStr).getAsJsonObject();
            MoneyWrapper wrapper = MoneyWrapper.fromContainer(jsonObject);
            if (wrapper != null) {
                moneyWrappers.add(wrapper);
            }
        }

        otherSideCachedMoney = moneyWrappers;
        return moneyWrappers;
    }

    /**
     * Gets the {@link Inventory} of this log.
     *
     * @return The inventory
     */
    public Inventory getInventory() {
        return InventoryHelper.buildAndGetLogInventory(this);
    }

    /**
     * Gets the texts that were saved in json.
     *
     * @return A list of the texts as strings
     */
    @Deprecated
    public List<String> getJsonTexts() {
        return new ArrayList<>(jsonTexts);
    }

    /**
     * Builds the log {@link net.md_5.bungee.api.chat.BaseComponent} from the json texts.
     *
     * @return The log text
     */
    @Deprecated
    public BaseComponent[] getText() {
        Text.Builder builder = Text.builder();
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(0)));
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(1)).toBuilder()
                .onClick(TextActions.executeCallback(src -> {
                    PaginationList.builder()
                            .title(TextSerializers.JSON.deserialize(jsonTexts.get(2)))
                            .contents(TextSerializers.JSON.deserialize(jsonTexts.get(3)))
                            .padding(Text.of(ChatColor.GRAY, "-", ChatColor.RESET))
                            .sendTo(src);
                }))
                .build());
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(4)));
        builder.append(TextSerializers.JSON.deserialize(jsonTexts.get(5)).toBuilder()
                .onClick(TextActions.executeCallback(src -> {
                    PaginationList.builder()
                            .title(TextSerializers.JSON.deserialize(jsonTexts.get(6)))
                            .contents(TextSerializers.JSON.deserialize(jsonTexts.get(7)))
                            .padding(Text.of(ChatColor.GRAY, "-", ChatColor.RESET))
                            .sendTo(src);
                }))
                .build());

        return builder.build();
    }

    public BaseComponent[] getDisplayText() {
        return Text.builder()
                .append(Text.of(ChatColor.AQUA, getParticipant().getName(), ChatColor.DARK_AQUA, " & ", Text.of(ChatColor.AQUA, getOtherParticipant().getName())))
                .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Click here to see this trade's extended log")))
                .onClick(TextActions.executeCallback(src -> {
                    Player player = (Player)src;
                    Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getInventory()));
                }))
                .build();
    }

    public BaseComponent[] getDisplayTextWithDate() {
        return Text.builder()
                .append(Text.builder().append(Text.of(ChatColor.LIGHT_PURPLE, "[" + Log.getFormatter().format(Utils.convertToUTC(LocalDateTime.now())) + " UTC] "))
                        .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Day/Month/Year Hour:Minute"))).build())
                .append(Text.of(ChatColor.AQUA, getParticipant().getName(), ChatColor.DARK_AQUA, " & ", Text.of(ChatColor.AQUA, getOtherParticipant().getName())))
                .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Click here to see this trade's extended log")))
                .onClick(TextActions.executeCallback(src -> {
                    Player player = (Player)src;
                    Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getInventory()));
                }))
                .build();
    }

    /**
     * Gets the {@link DateTimeFormatter} used for the timestamp.
     *
     * @return The format
     */
    public static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern("[dd/MM/yyyy HH:mm]");
    }

    @SuppressWarnings("all")
    private List<String> serialiseItemSnapshots(List<ReadWriteNBT> items) {
        List<String> itemStrings = new ArrayList<>();
        for (ReadWriteNBT snapshot : items) {
            try {
                itemStrings.add(DataFormats.JSON.write(snapshot.toContainer()));
            } catch (IOException ioe) {
                continue;
            }
        }

        return itemStrings;
    }

    private List<String> serialisePokemon(List<Pokemon> pokemons) {
        List<String> pokemonStrings = new ArrayList<>();
        for (Pokemon pokemon : pokemons) {
            pokemonStrings.add(GsonUtils.serialize(pokemon.writeToNBT(new NBTTagCompound())));
        }

        return pokemonStrings;
    }

    private List<String> serialiseMoneyWrappers(List<MoneyWrapper> moneyWrappers) {
        List<String> moneyStrings = new ArrayList<>();
        for (MoneyWrapper wrapper : moneyWrappers) {
            JsonObject jsonObject = new JsonObject();
            wrapper.toContainer(jsonObject);
            moneyStrings.add(jsonObject.toString());
        }

        return moneyStrings;
    }
}
