package io.github.cjcool06.safetrade.helpers;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.connection.ConnectionLeftEvent;
import io.github.cjcool06.safetrade.api.events.trade.connection.ConnectionPostJoinEvent;
import io.github.cjcool06.safetrade.api.events.trade.connection.ConnectionPreJoinEvent;
import io.github.cjcool06.safetrade.api.events.trade.inventory.InventoryPostChangeEvent;
import io.github.cjcool06.safetrade.api.events.trade.viewer.ViewerEvent;
import io.github.cjcool06.safetrade.economy.service.EconomyService;
import io.github.cjcool06.safetrade.obj.*;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import io.github.cjcool06.safetrade.economy.currency.Currency;
import io.github.cjcool06.safetrade.economy.transaction.ResultType;
import io.github.cjcool06.safetrade.economy.transaction.TransactionResult;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.ChatColor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/**
 * This class is to simply prevent these methods from unnecessarily cluttering up other classes
 */
public class InventoryHelper {

    //
    // Cooldowns
    //
    // A cooldown lasts 5 ticks (0.25 seconds)
    //

    private static List<UUID> clickingMainInv = new ArrayList<>();

    public static boolean hasCooldown(UUID uuid) {
        return clickingMainInv.contains(uuid);
    }

    public static void addCooldown(UUID uuid) {
        clickingMainInv.add(uuid);
        Bukkit.getScheduler().runTaskLater(SafeTrade.getPlugin(), () -> clickingMainInv.remove(uuid), 5L);
    }

    //
    // Default open handler for main trade inventories
    //
    // Default close handler for main trade inventories
    //

