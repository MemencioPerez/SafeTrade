package io.github.cjcool06.safetrade.utils;

import com.google.common.collect.Lists;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.TradeState;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.obj.MoneyWrapper;
import io.github.cjcool06.safetrade.obj.PlayerStorage;
import io.github.cjcool06.safetrade.obj.Side;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import io.github.cjcool06.safetrade.economy.currency.Currency;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.md_5.bungee.api.chat.BaseComponent.toLegacyText;

public final class ItemUtils {

    private ItemUtils() {}

    public static final class Main {

        private Main() {}

        public static ItemStack getStateStatus(Side side) {
            ItemStack item = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.AQUA, side.getOfflinePlayer().get().getName() + "'s Trade Status")));
            if (side.isPaused()) {
                item.setType(Material.ORANGE_STAINED_GLASS_PANE);
                meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Current state: ", ChatColor.GOLD, "Paused"))));
            }
            else if (side.isReady()) {
                item.setType(Material.LIME_STAINED_GLASS_PANE);
                meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Current state: ", ChatColor.GREEN, "Ready"))));
            }
            else {
                item.setType(Material.RED_STAINED_GLASS_PANE);
                meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Current state: ", ChatColor.RED, "Not Ready"))));
            }
            item.setItemMeta(meta);
            return item;
        }

        public static ItemStack getQuit() {
            ItemStack item = new ItemStack(Material.BARRIER, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.RED, "Quit")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "End the trade and get your items, money, and Pokemon back"))));
            return item;
        }

        public static ItemStack getHead(Side side) {
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (skullMeta == null) return itemStack;
            side.getOfflinePlayer().ifPresent(skullMeta::setOwningPlayer);
            skullMeta.setDisplayName(toLegacyText(Text.of(ChatColor.DARK_AQUA, side.getOfflinePlayer().get().getName())));
            skullMeta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "This side of the trade holds the Items, Money, and Pokemon that " +
                    side.getOfflinePlayer().get().getName() + " is willing to trade"))));
            return itemStack;
        }

        public static ItemStack getMoneyStorage(Side side) {
            List<String> lore = new ArrayList<>();
            ItemStack item = new ItemStack(Material.GOLD_BLOCK, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Money")));

            for (MoneyWrapper wrapper : side.vault.getAllMoney()) {
                lore.add(toLegacyText(Text.of(
                        ChatColor.DARK_BLUE, "- ", ChatColor.GREEN, wrapper.getCurrency().getSymbol(),
                        NumberFormat.getNumberInstance(Locale.US).format(wrapper.getBalance().intValue())
                )));
            }

            if (side.parentTrade.getState().equals(TradeState.TRADING)) {
                lore.add(toLegacyText(Text.of()));
                lore.add(toLegacyText(Text.of(ChatColor.GRAY, "Click to change the amount of money to trade")));
                lore.add(toLegacyText(Text.of(ChatColor.GOLD, "Only " + side.getOfflinePlayer().get().getName() + " can do this")));
            }

            meta.setLore(lore);

            return item;
        }

        public static ItemStack getItemStorage(Side side) {
            ItemStack item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Items")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Click to view the items that " + side.getOfflinePlayer().get().getName() + " wants to trade"))));
            return item;
        }

        public static ItemStack getPokemonStorage(Side side) {
            ItemStack item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Pokemon")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Click to view the Pokemon that " + side.getOfflinePlayer().get().getName() + " wants to trade"))));
            return item;
        }

        public static ItemStack getReady() {
            ItemStack item = new ItemStack(Material.LIME_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GREEN, "Ready")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Flag yourself as ready"))));
            return item;
        }

        public static ItemStack getNotReady() {
            ItemStack item = new ItemStack(Material.RED_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.RED, "Not Ready")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Flag yourself as not ready"))));
            return item;
        }

        public static ItemStack getPause() {
            ItemStack item = new ItemStack(Material.ORANGE_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Pause")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Flag yourself as paused"))));
            return item;
        }
    }

    public static final class Money {

        private Money() {}

        public static ItemStack changeCurrency() {
            ItemStack item = new ItemStack(Material.IRON_BLOCK, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.LIGHT_PURPLE, "Change Currency")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Add money of a different currency"))));
            return item;
        }

        public static ItemStack getCurrency(Currency currency) {
            ItemStack item = new ItemStack(Material.IRON_INGOT, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, currency.getPluralDisplayName())));
            return item;
        }

        public static ItemStack getMoney(MoneyWrapper wrapper) {
            ItemStack item = new ItemStack(Material.GOLD_INGOT, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, wrapper.getCurrency().getSymbol(), NumberFormat.getNumberInstance(Locale.US).format(wrapper.getBalance().intValue()))));
            return item;
        }

        public static ItemStack getTotalMoney(Side side) {
            List<String> lore = new ArrayList<>();
            ItemStack item = new ItemStack(Material.GOLD_BLOCK, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Money")));

            for (MoneyWrapper wrapper : side.vault.getAllMoney()) {
                lore.add(toLegacyText(Text.of(
                        ChatColor.DARK_BLUE, "- ", ChatColor.GREEN, wrapper.getCurrency().getSymbol(),
                        NumberFormat.getNumberInstance(Locale.US).format(wrapper.getBalance().intValue())
                )));
            }
            meta.setLore(lore);
            return item;
        }

        public static ItemStack getPlayersMoney(Side side, Currency currency) {
            ItemStack item = new ItemStack(Material.DIAMOND_ORE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Currency: ", ChatColor.AQUA, currency.getPluralDisplayName())));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GOLD, "Your balance: ", ChatColor.GREEN, NumberFormat.getNumberInstance(Locale.US).format(SafeTrade.getEcoService().getOrCreateAccount(side.getOfflinePlayer().get().getUniqueId()).get().getBalance(currency).intValue())))));
            return item;
        }

        public static ItemStack getMoneyBars(Currency currency, int amount) {
            ItemStack item = new ItemStack(Material.GOLD_INGOT, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, currency.getSymbol(), NumberFormat.getNumberInstance(Locale.US).format(amount))));
            meta.setLore(Lists.newArrayList(
                    toLegacyText(Text.of(ChatColor.GREEN, "Left-Click: ", ChatColor.GRAY, "Adds ", currency.getPluralDisplayName())),
                    toLegacyText(Text.of(ChatColor.RED, "Right-Click: ", ChatColor.GRAY, "Removes ", currency.getPluralDisplayName()))
            ));
            return item;
        }
    }

    public static final class Pokemon {

        private Pokemon() {}

        public static ItemStack getPC() {
            ItemStack item = new ItemStack(Material.valueOf("PIXELMON_PC"), 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Open PC")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Trade Pokemon from your PC"))));
            return item;
        }

        public static ItemStack getPokemonIcon(com.pixelmonmod.pixelmon.api.pokemon.Pokemon pokemon) {
            ItemStack pokemonIcon = Utils.getPicture(pokemon);
            ItemMeta meta = pokemonIcon.getItemMeta();
            if (meta == null) return pokemonIcon;
            if (pokemon.isEgg()) {
                meta.setDisplayName(toLegacyText(Text.of(ChatColor.LIGHT_PURPLE, Config.showEggName ? pokemon.getSpecies().getLocalizedName() + " Egg" : "Egg")));
            }
            else {
                meta.setDisplayName(toLegacyText(Text.of(ChatColor.LIGHT_PURPLE, pokemon.getSpecies().getLocalizedName())));
                meta.setLore(Utils.getPokemonLore(pokemon));
            }

            return pokemonIcon;
        }
    }

    public static final class PC {

        private PC() {}

        public static ItemStack getPartyInfo() {
            ItemStack item = new ItemStack(Material.PAPER, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Party Pokemon")));
            return item;
        }

        public static ItemStack getNextPage(int currentPage) {
            ItemStack item = new ItemStack(Material.GREEN_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GREEN, "Next Page")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Current page: ", currentPage))));
            return item;
        }

        public static ItemStack getPreviousPage(int currentPage) {
            ItemStack item = new ItemStack(Material.ORANGE_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Previous Page")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Current page: ", currentPage))));
            return item;
        }
    }

    public static final class Overview {

        private Overview() {}

        public static ItemStack getConfirmationStatus(Side side) {
            ItemStack item = new ItemStack(Material.LEGACY_STAINED_GLASS_PANE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.AQUA, side.getOfflinePlayer().get().getName() + "'s Confirmation Status")));
            if (side.isConfirmed()) {
                item.setType(Material.LIME_STAINED_GLASS_PANE);
                meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Current status: ", ChatColor.GREEN, "Ready"))));
            }
            else {
                item.setType(Material.RED_STAINED_GLASS_PANE);
                meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Current status: ", ChatColor.RED, "Not Ready"))));
            }
            return item;
        }

        public static ItemStack getConfirm() {
            ItemStack item = new ItemStack(Material.GREEN_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GREEN, "Confirm")));
            meta.setLore(Lists.newArrayList(
                    toLegacyText(Text.of(ChatColor.GOLD, "Confirm you are happy with the trade"))));
            return item;
        }

        public static ItemStack getCancel() {
            ItemStack item = new ItemStack(Material.YELLOW_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Cancel")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GREEN, "Go back and renegotiate the trade"))));
            return item;
        }

        public static ItemStack getOverviewInfo() {
            ItemStack item = new ItemStack(Material.PAPER, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "What is the trade overview?")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GREEN, "The trade overview allows you to browse the trade and make sure ")),
                    toLegacyText(Text.of(ChatColor.GREEN, "that you are happy.")),
                    toLegacyText(Text.of()),
                    toLegacyText(Text.of(ChatColor.DARK_GREEN, "During this time you are unable to change anything about the trade.")),
                    toLegacyText(Text.of()),
                    toLegacyText(Text.of(ChatColor.GRAY, "The trade will execute once both players have confirmed.")),
                    toLegacyText(Text.of()),
                    toLegacyText(Text.of(ChatColor.RED, "There is no reverting this!"))));
            return item;
        }
    }

    // Yeah yeah, I know this shit is kinda redundant
    public static final class Logs {

        private Logs() {}

        public static ItemStack getMoney(OfflinePlayer user) {
            ItemStack item = new ItemStack(Material.GOLD_BLOCK, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Money")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Click to view the money " + user.getName() + " traded"))));
            return item;
        }

        public static ItemStack getItems(OfflinePlayer user) {
            ItemStack item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Items")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Click to view the items that " + user.getName() + " traded"))));
            return item;
        }

        public static ItemStack getPokemon(OfflinePlayer user) {
            ItemStack item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Pokemon")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "Click to view the Pokemon that " + user.getName() + " traded"))));
            return item;
        }

        public static ItemStack getHead(OfflinePlayer user) {
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (skullMeta == null) return itemStack;
            skullMeta.setOwningPlayer(user);
            skullMeta.setDisplayName(toLegacyText(Text.of(ChatColor.DARK_AQUA, user.getName())));
            skullMeta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, "This side of the trade holds the Items, Money, and Pokemon that " +
                    user.getName() + " traded"))));
            return itemStack;
        }
    }

    public static final class Storage {

        private Storage() {}

        public static ItemStack getMoney(PlayerStorage storage) {
            ItemStack item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Money")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, storage.getOfflinePlayer().get().getName() + "'s stored money."))));
            return item;
        }

        public static ItemStack getItems(PlayerStorage storage) {
            ItemStack item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Items")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, storage.getOfflinePlayer().get().getName() + "'s stored items."))));
            return item;
        }

        public static ItemStack getPokemon(PlayerStorage storage) {
            ItemStack item = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(ChatColor.GOLD, "Pokemon")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY, storage.getOfflinePlayer().get().getName() + "'s stored Pokemon."))));
            return item;
        }

        public static ItemStack getHead(PlayerStorage storage) {
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (skullMeta == null) return itemStack;
            storage.getOfflinePlayer().ifPresent(skullMeta::setOwningPlayer);
            skullMeta.setDisplayName(toLegacyText(Text.of(ChatColor.DARK_AQUA, storage.getOfflinePlayer().get().getName())));
            return itemStack;
        }

        public static ItemStack getAutoClaim(PlayerStorage storage) {
            ItemStack item = new ItemStack(storage.isAutoGiveEnabled() ? Material.LIME_DYE : Material.RED_DYE, 1);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return item;
            meta.setDisplayName(toLegacyText(Text.of(storage.isAutoGiveEnabled() ? ChatColor.GREEN : ChatColor.RED, "AutoClaim")));
            meta.setLore(Lists.newArrayList(toLegacyText(Text.of(ChatColor.GRAY,
                    "SafeTrade will automatically claim anything in or added to your storage."
            ))));
            return item;
        }
    }

    public static final class Other {

        private Other() {}

        public static ItemStack getBackButton() {
            ItemStack itemStack = new ItemStack(Material.RED_DYE, 1);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) return itemStack;
            itemMeta.setDisplayName(ChatColor.RED + "Back");
            itemMeta.setLore(Lists.newArrayList(ChatColor.GRAY + "Return to the previous page."));
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        public static ItemStack getFiller(DyeColor color) {
            return new ItemStack(Material.valueOf(color.name() + "_STAINED_GLASS_PANE"), 1);
        }
    }
}
