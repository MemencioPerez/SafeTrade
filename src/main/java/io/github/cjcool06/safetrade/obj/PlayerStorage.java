package io.github.cjcool06.safetrade.obj;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;

import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.CommandType;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.utils.GsonUtils;
import io.github.cjcool06.safetrade.utils.Utils;
import net.minecraft.nbt.CompoundNBT;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

import io.github.cjcool06.safetrade.economy.transaction.ResultType;
import io.github.cjcool06.safetrade.economy.transaction.TransactionResult;

import java.util.*;

// TODO: Make PlayerStorage GUI that admins can edit

/**
 * A PlayerStorage represents a storage for a {@link OfflinePlayer} that holds
 * {@link ReadWriteNBT}s,
 * {@link Pokemon},
 * {@link CommandWrapper}s,
 * and {@link MoneyWrapper}s,
 * as well as some other smaller stuff.
 *
 * <p>PlayerStorage data persists across restarts.</p>
 */
public class PlayerStorage {
    private final UUID playerUUID;
    private final List<ReadWriteNBT> items = new ArrayList<>();
    private final List<Pokemon> pokemons = new ArrayList<>();
    private final List<CommandWrapper> commands = new ArrayList<>();
    private final List<MoneyWrapper> money = new ArrayList<>();

    private boolean needSaving = false;
    private boolean autoGive = true;

    public PlayerStorage(OfflinePlayer user) {
        this.playerUUID = user.getUniqueId();
    }

    private PlayerStorage(UUID uuid, boolean autoGive, List<ReadWriteNBT> items, List<Pokemon> pokemons, List<CommandWrapper> commands, List<MoneyWrapper> money) {
        this.playerUUID = uuid;
        this.autoGive = autoGive;
        this.items.addAll(items);
        this.pokemons.addAll(pokemons);
        this.commands.addAll(commands);
        this.money.addAll(money);
    }