    public static void handleOpen(Trade trade, InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Optional<Side> optSide = trade.getSide(player.getUniqueId());
        // The player is a participant of a side of the trade.
        if (optSide.isPresent()) {
            Side side = optSide.get();
            // If the side is paused, it means that they are reconnecting to the trade.
            // If the side is not paused, it means they have been transferred from another trade-related inventory and is not considered as a trade connection
            if (side.isPaused()) {
                if (BukkitEventManagerUtil.post(new ConnectionPreJoinEvent(side))) {
                    event.setCancelled(true);
                    return;
                }
                side.setPaused(false);
                Bukkit.getPluginManager().callEvent(new ConnectionPostJoinEvent(side));
                Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), trade::reformatInventory);
            }
            else {
                Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> Bukkit.getPluginManager().callEvent(new InventoryPostChangeEvent(side)));
            }
        }
        // An unauthorized player is attempting to open the trade unexpectedly.
        // Adds them to the trade's viewers.
        // This will happen if the player doesn't come through Trade#addViewer
        else if (!trade.getViewers().contains(player)) {
            if (BukkitEventManagerUtil.post(new ViewerEvent.Add.Pre(trade, player))) {
                event.setCancelled(true);
                return;
            }
            trade.addViewer(player, false);
            Bukkit.getPluginManager().callEvent(new ViewerEvent.Add.Post(trade, player));
        }
    }

    public static void handleBasicClose(Trade trade, InventoryType inventoryType, InteractInventoryEvent.Close event) {
        event.getCause().first(Player.class).ifPresent(player -> {
            Optional<Side> optSide = trade.getSide(player.getUniqueId());
            // The player is a participant of a side of the trade.
            if (optSide.isPresent()) {
                Side side = optSide.get();
                // Side#changeInventory changes the current inventory
                // Therefore if the current inventory is for this inventory, the player is exiting the trade and not changing inventories
                // This will not work correctly if Player#changeInventory is used instead of Side#changeInventory
                // For example, if currentInventory equals MAIN and this inventoryType is NOT MAIN, the player is accessing the other side's inventories
                // This is also true if the InventoryType is NONE, which is the proper way to close a trade inventory.
                if (side.currentInventory.equals(inventoryType) || side.currentInventory.equals(InventoryType.NONE)) {
                    side.setReady(false);
                    side.setPaused(true);
                    side.currentInventory = InventoryType.NONE;
                    Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), side.parentTrade::reformatInventory);
                    Bukkit.getPluginManager().callEvent(new ConnectionLeftEvent(side));
                }
            }
            // Else, the player must be a viewer
            else {
                Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
                    // If the viewer is not viewing an inventory after 1 tick, they have exited the trade.
                    // I have yet to test what happens if a viewer is FORCED to open an unrelated inventory. If I'd have to guess, this code block would not be executed
                    // as the player will have the unrelated inventory open, therefore the player will continue to be considered as a viewer.
                    // Just something to keep in mind.
                    if (!player.isViewingInventory()) {
                        if (Bukkit.getPluginManager().callEvent(new ViewerEvent.Remove.Pre(trade, player))) {
                            if (player.isOnline()) {
                                trade.getSides()[0].changeInventoryForViewer(player, inventoryType);
                            }
                        }
                        else {
                            trade.removeViewer(player, false);
                            Bukkit.getPluginManager().callEvent(new ViewerEvent.Remove.Post(trade, player));
                        }
                    }
                });
            }
        });
    }

    /** Main {@link Trade} inventories: */

    //
    //  INVENTORIES
    //
    //  - Money (Main)
    //
    //
    //  CLICKERS
    //
    //  - Money (Main)
    //

    private static Map<UUID, Currency> currentCurrency = new HashMap<>();

    public static Inventory buildAndGetMoneyInventory(Side side) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, side.getOfflinePlayer().get().getName() + "'s Money")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,3))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleMoneyClick(side, event))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(side.parentTrade, InventoryType.MONEY, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        updateMoneyInventory(inventory, side);

        return inventory;
    }

    private static void updateMoneyInventory(Inventory inventory, Side side) {
        Currency currency = currentCurrency.getOrDefault(side.sideOwnerUUID, SafeTrade.getEcoService().getDefaultCurrency());
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i == 1) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 1));
            }
            else if (i == 2) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 10));
            }
            else if (i == 3) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 100));
            }
            else if (i == 4) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 1000));
            }
            else if (i == 5) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 10000));
            }
            else if (i == 6) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 100000));
            }
            else if (i == 7) {
                slot.set(ItemUtils.Money.getMoneyBars(currency, 1000000));
            }
            else if (i == 18) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i == 21) {
                slot.set(ItemUtils.Money.getTotalMoney(side));
            }
            else if (i == 23) {
                slot.set(ItemUtils.Money.getPlayersMoney(side, currency));
            }
            else if (i == 26) {
                slot.set(ItemUtils.Money.changeCurrency());
            }
            else if (i <= 26) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handleMoneyClick(Side side, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
                            side.changeInventoryForViewer(player, side.parentTrade.getState() == TradeState.WAITING_FOR_CONFIRMATION ? InventoryType.OVERVIEW : InventoryType.MAIN);
                            currentCurrency.remove(side.sideOwnerUUID);
                        });
                    }
                    // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                    // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's money inventory
                    else if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && (event instanceof ClickInventoryEvent.Primary || event instanceof ClickInventoryEvent.Secondary))
                            && side.parentTrade.getState() == TradeState.TRADING) {

                        if (item.equalTo(ItemUtils.Money.changeCurrency())) {
                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(buildAndGetCurrenciesInventory(side, SafeTrade.getEcoService(), event.getTargetInventory())));
                            return;
                        }

                        Currency currency = currentCurrency.getOrDefault(side.sideOwnerUUID, SafeTrade.getEcoService().getDefaultCurrency());

                        for (int i = 1; i <= 1000000; i *= 10) {

                            if (item.equalTo(ItemUtils.Money.getMoneyBars(currency, i))) {
                                // Left clicking = adding money to trade
                                if (event instanceof ClickInventoryEvent.Primary) {
                                    TransactionResult result = SafeTrade.getEcoService().getOrCreateAccount(side.getOfflinePlayer().get().getUniqueId()).get()
                                            .transfer(side.vault.account, currency, BigDecimal.valueOf(i));

                                    if (result.getResult() == ResultType.SUCCESS) {
                                        side.sendMessage(Text.of(ChatColor.GREEN, "Added " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " to the trade."));
                                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updateMoneyInventory(event.getTargetInventory(), side));
                                        side.parentTrade.reformatInventory();
                                    } else {
                                        side.sendMessage(Text.of(ChatColor.RED, "You do not have enough ", currency.getPluralDisplayName(), "."));
                                    }
                                }
                                // Right clicking = removing money from trade
                                else {
                                    if (i > side.vault.account.getBalance(currency).intValue()) {
                                        int val = side.vault.account.getBalance(currency).intValue();
                                        side.vault.account.transfer(SafeTrade.getEcoService().getOrCreateAccount(side.getOfflinePlayer().get().getUniqueId()).get(), currency, side.vault.account.getBalance(currency));
                                        side.sendMessage(Text.of(ChatColor.GREEN, "Removed " + NumberFormat.getNumberInstance(Locale.US).format(val) + " ", currency.getPluralDisplayName(), " from the trade."));
                                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updateMoneyInventory(event.getTargetInventory(), side));
                                        side.parentTrade.reformatInventory();
                                    }
                                    else {
                                        TransactionResult result = side.vault.account.transfer(SafeTrade.getEcoService().getOrCreateAccount(side.getOfflinePlayer().get().getUniqueId()).get(), currency, BigDecimal.valueOf(i));

                                        if (result.getResult() == ResultType.SUCCESS) {
                                            side.sendMessage(Text.of(ChatColor.GREEN, "Removed " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " from the trade."));
                                            // Refreshes the total money and player money item
                                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updateMoneyInventory(event.getTargetInventory(), side));
                                            side.parentTrade.reformatInventory();
                                        } else {
                                            side.sendMessage(Text.of(ChatColor.RED, "There was an error removing " + NumberFormat.getNumberInstance(Locale.US).format(i) + " ", currency.getPluralDisplayName(), " from the trade."));
                                            side.sendMessage(Text.of(ChatColor.RED, "Contact an administrator if this continues."));
                                        }
                                    }
                                }

                                break;
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Currencies
    //
    //
    //  CLICKERS
    //
    //  - Currencies
    //

    public static Inventory buildAndGetCurrenciesInventory(Side side, EconomyService economyService, Inventory parentMoneyInventory) {
        Set<Currency> currencies = economyService.getCurrencies();

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, "Currencies")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,(currencies.size() / 9) + 1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleCurrenciesClick(side, event, parentMoneyInventory))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(side.parentTrade, InventoryType.MONEY, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        updateCurrenciesInventory(side, inventory, currencies);

        return inventory;
    }

    private static void updateCurrenciesInventory(Side side, Inventory inventory, Set<Currency> currencies) {
        List<Currency> nonBlacklistedCurrencies = new ArrayList<>();
        Iterator<Currency> iter;

        // Removes blacklisted currencies from currency inventory if player is not admin
        if (!side.getPlayer().get().hasPermission("safetrade.admin.blacklist.bypass.currency")) {
            for (Currency c : currencies) {
                if (!BlacklistUtils.isBlacklisted(c)) {
                    nonBlacklistedCurrencies.add(c);
                }
            }

            iter = nonBlacklistedCurrencies.iterator();
        }
        // Else use all currencies
        else {
            iter = currencies.iterator();
        }

        inventory.slots().forEach(slot -> {
            if (iter.hasNext()) {
                slot.set(ItemUtils.Money.getCurrency(iter.next()));
            }
        });
    }

    private static void handleCurrenciesClick(Side side, ClickInventoryEvent event, Inventory parentMoneyInventory) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                ItemStack item = transaction.getOriginal().createStack();

                // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's money inventory
                if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && (event instanceof ClickInventoryEvent.Primary || event instanceof ClickInventoryEvent.Secondary))
                        && side.parentTrade.getState() == TradeState.TRADING) {

                    for (Currency currency : SafeTrade.getEcoService().getCurrencies()) {
                        if (item.equalTo(ItemUtils.Money.getCurrency(currency))) {
                            currentCurrency.put(side.sideOwnerUUID, currency);

                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
                                player.openInventory(buildAndGetMoneyInventory(side));
                            });
                        }
                    }
                }
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - PC
    //
    //
    //  CLICKERS
    //
    //  - PC
    //

    private static HashMap<Side, Integer> currentPage = new HashMap<>();

    public static Inventory buildAndGetPCInventory(Side side) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, "PC")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handlePCClick(side, event))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(side.parentTrade, InventoryType.PC, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(side.parentTrade, event))
                .build(SafeTrade.getPlugin());

        currentPage.put(side, 1);
        updatePC(inventory, side);

        return inventory;
    }

    private static void updatePC(Inventory inventory, Side side) {
        LinkedHashMap<ItemStack, Pokemon>[] pcArr = Utils.generatePCMaps(side);
        LinkedHashMap<ItemStack, Pokemon> partyMap = pcArr[0];
        LinkedHashMap<ItemStack, Pokemon> pcMap = pcArr[1];
        Iterator<ItemStack> partyIter = partyMap.keySet().iterator();
        Iterator<ItemStack> pcIter = pcMap.keySet().iterator();
        int page = currentPage.get(side);

        for (int j = 0; j < (page-1)*30 && pcIter.hasNext(); j++) {
            pcIter.next();
        }

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if ((i >= 3 && i <= 8) || (i >= 12 && i <= 17) || (i >= 21 && i <= 26) || (i >= 30 && i <= 35) || (i >= 39 && i <= 44)) {
                slot.set(pcIter.hasNext() ? pcIter.next() : ItemStack.empty());
            }
            else if (i == 9 || i == 10 || i == 18 || i == 19 || i == 27 || i == 28) {
                slot.set(partyIter.hasNext() ? partyIter.next() : ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i == 48) {
                slot.set(ItemUtils.PC.getPreviousPage(page));
            }
            else if (i == 50) {
                slot.set(ItemUtils.PC.getNextPage(page));
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handlePCClick(Side side, ClickInventoryEvent event) {
        LinkedHashMap<ItemStack, Pokemon>[] pcArr = Utils.generatePCMaps(side);
        LinkedHashMap<ItemStack, Pokemon> partyMap = pcArr[0];
        LinkedHashMap<ItemStack, Pokemon> pcMap = pcArr[1];
        int page = currentPage.get(side);

        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> side.changeInventoryForViewer(player, InventoryType.POKEMON));
                    }
                    // If player is not the in the side of this vault, or the if the vault is locked, or they didn't left/right click, nothing will happen.
                    // Checking for player is redundant when going through the main trade inventory, but I'll keep it here in-case a player somehow opens another side's pc inventory
                    else if ((player.getUniqueId().equals(side.sideOwnerUUID) && !side.vault.isLocked() && event instanceof ClickInventoryEvent.Primary)
                            && side.parentTrade.getState() == TradeState.TRADING) {
                        boolean continueChecks = true;
                        PlayerPartyStorage partyStorage = StorageProxy.getParty(side.getOfflinePlayer().get().getUniqueId());
                        PCStorage pcStorage = StorageProxy.getPCForPlayer(side.getOfflinePlayer().get().getUniqueId());

                        for (ItemStack itemStack : partyMap.keySet()) {
                            if (itemStack.equalTo(item)) {
                                Pokemon pokemon = partyMap.get(itemStack);

                                if (Utils.untradeable.isDataMatch(pokemon) || partyStorage.countPokemon() <= 1 || pokemon.isInRanch()) {
                                    return;
                                }
                                // Prevents players trading blacklisted pokemon
                                if (BlacklistUtils.isBlacklisted(pokemon) && !player.hasPermission("safetrade.admin.blacklist.bypass.pokemon")) {
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.DARK_RED, pokemon.getSpecies().name, ChatColor.RED, " is not allowed to be traded."));
                                    return;
                                }
                                if (Utils.getAllPokemon(partyStorage).contains(pokemon)) {
                                    if (side.vault.addPokemon(pokemon)) {
                                        partyStorage.set(partyStorage.getPosition(pokemon), null);
                                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updatePC(event.getTargetInventory(), side));
                                    }
                                }
                                continueChecks = false;
                                break;
                            }
                        }
                        for (ItemStack itemStack : pcMap.keySet()) {
                            if (!continueChecks) {
                                break;
                            }
                            if (itemStack.equalTo(item)) {
                                Pokemon pokemon = pcMap.get(itemStack);
                                if (Utils.untradeable.isDataMatch(pokemon) || pokemon.isInRanch()) {
                                    return;
                                }
                                // Prevents players trading blacklisted pokemon
                                if (BlacklistUtils.isBlacklisted(pokemon) && !player.hasPermission("safetrade.admin.blacklist.bypass.pokemon")) {
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, Text.of(ChatColor.DARK_RED, pokemon.getSpecies().name, ChatColor.RED, " is not allowed to be traded."));
                                    return;
                                }
                                List<Pokemon> pcPokemon = Utils.getAllPokemon(pcStorage);
                                if (pcPokemon.contains(pokemon)) {
                                    if (side.vault.addPokemon(pokemon)) {
                                        pcStorage.set(pcStorage.getPosition(pokemon), null);
                                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updatePC(event.getTargetInventory(), side));
                                    }
                                }
                                continueChecks = false;
                                break;
                            }
                        }

                        if (continueChecks) {
                            if (item.equalTo(ItemUtils.PC.getNextPage(page))) {
                                if (page*30 >= pcMap.size()) {
                                    currentPage.put(side, 1);
                                }
                                else {
                                    currentPage.put(side, page+1);
                                }
                                Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updatePC(event.getTargetInventory(), side));
                            }
                            else if (item.equalTo(ItemUtils.PC.getPreviousPage(page))) {
                                if (page > 1) {
                                    currentPage.put(side, page-1);
                                }
                                else {
                                    int num = pcMap.size() / 30;
                                    currentPage.put(side, num+1);
                                }
                                Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updatePC(event.getTargetInventory(), side));
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Overview
    //
    //
    //  CLICKERS
    //
    //  - Overview
    //

    public static Inventory buildAndGetOverviewInventory(Trade trade) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, "Trade Overview")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,3))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleOverviewClick(trade, event))
                .listener(InteractInventoryEvent.Close.class, event -> handleBasicClose(trade, InventoryType.MONEY, event))
                .listener(InteractInventoryEvent.Open.class, event -> handleOpen(trade, event))
                .build(SafeTrade.getPlugin());

        updateOverview(inventory, trade);

        return inventory;
    }

    private static void updateOverview(Inventory inventory, Trade trade) {
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            // Side 1
            if ((i >= 0 && i <= 3) || (i >= 18 && i <= 21)) {
                slot.set(ItemUtils.Overview.getConfirmationStatus(trade.getSides()[0]));
            }
            else if (i == 9) {
                slot.set(ItemUtils.Money.getTotalMoney(trade.getSides()[0]));
            }
            else if (i == 10) {
                slot.set(ItemUtils.Main.getItemStorage(trade.getSides()[0]));
            }
            else if (i == 11) {
                slot.set(ItemUtils.Main.getPokemonStorage(trade.getSides()[0]));
            }
            else if (i == 12) {
                slot.set(ItemUtils.Main.getHead(trade.getSides()[0]));
            }

            // Side 2
            else if ((i >= 5 && i <= 8) || (i >= 23 && i <= 26)) {
                slot.set(ItemUtils.Overview.getConfirmationStatus(trade.getSides()[1]));
            }
            else if (i == 14) {
                slot.set(ItemUtils.Main.getHead(trade.getSides()[1]));
            }
            else if (i == 15) {
                slot.set(ItemUtils.Main.getPokemonStorage(trade.getSides()[1]));
            }
            else if (i == 16) {
                slot.set(ItemUtils.Main.getItemStorage(trade.getSides()[1]));
            }
            else if (i == 17) {
                slot.set(ItemUtils.Money.getTotalMoney(trade.getSides()[1]));
            }

            // Other
            else if (i == 4) {
                slot.set(ItemUtils.Overview.getConfirm());
            }
            else if (i == 13) {
                slot.set(ItemUtils.Overview.getOverviewInfo());
            }
            else if (i == 22) {
                slot.set(ItemUtils.Overview.getCancel());
            }
        });
    }

    private static void handleOverviewClick(Trade trade, ClickInventoryEvent event) {
        event.setCancelled(true);
        if (trade.getState() !=  TradeState.WAITING_FOR_CONFIRMATION) {
            return;
        }
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    trade.getSide(player.getUniqueId()).ifPresent(side -> {
                        Side otherSide = side.getOtherSide();

                        if (item.equalTo(ItemUtils.Overview.getConfirm())) {
                            side.setConfirmed(true);

                            if (otherSide.isConfirmed()) {
                                trade.executeTrade();
                            }
                            else {
                                Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updateOverview(event.getTargetInventory(), trade));
                            }
                        }
                        else if (item.equalTo(ItemUtils.Overview.getCancel())) {
                            side.setConfirmed(false);
                            side.setReady(false);
                            side.vault.setLocked(false);
                            otherSide.setConfirmed(false);
                            otherSide.setReady(false);
                            otherSide.vault.setLocked(false);
                            trade.setState(TradeState.TRADING);

                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), trade::reformatInventory);
                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> side.changeInventory(InventoryType.MAIN));
                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> otherSide.changeInventory(InventoryType.MAIN));
                        }
                        else if (item.equalTo(ItemUtils.Main.getItemStorage(side))) {
                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> side.changeInventoryForViewer(player, InventoryType.ITEM));
                        }
                        else if (item.equalTo(ItemUtils.Main.getPokemonStorage(side))) {
                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> side.changeInventoryForViewer(player, InventoryType.POKEMON));
                        }
                        else if (item.equalTo(ItemUtils.Main.getItemStorage(otherSide))) {
                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> otherSide.changeInventoryForViewer(player, InventoryType.ITEM));
                        }
                        else if (item.equalTo(ItemUtils.Main.getPokemonStorage(otherSide))) {
                            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> otherSide.changeInventoryForViewer(player, InventoryType.POKEMON));
                        }
                    });
                });
            });
        });
    }

    /** {@link Log} inventory shit */

    //
    //  INVENTORIES
    //
    //  - Log (Main)
    //
    //
    //  CLICKERS
    //
    //  - Log (Main)
    //

    public static Inventory buildAndGetLogInventory(Log log) {
        OfflinePlayer user = log.getParticipant();
        OfflinePlayer otherOfflinePlayer = log.getOtherParticipant();

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, user.getName() + " & " + otherUser.getName() + "'s Trade Log")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogClick(log, event))
                .build(SafeTrade.getPlugin());

        updateLogInventory(inventory, log);

        return inventory;
    }

    private static void updateLogInventory(Inventory inventory, Log log) {
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            // First participant
            if (i == 0) {
                slot.set(ItemUtils.Logs.getMoney(log.getParticipant()));
            }
            else if (i == 1) {
                slot.set(ItemUtils.Logs.getItems(log.getParticipant()));
            }
            else if (i == 2) {
                slot.set(ItemUtils.Logs.getPokemon(log.getParticipant()));
            }
            else if (i == 3) {
                slot.set(ItemUtils.Logs.getHead(log.getParticipant()));
            }

            // Other participant
            else if (i == 5) {
                slot.set(ItemUtils.Logs.getHead(log.getOtherParticipant()));
            }
            else if (i == 6) {
                slot.set(ItemUtils.Logs.getPokemon(log.getOtherParticipant()));
            }
            else if (i == 7) {
                slot.set(ItemUtils.Logs.getItems(log.getOtherParticipant()));
            }
            else if (i == 8) {
                slot.set(ItemUtils.Logs.getMoney(log.getOtherParticipant()));
            }

            else if (i <= 8) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.LIGHT_BLUE));
            }
        });
    }

    private static void handleLogClick(Log log, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(log.getInventory()));
                    }
                    else if (item.equalTo(ItemUtils.Logs.getItems(log.getParticipant()))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getLogItemsInventory(log, log.getParticipant(), log.getSidesItems(), player.hasPermission("safetrade.admin.logs.interact.items"))))
                                ;
                    }
                    else if (item.equalTo(ItemUtils.Logs.getItems(log.getOtherParticipant()))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getLogItemsInventory(log, log.getOtherParticipant(), log.getOtherSidesItems(), player.hasPermission("safetrade.admin.logs.interact.items"))))
                                ;
                    }
                    else if (item.equalTo(ItemUtils.Logs.getPokemon(log.getParticipant()))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getLogPokemonInventory(log, log.getParticipant(), log.getSidesPokemon(), player.hasPermission("safetrade.admin.logs.interact.pokemon"))))
                                ;
                    }
                    else if (item.equalTo(ItemUtils.Logs.getPokemon(log.getOtherParticipant()))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getLogPokemonInventory(log, log.getOtherParticipant(), log.getOtherSidesPokemon(), player.hasPermission("safetrade.admin.logs.interact.pokemon"))))
                                ;
                    }
                    else if (item.equalTo(ItemUtils.Logs.getMoney(log.getParticipant()))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getLogMoneyInventory(log, log.getParticipant(), log.getSidesMoneyWrappers(), player.hasPermission("safetrade.admin.logs.interact.money"))))
                                ;
                    }
                    else if (item.equalTo(ItemUtils.Logs.getMoney(log.getOtherParticipant()))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getLogMoneyInventory(log, log.getOtherParticipant(), log.getOtherSidesMoneyWrappers(), player.hasPermission("safetrade.admin.logs.interact.money"))))
                                ;
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Log (Items)
    //
    //
    //  CLICKERS
    //
    //  - Log (Items)
    //

    private static Inventory getLogItemsInventory(Log log, OfflinePlayer user, List<ReadWriteNBT> items, boolean adminAccess) {
        List<ItemStack> itemsForClicking = new ArrayList<>();
        items.forEach(item -> itemsForClicking.add(item.createStack()));
        if (adminAccess) {
            itemsForClicking.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to put this item in your SafeTrade storage"));
                existingLore.add(Text.of(ChatColor.GOLD, "Right-click to put this item in " + user.getName() + "'s SafeTrade storage"));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, user.getName() + "'s Traded Items")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogItemsClick(log, user, items, itemsForClicking, event))
                .build(SafeTrade.getPlugin());

        updateLogItemsInventory(inventory, itemsForClicking);

        return inventory;
    }

    private static void updateLogItemsInventory(Inventory inventory, List<ItemStack> itemsForClicking) {
        Iterator<ItemStack> iter = itemsForClicking.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handleLogItemsClick(Log log, OfflinePlayer user, List<ReadWriteNBT> actualItems, List<ItemStack> itemsForClicking, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    PlayerStorage storage;

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(log.getInventory()));
                    }

                    // If the player does not have interact permissions they will not be able to interact with the items of the log.
                    if (!player.hasPermission("safetrade.admin.logs.interact.items")) {
                        return;
                    }

                    if (event instanceof ClickInventoryEvent.Primary) {
                        storage = Tracker.getOrCreateStorage(player);
                    }
                    else if (event instanceof ClickInventoryEvent.Secondary) {
                        storage = Tracker.getOrCreateStorage(user);
                    }
                    else {
                        return;
                    }

                    for (ItemStack i : itemsForClicking) {
                        if (item.equalTo(i)) {
                            ReadWriteNBT snapshot = actualItems.get(itemsForClicking.indexOf(i));
                            storage.addItem(snapshot);
                            SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, snapshot.getQuantity(), "x", snapshot.getType().getTranslation().get(), ChatColor.GREEN, " was added to " + (storage.getOfflinePlayer().get().getUniqueId().equals(user.getUniqueId()) ? "your" : (user.getName() + "'s")) + " storage."));
                            break;
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Log (Pokemon)
    //
    //
    //  CLICKERS
    //
    //  - Log (Pokemon)
    //

    private static Inventory getLogPokemonInventory(Log log, OfflinePlayer user, List<Pokemon> pokemon, boolean adminAccess) {
        List<ItemStack> pokemonItems = new ArrayList<>();
        pokemon.forEach(p -> pokemonItems.add(ItemUtils.Pokemon.getPokemonIcon(p)));
        if (adminAccess) {
            pokemonItems.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to put this pokemon in your SafeTrade storage"));
                existingLore.add(Text.of(ChatColor.GOLD, "Right-click to put this pokemon in " + user.getName() + "'s SafeTrade storage"));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, user.getName() + "'s Traded Items")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogPokemonClick(log, user, pokemon, pokemonItems, event))
                .build(SafeTrade.getPlugin());

        updateLogPokemonInventory(inventory, pokemonItems);

        return inventory;
    }

    private static void updateLogPokemonInventory(Inventory inventory, List<ItemStack> pokemonItems) {
        Iterator<ItemStack> iter = pokemonItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handleLogPokemonClick(Log log, OfflinePlayer user, List<Pokemon> pokemon, List<ItemStack> pokemonItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    PlayerStorage storage;

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(log.getInventory()));
                    }

                    // If the player does not have interact permissions they will not be able to interact with the pokemon of the log.
                    if (!player.hasPermission("safetrade.admin.logs.interact.pokemon")) {
                        return;
                    }

                    if (event instanceof ClickInventoryEvent.Primary) {
                        storage = Tracker.getOrCreateStorage(player);
                    }
                    else if (event instanceof ClickInventoryEvent.Secondary) {
                        storage = Tracker.getOrCreateStorage(user);
                    }
                    else {
                        return;
                    }

                    for (ItemStack i : pokemonItems) {
                        if (item.equalTo(i)) {
                            Pokemon p = pokemon.get(pokemonItems.indexOf(i));
                            storage.addPokemon(p);
                            SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, p.getDisplayName(), ChatColor.GREEN, " was added to " + (storage.getOfflinePlayer().get().getUniqueId().equals(user.getUniqueId()) ? "your" : (user.getName() + "'s")) + " storage."));
                            break;
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Log (MoneyWrapper)
    //
    //
    //  CLICKERS
    //
    //  - Log (MoneyWrapper)
    //

    private static Inventory getLogMoneyInventory(Log log, OfflinePlayer user, List<MoneyWrapper> moneyWrappers, boolean adminAccess) {
        List<ItemStack> itemStacks = new ArrayList<>();
        moneyWrappers.forEach(money -> itemStacks.add(ItemUtils.Money.getMoney(money)));
        if (adminAccess) {
            itemStacks.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to put this item in your SafeTrade storage"));
                existingLore.add(Text.of(ChatColor.GOLD, "Right-click to put this item in " + user.getName() + "'s SafeTrade storage"));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, user.getName() + "'s Traded Money")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, 6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleLogMoneyClick(log, user, moneyWrappers, itemStacks, event))
                .build(SafeTrade.getPlugin());

        updateLogMoneyInventory(inventory, itemStacks);

        return inventory;
    }

    private static void updateLogMoneyInventory(Inventory inventory, List<ItemStack> moneyItems) {
        Iterator<ItemStack> iter = moneyItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            } else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            } else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handleLogMoneyClick(Log log, OfflinePlayer user, List<MoneyWrapper> moneyWrappers, List<ItemStack> moneyWrapperItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    PlayerStorage storage;

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(log.getInventory()));
                    }

                    // If the player does not have interact permissions they will not be able to interact with the money of the log.
                    if (!player.hasPermission("safetrade.admin.logs.interact.money")) {
                        return;
                    }

                    if (event instanceof ClickInventoryEvent.Primary) {
                        storage = Tracker.getOrCreateStorage(player);
                    }
                    else if (event instanceof ClickInventoryEvent.Secondary) {
                        storage = Tracker.getOrCreateStorage(user);
                    }
                    else {
                        return;
                    }

                    for (ItemStack i : moneyWrapperItems) {
                        if (item.equalTo(i)) {
                            MoneyWrapper moneyWrapper = moneyWrappers.get(moneyWrapperItems.indexOf(i));
                            storage.addMoney(moneyWrapper);
                            SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, moneyWrapper.getCurrency().getSymbol(), NumberFormat.getNumberInstance(Locale.US).format(moneyWrapper.getBalance().intValue()), ChatColor.GREEN, " was added to " + (storage.getOfflinePlayer().get().getUniqueId().equals(user.getUniqueId()) ? "your" : (user.getName() + "'s")) + " storage."));
                            break;
                        }
                    }
                });
            });
        });
    }

    /** {@link PlayerStorage} inventories: */

    //
    //  INVENTORIES
    //
    //  - Storage (Main)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Main)
    //

    public static Inventory buildAndGetStorageInventory(PlayerStorage storage) {
        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, storage.getOfflinePlayer().get().getName() + "'s SafeTrade Storage", ChatColor.RED, " [ALPHA]")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,1))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStorageClick(storage, event))
                .build(SafeTrade.getPlugin());

        updateStorageInventory(inventory, storage);

        return inventory;
    }

    private static void updateStorageInventory(Inventory inventory, PlayerStorage storage) {
        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i == 1) {
                slot.set(ItemUtils.Storage.getHead(storage));
            }
            else if (i == 2) {
                slot.set(ItemUtils.Storage.getAutoClaim(storage));
            }
            else if (i == 4) {
                slot.set(ItemUtils.Storage.getMoney(storage));
            }
            else if (i == 5) {
                slot.set(ItemUtils.Storage.getItems(storage));
            }
            else if (i == 6) {
                slot.set(ItemUtils.Storage.getPokemon(storage));
            }
            else if (i <= 8) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.LIGHT_BLUE));
            }
        });
    }

    private static void handleStorageClick(PlayerStorage storage, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(storage.getInventory()));
                    }
                    else if (item.equalTo(ItemUtils.Storage.getAutoClaim(storage)) && (storage.getPlayerUUID().equals(player.getUniqueId()) || player.hasPermission("safetrade.admin.storage.interact.autoclaim"))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
                            storage.setAutoGive(!storage.isAutoGiveEnabled()); // This doesn't need to be delayed but no harm
                            storage.giveItems().forEach(snapshot -> SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), ChatColor.GREEN, " was added to your inventory.")));
                            storage.givePokemon().forEach(pokemon -> SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, pokemon.getDisplayName(), ChatColor.GREEN, " was added to your party/pc.")));
                            storage.giveMoney().forEach(money -> SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, money.getCurrency().getSymbol(), money.getBalance().intValue(), ChatColor.GREEN, " was added to your bank account.")));
                            updateStorageInventory(event.getTargetInventory(), storage);
                        });
                    }
                    else if (item.equalTo(ItemUtils.Storage.getItems(storage))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getStorageItemsInventory(player, storage)))
                                ;
                    }
                    else if (item.equalTo(ItemUtils.Storage.getPokemon(storage))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getStoragePokemonInventory(player, storage)))
                                ;
                    }
                    else if (item.equalTo(ItemUtils.Storage.getMoney(storage))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getStorageMoneyInventory(player, storage)))
                                ;
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Storage (Items)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Items)
    //

    private static Inventory getStorageItemsInventory(Player player, PlayerStorage storage) {
        List<ItemStack> itemsForClicking = new ArrayList<>();
        storage.getItems().forEach(item -> itemsForClicking.add(item.createStack()));

        if (player.getUniqueId().equals(storage.getPlayerUUID())) {
            itemsForClicking.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to claim this item to your inventory."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }
        else if (player.hasPermission("safetrade.admin.storage.interact.items")) {
            itemsForClicking.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to add this item to your SafeTrade storage."));
                existingLore.add(Text.of(ChatColor.RED, "Right-click to remove this item from " + storage.getOfflinePlayer().get().getName() + "'s storage."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, storage.getOfflinePlayer().get().getName() + "'s Item Storage")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStorageItemsClick(storage, itemsForClicking, event))
                .build(SafeTrade.getPlugin());

        updateStorageItemsInventory(inventory, itemsForClicking);

        return inventory;
    }

    private static void updateStorageItemsInventory(Inventory inventory, List<ItemStack> itemsForClicking) {
        Iterator<ItemStack> iter = itemsForClicking.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handleStorageItemsClick(final PlayerStorage storage, List<ItemStack> itemsForClicking, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    Iterator<ItemStack> iter = itemsForClicking.iterator();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(storage.getInventory()));
                    }
                    else if (player.getUniqueId().equals(storage.getPlayerUUID())) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                ReadWriteNBT snapshot = storage.getItems().get(itemsForClicking.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    if (Utils.giveItem(player, snapshot)) {
                                        storage.removeItem(snapshot);
                                        iter.remove();
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), ChatColor.GREEN, " was added to your inventory."));
                                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getStorageItemsInventory(player, storage)));
                                    }
                                    else {
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.RED, "Cannot claim item: Inventory is full."));
                                    }
                                }
                                break;
                            }
                        }
                    }
                    else if (player.hasPermission("safetrade.admin.storage.interact.items")) {
                        for (ItemStack i : itemsForClicking) {
                            if (item.equalTo(i)) {
                                ReadWriteNBT snapshot = storage.getItems().get(itemsForClicking.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    Tracker.getOrCreateStorage(player).addItem(snapshot);
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), ChatColor.GREEN, " was added to your storage."));
                                }
                                else if (event instanceof ClickInventoryEvent.Secondary) {
                                    storage.removeItem(snapshot);
                                    iter.remove();
                                    Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updateStorageItemsInventory(event.getTargetInventory(), itemsForClicking));
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, snapshot.getQuantity(), "x ", snapshot.getType().getTranslation().get(), ChatColor.GREEN, " was removed from " + storage.getOfflinePlayer().get().getName() + "'s storage."));
                                }
                                break;
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Storage (Pokemon)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Pokemon)
    //

    private static Inventory getStoragePokemonInventory(Player player, PlayerStorage storage) {
        List<ItemStack> pokemonItems = new ArrayList<>();
        storage.getPokemons().forEach(p -> pokemonItems.add(ItemUtils.Pokemon.getPokemonIcon(p)));

        if (player.getUniqueId().equals(storage.getPlayerUUID())) {
            pokemonItems.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to claim this Pokemon to your party/pc."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }
        else if (player.hasPermission("safetrade.admin.storage.interact.pokemon")) {
            pokemonItems.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to add this Pokemon to your SafeTrade storage."));
                existingLore.add(Text.of(ChatColor.RED, "Right-click to remove this Pokemon from " + storage.getOfflinePlayer().get().getName() + "'s storage."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, storage.getOfflinePlayer().get().getName() + "'s Pokemon Storage")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStoragePokemonClick(storage, pokemonItems, event))
                .build(SafeTrade.getPlugin());

        updateStoragePokemonInventory(inventory, pokemonItems);

        return inventory;
    }

    private static void updateStoragePokemonInventory(Inventory inventory, List<ItemStack> pokemonItems) {
        Iterator<ItemStack> iter = pokemonItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handleStoragePokemonClick(PlayerStorage storage, List<ItemStack> pokemonItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    Iterator<ItemStack> iter = pokemonItems.iterator();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(storage.getInventory()));
                    }
                    else if (player.getUniqueId().equals(storage.getPlayerUUID())) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                Pokemon p = storage.getPokemons().get(pokemonItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    if (StorageProxy.getParty(storage.getPlayerUUID()).add(p)) {
                                        storage.removePokemon(p);
                                        iter.remove();
                                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getStoragePokemonInventory(player, storage)));
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, p.getDisplayName(), ChatColor.GREEN, " was added to your party/pc."));
                                    }
                                    else {
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.RED, "Cannot claim Pokemon: PC is full."));
                                    }
                                }
                                break;
                            }
                        }
                    }
                    else if (player.hasPermission("safetrade.admin.storage.interact.pokemon")) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                Pokemon p = storage.getPokemons().get(pokemonItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    Tracker.getOrCreateStorage(player).addPokemon(p);
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, p.getDisplayName(), ChatColor.GREEN, " was added to your storage."));
                                }
                                else if (event instanceof ClickInventoryEvent.Secondary) {
                                    storage.removePokemon(p);
                                    iter.remove();
                                    Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updateStoragePokemonInventory(event.getTargetInventory(), pokemonItems));
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, p.getDisplayName(), ChatColor.GREEN, " was removed from " + storage.getOfflinePlayer().get().getName() + "'s storage."));
                                }
                                break;
                            }
                        }
                    }
                });
            });
        });
    }

    //
    //  INVENTORIES
    //
    //  - Storage (Money)
    //
    //
    //  CLICKERS
    //
    //  - Storage (Money)
    //

    private static Inventory getStorageMoneyInventory(Player player, PlayerStorage storage) {
        List<ItemStack> moneyItems = new ArrayList<>();
        storage.getMoney().forEach(moneyWrapper -> moneyItems.add(ItemUtils.Money.getMoney(moneyWrapper)));

        if (player.getUniqueId().equals(storage.getPlayerUUID())) {
            moneyItems.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to claim this money to your bank account."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }
        else if (player.hasPermission("safetrade.admin.storage.interact.money")) {
            moneyItems.forEach(item -> {
                List<BaseComponent[]> existingLore = item.get(Keys.ITEM_LORE).isPresent() ? item.get(Keys.ITEM_LORE).get() : new ArrayList<>();
                if (existingLore.size() != 0) {
                    existingLore.add(Text.of());
                }
                existingLore.add(Text.of(ChatColor.GREEN, "Left-click to add this money to your SafeTrade storage."));
                existingLore.add(Text.of(ChatColor.RED, "Right-click to remove this money from " + storage.getOfflinePlayer().get().getName() + "'s storage."));
                item.offer(Keys.ITEM_LORE, existingLore);
            });
        }

        Inventory inventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.of(ChatColor.DARK_AQUA, storage.getOfflinePlayer().get().getName() + "'s Money Storage")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, event -> handleStorageMoneyClick(storage, moneyItems, event))
                .build(SafeTrade.getPlugin());

        updateStorageMoneyInventory(inventory, moneyItems);

        return inventory;
    }

    private static void updateStorageMoneyInventory(Inventory inventory, List<ItemStack> moneyItems) {
        Iterator<ItemStack> iter = moneyItems.iterator();

        inventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            if (i <= 35) {
                if (iter.hasNext()) {
                    slot.set(iter.next());
                }
            }
            else if (i == 45) {
                slot.set(ItemUtils.Other.getBackButton());
            }
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.GRAY));
            }
        });
    }

    private static void handleStorageMoneyClick(PlayerStorage storage, List<ItemStack> moneyItems, ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (hasCooldown(player.getUniqueId())) {
                return;
            }
            addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                transaction.getSlot().getProperty(SlotIndex.class, "slotindex").ifPresent(slot -> {
                    ItemStack item = transaction.getOriginal().createStack();
                    Iterator<ItemStack> iter = moneyItems.iterator();

                    if (item.equalTo(ItemUtils.Other.getBackButton())) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(storage.getInventory()));
                    }
                    else if (player.getUniqueId().equals(storage.getPlayerUUID())) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                MoneyWrapper moneyWrapper = storage.getMoney().get(moneyItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    if (moneyWrapper.transferBalance(SafeTrade.getEcoService().getOrCreateAccount(player.getUniqueId()).get()).getResult() == ResultType.SUCCESS) {
                                        storage.removeMoney(moneyWrapper);
                                        iter.remove();
                                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(getStorageMoneyInventory(player, storage)));
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, moneyWrapper.getCurrency().getSymbol(), moneyWrapper.getBalance().intValue(), ChatColor.GREEN, " was added to your bank account."));
                                    }
                                    else {
                                        SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.RED, "Cannot claim money: Unknown."));
                                    }
                                }
                                break;
                            }
                        }
                    }
                    else if (player.hasPermission("safetrade.admin.storage.interact.money")) {
                        while (iter.hasNext()) {
                            ItemStack i = iter.next();

                            if (item.equalTo(i)) {
                                MoneyWrapper moneyWrapper = storage.getMoney().get(moneyItems.indexOf(i));

                                if (event instanceof ClickInventoryEvent.Primary) {
                                    Tracker.getOrCreateStorage(player).addMoney(moneyWrapper);
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, moneyWrapper.getCurrency().getSymbol(), moneyWrapper.getBalance().intValue(), ChatColor.GREEN, " was added to your storage."));
                                }
                                else if (event instanceof ClickInventoryEvent.Secondary) {
                                    storage.removeMoney(moneyWrapper);
                                    iter.remove();
                                    Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> updateStorageMoneyInventory(event.getTargetInventory(), moneyItems));
                                    SafeTrade.sendMessageToPlayer(player, PrefixType.STORAGE, Text.of(ChatColor.GOLD, moneyWrapper.getCurrency().getSymbol(), moneyWrapper.getBalance().intValue(), ChatColor.GREEN, " was removed from " + storage.getOfflinePlayer().get().getName() + "'s storage."));
                                }
                                break;
                            }
                        }
                    }
                });
            });
        });
    }

}
