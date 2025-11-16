package io.github.cjcool06.safetrade.obj;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.InventoryType;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.api.enums.TradeResult;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.api.events.trade.state.StateChangedEvent;
import io.github.cjcool06.safetrade.api.events.trade.TradeCreationEvent;
import io.github.cjcool06.safetrade.api.events.trade.TradeEvent;
import io.github.cjcool06.safetrade.channels.TradeChannel;
import io.github.cjcool06.safetrade.helpers.InventoryHelper;
import io.github.cjcool06.safetrade.trackers.Tracker;
import io.github.cjcool06.safetrade.utils.BukkitEventManagerUtils;
import io.github.cjcool06.safetrade.utils.ItemUtils;
import io.github.cjcool06.safetrade.utils.LogUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

/**
 * A Trade represents two participants attempting to trade Items, Pokemon, and Money with each other using an intuitive and real-time GUI.
 */
public class Trade {
    private final UUID id;
    private final Side[] sides;
    private final List<Player> viewers = new ArrayList<>();
    private final TradeChannel tradeChannel = new TradeChannel();
    private final Inventory tradeInventory;

    private TradeState state = TradeState.TRADING;

    public Trade(Player participant1, Player participant2) {
        this(UUID.randomUUID(), participant1, participant2);
    }

    private Trade(UUID id, Player participant1, Player participant2) {
        this.id = id;
        sides = new Side[]{new Side(this, participant1), new Side(this, participant2)};
        tradeChannel.addMember(participant1);
        tradeChannel.addMember(participant2);
        participant1.setMessageChannel(tradeChannel);
        participant2.setMessageChannel(tradeChannel);

        tradeInventory = Inventory.builder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(BaseComponentUtils.of(ChatColor.DARK_AQUA, "SafeTrade - Trade Safely!")))
                .property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9,6))
                .of(InventoryArchetypes.MENU_GRID)
                .listener(ClickInventoryEvent.class, this::handleClick)
                .listener(InteractInventoryEvent.Open.class, event -> InventoryHelper.handleOpen(this, event))
                .listener(InteractInventoryEvent.Close.class, event -> InventoryHelper.handleBasicClose(this, InventoryType.MAIN, event))
                .build(SafeTrade.getPlugin());
        reformatInventory();

        Tracker.addActiveTrade(this);
        Bukkit.getPluginManager().callEvent(new TradeCreationEvent(this));
    }

    /**
     * Gets the {@link UUID} identifier of the trade
     *
     * @return The ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the current {@link TradeState} of the trade.
     *
     * @return The state of the trade
     */
    public TradeState getState() {
        return state;
    }

    /**
     * Gets the {@link TradeChannel} of the trade.
     *
     * @return The channel
     */
    public TradeChannel getChannel() {
        return tradeChannel;
    }

    public Inventory getTradeInventory() {
        return tradeInventory;
    }

    /**
     * Gets a list of participants of the trade as a {@link OfflinePlayer} object.
     *
     * @return The participants
     */
    public List<OfflinePlayer> getParticipants() {
        List<OfflinePlayer> participants = new ArrayList<>();
        participants.add(sides[0].getOfflinePlayer().get());
        participants.add(sides[1].getOfflinePlayer().get());

        return participants;
    }

    /**
     * Gets an immutable list of players currently viewing the trade.
     *
     * @return The viewers
     */
    public List<Player> getViewers() {
        return Collections.unmodifiableList(viewers);
    }

    /**
     * Set the {@link TradeState} of the trade.
     *
     * @param state True to pause, false to un-pause
     */
    public void setState(TradeState state) {
        if (this.state != state) {
            TradeState oldState = this.state;
            this.state = state;
            Bukkit.getPluginManager().callEvent(new StateChangedEvent(this, oldState, state));
        }
    }

    /**
     * Executes the trade.
     */
    public Result executeTrade() {
        if (BukkitEventManagerUtils.post(new TradeEvent.Executing(this))) {
            return new Result(this, TradeResult.CANCELLED);
        }

        Trade.Result result = handleTrade();

        Tracker.removeActiveTrade(this);
        setState(TradeState.ENDED);
        LogUtils.saveLog(result.tradeLog);

        Bukkit.getPluginManager().callEvent(new TradeEvent.Executed.Success(result));

        return result;
    }

    /**
     * Handles the trade execution.
     *
     * @return The result
     */
    private Result handleTrade() {
        Side side0 = getSides()[0];
        Side side1 = getSides()[1];
        PlayerStorage storage0 = Tracker.getOrCreateStorage(side0.getOfflinePlayer().get());
        PlayerStorage storage1 = Tracker.getOrCreateStorage(side1.getOfflinePlayer().get());

        // Handles possible evolutions
        TradeEvolutionWrapper evolutionWrapper = new TradeEvolutionWrapper(this);
        TradeEvolutionWrapper.Result evolutionResult = evolutionWrapper.doEvolutions();

        // Unloads storages to the opposite sides
        side1.vault.unloadToStorage(storage0);
        side0.vault.unloadToStorage(storage1);

        // Requires scheduler as some calls come through Sponge's inventory events
        // and will cause horrible errors (Phase Stack errors, yuck!).
        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
            sides[0].getPlayer().ifPresent(player -> {
                player.setMessageChannel(MessageChannel.TO_ALL);
                player.closeInventory();
            });
            sides[1].getPlayer().ifPresent(player -> {
                player.setMessageChannel(MessageChannel.TO_ALL);
                player.closeInventory();
            });
        });

        return new Trade.Result(this, evolutionResult, TradeResult.SUCCESS);
    }

    /**
     * Forces the trade to close and return all items, money, and Pokemon that are being held by the trade.
     */
    public void forceEnd() {
        Tracker.removeActiveTrade(this);
        unloadToStorages();
        tradeChannel.clearMembers();

        // Requires schedular as some calls come through Sponge's inventory events
        // and will cause horrible errors (Phase Stack errors, yuck!).
        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
            sides[0].getPlayer().ifPresent(player -> {
                player.setMessageChannel(MessageChannel.TO_ALL);
                player.closeInventory();
            });
            sides[1].getPlayer().ifPresent(player -> {
                player.setMessageChannel(MessageChannel.TO_ALL);
                player.closeInventory();
            });
        });

        setState(TradeState.ENDED);
        Bukkit.getPluginManager().callEvent(new TradeEvent.Cancelled(new Trade.Result(this, TradeResult.CANCELLED)));
    }

    /**
     * Moves all possessions in the trade (Pokemon, items, money) in to the respective participant's {@link PlayerStorage}.
     *
     * <p>This is useful when a trade is cancelled or the server is stopping.</p>
     */
    public void unloadToStorages() {
        for (Side side : sides) {
            PlayerStorage storage = Tracker.getOrCreateStorage(side.getOfflinePlayer().get());
            side.vault.unloadToStorage(storage);
        }
    }

    /**
     * Adds a viewer to the trade.
     *
     * <p>Opens the trade for a {@link Player} to view, although they cannot interact with the trade.</p>
     *
     * @param player The player
     * @param openInventory Whether to open the player's inventory
     */
    public void addViewer(Player player, boolean openInventory) {
        viewers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
        viewers.add(player);
        if (openInventory) {
            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> player.openInventory(tradeInventory));
        }
    }

    /**
     * Removes a viewer from the trade.
     *
     * <p>Closes the trade for the {@link Player}.</p>
     *
     * @param player The player
     * @param closeInventory Whether to close the player's inventory
     */
    public void removeViewer(Player player, boolean closeInventory) {
        viewers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
        if (closeInventory) {
            Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), player::closeInventory);
        }
    }

    /**
     * Gets a cloned array of the sides of the trade.
     *
     * @return The sides
     */
    public Side[] getSides() {
        return sides.clone();
    }

    /**
     * Gets the side of a participant, if present.
     *
     * @param uuid The {@link UUID} of the user
     * @return The side
     */
    public Optional<Side> getSide(UUID uuid) {
        if (sides[0].sideOwnerUUID.equals(uuid)) {
            return Optional.of(sides[0]);
        }
        else if (sides[1].sideOwnerUUID.equals(uuid)) {
            return Optional.of(sides[1]);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Sends a {@link BaseComponent} message to both trade participants.
     *
     * @param text The message
     */
    public void sendMessage(BaseComponent[] text) {
        for (Side side : getSides()) {
            side.getPlayer().ifPresent(player -> SafeTrade.sendMessageToPlayer(player, PrefixType.SAFETRADE, text));
        }
    }

    /**
     * Sends a {@link BaseComponent} message to the trade's {@link TradeChannel}.
     *
     * @param text The message
     */
    public void sendChannelMessage(BaseComponent[] text) {
        tradeChannel.send(text);
    }

    /**
     * Handles the inventory click of InventoryType MAIN.
     *
     * @param event The click event
     */
    private void handleClick(ClickInventoryEvent event) {
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> {
            // Prevents players from clicking if they have a cooldown
            if (InventoryHelper.hasCooldown(player.getUniqueId())) {
                return;
            }
            // Needs larger tick delay than 1 because forceEnd has a tick delay
            InventoryHelper.addCooldown(player.getUniqueId());

            event.getTransactions().forEach(transaction -> {
                ItemStack item = transaction.getOriginal().createStack();
                Optional<Side> optSide = getSide(player.getUniqueId());

                // Only players in a side can use these buttons
                if (optSide.isPresent()) {
                    Side side = optSide.get();
                    Side otherSide = optSide.get().getOtherSide();

                    if (item.equalTo(ItemUtils.Main.getReady()) && state == TradeState.TRADING) {
                        side.setReady(true);
                        side.vault.setLocked(true);
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
                            if (side.getOtherSide().isReady() && !side.getOtherSide().isPaused()) {
                                setState(TradeState.WAITING_FOR_CONFIRMATION);
                                side.changeInventory(InventoryType.OVERVIEW);
                                side.getOtherSide().changeInventory(InventoryType.OVERVIEW);
                            }
                            else {
                                reformatInventory();
                            }
                        });
                    }
                    else if (item.equalTo(ItemUtils.Main.getNotReady()) && state == TradeState.TRADING) {
                        side.setReady(false);
                        side.vault.setLocked(false);
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), this::reformatInventory);
                    }
                    else if (item.equalTo(ItemUtils.Main.getPause()) && state == TradeState.TRADING) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> {
                            side.changeInventory(InventoryType.NONE);
                            reformatInventory();
                        });
                    }
                    else if (item.equalTo(ItemUtils.Main.getQuit()) && state == TradeState.TRADING) {
                        sendMessage(BaseComponentUtils.of(ChatColor.GRAY, "Trade ended by " + player.getName() + "."));
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), this::forceEnd);
                    }
                    else if (item.equalTo(ItemUtils.Main.getMoneyStorage(side))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> side.changeInventory(InventoryType.MONEY));
                    }
                    else if (item.equalTo(ItemUtils.Main.getItemStorage(side))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> side.changeInventory(InventoryType.ITEM));
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(side))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> side.changeInventory(InventoryType.POKEMON));
                    }
                    else if (item.equalTo(ItemUtils.Main.getItemStorage(otherSide))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> otherSide.changeInventoryForViewer(player, InventoryType.ITEM));
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(otherSide))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> otherSide.changeInventoryForViewer(player, InventoryType.POKEMON));
                    }
                }
                // Viewers can use these buttons
                else {
                    // Side 1
                    if (item.equalTo(ItemUtils.Main.getItemStorage(sides[0]))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> sides[0].changeInventoryForViewer(player, InventoryType.ITEM));
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(sides[0]))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> sides[0].changeInventoryForViewer(player, InventoryType.POKEMON));
                    }

                    // Side 2
                    else if (item.equalTo(ItemUtils.Main.getItemStorage(sides[1]))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> sides[1].changeInventoryForViewer(player, InventoryType.ITEM));
                    }
                    else if (item.equalTo(ItemUtils.Main.getPokemonStorage(sides[1]))) {
                        Bukkit.getScheduler().runTask(SafeTrade.getPlugin(), () -> sides[1].changeInventoryForViewer(player, InventoryType.POKEMON));
                    }
                }
            });
        });
    }

    /**
     * Reformats the inventory.
     */
    public void reformatInventory() {
        tradeInventory.slots().forEach(slot -> {
            int i = slot.getProperty(SlotIndex.class, "slotindex").get().getValue();

            // Side 1
            // Status border
            if ((i >= 0 && i <= 3) || i == 9 || i == 18 || i == 27 || i ==36 || (i >= 45 && i <= 47)) {
                slot.set(ItemUtils.Main.getStateStatus(sides[0]));
            }
            // Head
            else if (i == 11) {
                slot.set(ItemUtils.Main.getHead(sides[0]));
            }
            // Money storage
            else if (i == 20) {
                slot.set(ItemUtils.Main.getMoneyStorage(sides[0]));
            }
            // Item storage
            else if (i == 28) {
                slot.set(ItemUtils.Main.getItemStorage(sides[0]));
            }
            // Pokemon storage
            else if (i == 30) {
                slot.set(ItemUtils.Main.getPokemonStorage(sides[0]));
            }

            // Side 2
            // Status border
            else if ((i >= 5 && i <= 8) || i == 17 || i == 26 || i == 35 || i == 44 || (i >= 51 && i <= 53)) {
                slot.set(ItemUtils.Main.getStateStatus(sides[1]));
            }
            // Head
            else if (i == 15) {
                slot.set(ItemUtils.Main.getHead(sides[1]));
            }
            // Money storage
            else if (i == 24) {
                slot.set(ItemUtils.Main.getMoneyStorage(sides[1]));
            }
            // Item storage
            else if (i == 32) {
                slot.set(ItemUtils.Main.getItemStorage(sides[1]));
            }
            // Pokemon storage
            else if (i == 34) {
                slot.set(ItemUtils.Main.getPokemonStorage(sides[1]));
            }

            // Rest
            // Quit item
            else if (i == 4) {
                slot.set(ItemUtils.Main.getQuit());
            }
            // Middle border (Currently is filler)
            else if (i == 13 || i == 22 || i == 31 || i == 40) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.BLACK));
            }
            // Ready
            else if (i == 48) {
                slot.set(ItemUtils.Main.getReady());
            }
            // Not ready
            else if (i == 50) {
                slot.set(ItemUtils.Main.getNotReady());
            }
            // Pause
            else if (i == 49) {
                slot.set(ItemUtils.Main.getPause());
            }

            // Filler
            else if (i <= 53) {
                slot.set(ItemUtils.Other.getFiller(DyeColor.BLACK));
            }
        });
    }


    /**
     * The result of a {@link Trade}.
     */
    public class Result {
        private final Trade trade;
        private final TradeEvolutionWrapper.Result evolutionResult;
        private final TradeResult tradeResult;
        private final Log tradeLog;

        private Result(Trade trade, TradeResult tradeResult) {
            this.trade = trade;
            this.tradeResult = tradeResult;
            this.evolutionResult = new TradeEvolutionWrapper(trade).DUMMY();
            this.tradeLog = new Log(trade);
        }

        private Result(Trade trade, TradeEvolutionWrapper.Result evolutionResult, TradeResult tradeResult) {
            this.trade = trade;
            this.evolutionResult = evolutionResult;
            this.tradeResult = tradeResult;
            this.tradeLog = new Log(trade);
        }

        public Trade getTrade() {
            return trade;
        }

        public TradeEvolutionWrapper.Result getEvolutionResult() {
            return evolutionResult;
        }

        public TradeResult getTradeResult() {
            return tradeResult;
        }

        public Log getTradeLog() {
            return tradeLog;
        }
    }
}