    /**
     * Gets the {@link UUID} associated with the owner of the storage.
     *
     * @return The uuid
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Attempts to get the {@link Player} of this storage.
     *
     * @return An {@link Optional}
     */
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(playerUUID));
    }

    /**
     * Attempts to get the {@link OfflinePlayer} of this storage.
     *
     * @return An {@link Optional}
     */
    public Optional<OfflinePlayer> getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(playerUUID).hasPlayedBefore() ? Optional.of(Bukkit.getOfflinePlayer(playerUUID)) : Optional.empty();
    }

    /**
     * Gets whether the storage needs saving.
     *
     * @return True if needs saving
     */
    public boolean needSaving() {
        return needSaving;
    }

    /**
     * Gets whether auto give is enabled for this storage.
     *
     * @return True if enabled
     */
    public boolean isAutoGiveEnabled() {
        return autoGive;
    }

    /**
     * Sets whether auto give is enabled for this storage.
     *
     * @param autoGive True for enable, false for disable
     */
    public void setAutoGive(boolean autoGive) {
        this.autoGive = autoGive;
    }

    /**
     * Gets all {@link MoneyWrapper}s in this storage.
     *
     * @return An unmodifiable list of money wrappers
     */
    public List<MoneyWrapper> getMoney() {
        return Collections.unmodifiableList(money);
    }

    /**
     * Adds a {@link MoneyWrapper} to this storage.
     *
     * @param moneyWrapper The money wrapper
     */
    public void addMoney(MoneyWrapper moneyWrapper) {
        money.add(moneyWrapper);
        if (autoGive) {
            giveMoney();
        }
        needSaving = true;
    }

    /**
     * Adds {@link MoneyWrapper}s to this storage.
     *
     * @param moneyWrapper The money wrapper
     */
    public void addMoney(List<MoneyWrapper> moneyWrapper) {
        money.addAll(moneyWrapper);
        if (autoGive) {
            giveMoney();
        }
        needSaving = true;
    }

    /**
     * Removes a {@link MoneyWrapper} from this storage.
     *
     * @param moneyWrapper The money wrapper
     */
    public void removeMoney(MoneyWrapper moneyWrapper) {
        needSaving = true;
        money.remove(moneyWrapper);
    }

    /**
     * Clears all {@link MoneyWrapper}s from this storage.
     */
    public void clearMoney() {
        needSaving = true;
        money.clear();
    }

    /**
     * Give the money in this storage to the storage owner and remove it.
     *
     * If the deposit fails, the money will not be removed.
     *
     * @return The {@link MoneyWrapper}s that were successful
     */
    public List<MoneyWrapper> giveMoney() {
        List<MoneyWrapper> successes = new ArrayList<>();
        Iterator<MoneyWrapper> iter = money.iterator();

        while (iter.hasNext()) {
            MoneyWrapper wrapper = iter.next();

            TransactionResult result = wrapper.transferBalance(SafeTrade.getEcoService().getOrCreateAccount(getOfflinePlayer().get().getUniqueId()).get());

            if (result.getResult() == ResultType.SUCCESS) {
                successes.add(wrapper);
                iter.remove();
                needSaving = true;
            }
        }

        return successes;
    }

    /**
     * Gets all {@link ReadWriteNBT}s in this storage.
     *
     * @return An unmodifiable list of items
     */
    public List<ReadWriteNBT> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds an {@link ReadWriteNBT} to this storage.
     *
     * @param snapshot The item
     */
    public void addItem(ReadWriteNBT snapshot) {
        items.add(snapshot);
        if (autoGive) {
            giveItems();
        }
        needSaving = true;
    }

    /**
     * Adds {@link ReadWriteNBT}s to this storage.
     *
     * @param snapshot The items
     */
    public void addItems(List<ReadWriteNBT> snapshot) {
        items.addAll(snapshot);
        if (autoGive) {
            giveItems();
        }
        needSaving = true;
    }

    /**
     * Removes an {@link ReadWriteNBT} from this storage.
     *
     * @param snapshot The item
     */
    public void removeItem(ReadWriteNBT snapshot) {
        needSaving = true;
        items.remove(snapshot);
    }

    /**
     * Removes an {@link org.bukkit.inventory.ItemStack} from this storage.
     *
     * @param itemStack The item
     */
    public void removeItem(ItemStack itemStack) {
        if (itemStack != null) {
            Iterator<ReadWriteNBT> iter = items.iterator();
            while (iter.hasNext()) {
                ItemStack item = NBT.itemStackFromNBT(iter.next());
                if (item != null) {
                    if (item.equals(itemStack)) {
                        iter.remove();
                        needSaving = true;
                    }
                }
            }
        }
    }

    /**
     * Clears all items from this storage.
     */
    public void clearItems() {
        needSaving = true;
        items.clear();
    }

    /**
     * Give the items in this storage to the storage owner and remove them.
     *
     * <p>If the player's {@link Inventory} becomes full it will halt and return.</p>
     *
     * @return The {@link ReadWriteNBT}s that were successfully given
     */
    public List<ReadWriteNBT> giveItems() {
        List<ReadWriteNBT> successes = new ArrayList<>();
        Iterator<ReadWriteNBT> iter = items.iterator();

        if (!getPlayer().isPresent()) {
            return successes;
        }

        while (iter.hasNext()) {
            ReadWriteNBT snapshot = iter.next();

            if (Utils.giveItem(getPlayer().get(), snapshot)) {
                successes.add(snapshot);
                iter.remove();
                needSaving = true;
            }
            else {
                // There will most likely be no space for any of the items left, so no point continuing the iteration.
                break;
            }
        }

        return successes;
    }

    /**
     * Gets all {@link Pokemon}s in this storage.
     *
     * @return An unmodifiable list of pokemon
     */
    public List<Pokemon> getPokemons() {
        return Collections.unmodifiableList(pokemons);
    }

    /**
     * Adds a {@link Pokemon} to this storage.
     *
     * @param pokemon The pokemon
     */
    public void addPokemon(Pokemon pokemon) {
        pokemons.add(pokemon);
        if (autoGive) {
            givePokemon();
        }
        needSaving = true;
    }

    /**
     * Adds {@link Pokemon} to this storage.
     *
     * @param pokemon The pokemon
     */
    public void addPokemon(List<Pokemon> pokemon) {
        pokemons.addAll(pokemon);
        if (autoGive) {
            givePokemon();
        }
        needSaving = true;
    }

    /**
     * Removes a {@link Pokemon} from this storage.
     *
     * @param pokemon The pokemon
     */
    public void removePokemon(Pokemon pokemon) {
        needSaving = true;
        pokemons.remove(pokemon);
    }

    /**
     * Clears all pokemon from this storage.
     */
    public void clearPokemon() {
        needSaving = true;
        pokemons.clear();
    }

    /**
     * Puts as many of the {@link Pokemon} in this storage in to the player's {@link com.pixelmonmod.pixelmon.api.storage.PokemonStorage}
     * and removes those {@link Pokemon} from this storage.
     *
     * <p>If the player's {@link com.pixelmonmod.pixelmon.api.storage.PokemonStorage} becomes full it will halt and return.</p>
     *
     * @return A list of the {@link Pokemon} that were successfully added to the player's {@link com.pixelmonmod.pixelmon.api.storage.PokemonStorage}
     */
    public List<Pokemon> givePokemon() {
        List<Pokemon> successes = new ArrayList<>();
        Iterator<Pokemon> iter = pokemons.iterator();
        PlayerPartyStorage partyStorage = StorageProxy.getParty(playerUUID);

        while (iter.hasNext()) {
            Pokemon pokemon = iter.next();

            if (partyStorage.add(pokemon)) {
                successes.add(pokemon);
                iter.remove();
                needSaving = true;
            }
            else {
                // There are no more spaces in the player's pokemon storage, so no point continuing the iteration.
                break;
            }
        }

        return successes;
    }

    /**
     * Gets all {@link CommandWrapper}s in this storage.
     *
     * @return An unmodifiable list of command wrappers
     */
    public List<CommandWrapper> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * Adds a {@link CommandWrapper} to this storage.
     *
     * @param commandWrapper The command wrapper
     */
    public void addCommand(CommandWrapper commandWrapper) {
        commands.add(commandWrapper);
        if (autoGive) {
            executeCommands();
        }
        needSaving = true;
    }

    /**
     * Adds {@link CommandWrapper}s to this storage.
     *
     * @param commandWrapper The command wrapper
     */
    public void addCommands(List<CommandWrapper> commandWrapper) {
        commands.addAll(commandWrapper);
        if (autoGive) {
            executeCommands();
        }
        needSaving = true;
    }

    /**
     * Removes a {@link CommandWrapper} from this storage.
     *
     * @param commandWrapper The command wrapper
     */
    public void removeCommand(CommandWrapper commandWrapper) {
        needSaving = true;
        commands.remove(commandWrapper);
    }

    /**
     * Clears all {@link CommandWrapper}s from this storage.
     */
    public void clearCommands() {
        needSaving = true;
        commands.clear();
    }

    /**
     * Gets whether the storage is empty.
     *
     * @return True if empty
     */
    public boolean isEmpty() {
        return items.isEmpty() && pokemons.isEmpty() && commands.isEmpty() && money.isEmpty();
    }

    /**
     * Executes all {@link CommandWrapper}s.
     *
     * <p>If the wrapper requires an online player and one isn't present, it will skip over it.</p>
     *
     * @return The commands executed
     */
    public List<CommandWrapper> executeCommands() {
        List<CommandWrapper> commandsExecuted = new ArrayList<>();
        Iterator<CommandWrapper> iter = commands.iterator();

        while (iter.hasNext()) {
            CommandWrapper wrapper = iter.next();
            if (wrapper.commandType == CommandType.CONSOLE) {
                wrapper.consoleExecute();
                iter.remove();
                needSaving = true;
                commandsExecuted.add(wrapper);
            }
            else if (wrapper.commandType == CommandType.SUDO && getPlayer().isPresent()) {
                Player player = getPlayer().get();
                wrapper.sudoExecute(player);
                iter.remove();
                needSaving = true;
                commandsExecuted.add(wrapper);
            }
        }

        return commandsExecuted;
    }

    /**
     * Opens the {@link Inventory} representation of this storage.
     *
     * @param player The player to open the inventory for
     */
    public void open(Player player) {
        player.openInventory(getInventory());
    }

    /**
     * Gets an {@link Inventory} view of this storage.
     *
     * @return The inventory
     */
    public Inventory getInventory() {
        return InventoryHelper.buildAndGetStorageInventory(this);
    }

    /**
     * Save the storage to file.
     */
    public void save() {
        DataManager.savePlayerStorage(this);
        needSaving = false;
    }

    /**
     * Clears the entire storage.
     */
    public void clearAll() {
        clearCommands();
        clearItems();
        clearMoney();
        clearPokemon();
    }

    /**
     * Serialise the storage in to a container ({@link JsonObject}) to be saved to file.
     *
     * @param jsonObject The container
     */
    public void toContainer(JsonObject jsonObject) {
        JsonArray itemsArr = new JsonArray();
        JsonArray pokemonsArr = new JsonArray();
        JsonArray commandsArr = new JsonArray();
        JsonArray moneyArr = new JsonArray();

        for (CommandWrapper wrapper : commands) {
            JsonObject cmdObj = new JsonObject();
            wrapper.toContainer(cmdObj);
            commandsArr.add(cmdObj);
        }

        for (MoneyWrapper wrapper : money) {
            JsonObject moneyObj = new JsonObject();
            wrapper.toContainer(moneyObj);
            moneyArr.add(moneyObj);
        }

        for (Pokemon pokemon : pokemons) {
            pokemonsArr.add(GsonUtils.serialize(pokemon.writeToNBT(new CompoundNBT())));
        }

        for (ReadWriteNBT snapshot : items) {
            itemsArr.add(snapshot.toString());
        }

        jsonObject.add("PlayerUUID", new JsonPrimitive(playerUUID.toString()));
        jsonObject.add("AutoGive", new JsonPrimitive(autoGive));
        jsonObject.add("Money", moneyArr);
        jsonObject.add("Commands", commandsArr);
        jsonObject.add("Pokemon", pokemonsArr);
        jsonObject.add("Items", itemsArr);
    }

    /**
     * De-serialise the storage from a container ({@link JsonObject}).
     *
     * @param jsonObject The container
     * @return The de-serialised storage
     */
    public static PlayerStorage fromContainer(JsonObject jsonObject) {
        try {
            UUID playerUUID = UUID.fromString(jsonObject.get("PlayerUUID").getAsString());
            boolean autoGive = jsonObject.get("AutoGive").getAsBoolean();

            List<ReadWriteNBT> items = new ArrayList<>();
            List<Pokemon> pokemons = new ArrayList<>();
            List<CommandWrapper> commands = new ArrayList<>();
            List<MoneyWrapper> money = new ArrayList<>();

            for (JsonElement element : jsonObject.get("Commands").getAsJsonArray()) {
                CommandWrapper wrapper = CommandWrapper.fromContainer(element.getAsJsonObject());
                if (wrapper != null) {
                    commands.add(wrapper);
                }
            }

            for (JsonElement element : jsonObject.get("Money").getAsJsonArray()) {
                MoneyWrapper wrapper = MoneyWrapper.fromContainer(element.getAsJsonObject());
                if (wrapper != null) {
                    money.add(wrapper);
                }
            }

            for (JsonElement element : jsonObject.get("Pokemon").getAsJsonArray()) {
                pokemons.add(PokemonFactory.create(GsonUtils.deserialize(element.getAsString())));
            }

            for (JsonElement element : jsonObject.get("Items").getAsJsonArray()) {
                items.add(NBT.parseNBT(element.getAsString()));
            }

            return new PlayerStorage(playerUUID, autoGive, items, pokemons, commands, money);
        } catch (Exception e) {
            SafeTrade.getPluginLogger().warning("There was a problem deserialising a PlayerStorage from a container.");
            e.printStackTrace();
            return null;
        }
    }
}
