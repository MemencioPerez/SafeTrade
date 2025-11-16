package io.github.cjcool06.safetrade.utils;

import com.pixelmonmod.api.pokemon.requirement.impl.HasSpecFlagRequirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import de.tr7zw.nbtapi.NBT;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.obj.MoneyWrapper;
import io.github.cjcool06.safetrade.obj.Side;
import io.github.cjcool06.safetrade.obj.Trade;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.ChatColor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static net.md_5.bungee.api.chat.BaseComponent.toLegacyText;

public class Utils {

    public static final HasSpecFlagRequirement unbreedable = new HasSpecFlagRequirement("unbreedable");
    public static final HasSpecFlagRequirement untradeable = new HasSpecFlagRequirement("untradeable");

    public static Optional<OfflinePlayer> getOfflinePlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).hasPlayedBefore() ? Optional.of(Bukkit.getOfflinePlayer(uuid)) : Optional.empty();
    }

    public static ItemStack getPicture(Pokemon pokemon) {
        if (pokemon.isEgg()) {
            net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("SpriteName", "pixelmon:sprites/eggs/egg1");
            itemStack.setTag(nbt);
            return (ItemStack)(Object)itemStack;
        }

        return (ItemStack)(Object)ItemPixelmonSprite.getPhoto(pokemon);
    }

    public static BaseComponent[] getTradeOverviewLore(Trade trade) {
        ComponentBuilder builder1 =  Text.builder();
        ComponentBuilder builder2 = Text.builder();

        builder1.append(Text.of("Money:"))
                .color(ChatColor.DARK_AQUA)
                .create();
        builder2.append(Text.of("Money:"))
                .color(ChatColor.DARK_AQUA)
                .create();

        for (MoneyWrapper wrapper : trade.getSides()[0].vault.getAllMoney()) {
            builder1.append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.GOLD, wrapper.getCurrency().getSymbol(), ChatColor.AQUA, NumberFormat.getNumberInstance(Locale.US).format(wrapper.getBalance().intValue())));
        }
        for (MoneyWrapper wrapper : trade.getSides()[1].vault.getAllMoney()) {
            builder2.append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.GOLD, wrapper.getCurrency().getSymbol(), ChatColor.AQUA, NumberFormat.getNumberInstance(Locale.US).format(wrapper.getBalance().intValue())));
        }

        builder1.append(Text.of("\n\n" + "Pokemon:"))
                .color(ChatColor.DARK_AQUA)
                .create();
        builder2.append(Text.of("\n\n" + "Pokemon:"))
                .color(ChatColor.DARK_AQUA)
                .create();
        for (Pokemon pokemon : trade.getSides()[0].vault.getAllPokemon()) {
            if (pokemon.isEgg()) {
                builder1.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.AQUA, (Config.showEggName ? pokemon.getSpecies().getLocalizedName() + " Egg" : "Egg"))).create()).create();
            }
            else {
                builder1.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.AQUA, pokemon.getSpecies().getLocalizedName())).create()).create();
            }
        }
        for (Pokemon pokemon : trade.getSides()[1].vault.getAllPokemon()) {
            if (pokemon.isEgg()) {
                builder2.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.AQUA, (Config.showEggName ? pokemon.getSpecies().getLocalizedName() + " Egg" : "Egg"))).create()).create();
            }
            else {
                builder2.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.AQUA, pokemon.getSpecies().getLocalizedName())).create()).create();
            }
        }

        builder1.append(Text.of("\n\n" + "Items:"))
                .color(ChatColor.DARK_AQUA)
                .create();
        builder2.append(Text.of("\n\n" + "Items:"))
                .color(ChatColor.DARK_AQUA)
                .create();
        for (ReadWriteNBT snapshot : trade.getSides()[0].vault.getAllItems()) {
            builder1.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.DARK_GREEN, snapshot.getQuantity() + "x ", ChatColor.AQUA, snapshot.getTranslation().get()))
                    .create()).create();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder1.append(Text.builder().append(Text.of("  ", ChatColor.GOLD, "[", snapshot.get(Keys.DISPLAY_NAME).get(), ChatColor.GOLD, "]")).create()).create();
            }
        }
        for (ReadWriteNBT snapshot : trade.getSides()[1].vault.getAllItems()) {
            builder2.append(Text.builder().append(Text.of("\n", ChatColor.GREEN, "- ", ChatColor.DARK_GREEN, snapshot.getQuantity() + "x ", ChatColor.AQUA, snapshot.getTranslation().get()))
                    .create()).create();
            if (snapshot.get(Keys.DISPLAY_NAME).isPresent()) {
                builder2.append(Text.builder().append(Text.of("  ", ChatColor.GRAY, "[", ChatColor.GOLD, snapshot.get(Keys.DISPLAY_NAME).get(), ChatColor.GRAY, "]")).create()).create();
            }
        }

        return new BaseComponent[]{builder1.create(), builder2.create()};
    }

    public static BaseComponent[] getSuccessMessage(Trade trade) {
        BaseComponent[] texts = getTradeOverviewLore(trade);

        return BaseComponent.builder("SafeTrade Overview >> ")
                .color(ChatColor.GREEN)
                .style(ChatColor.BOLD)
                .append(Text.builder().append(Text.of(ChatColor.DARK_AQUA, trade.getSides()[0].getOfflinePlayer().get().getName()))
                        .onHover(BaseComponentActions.showBaseComponent(texts[0]))
                        .create())
                .append(Text.builder().append(Text.of(ChatColor.DARK_AQUA, " & "))
                        .create())
                .append(Text.builder().append(Text.of(ChatColor.DARK_AQUA, trade.getSides()[1].getOfflinePlayer().get().getName()))
                        .onHover(BaseComponentActions.showBaseComponent(texts[1]))
                        .create())
                .create();
    }

    public static BaseComponent[] getTradeOverview(Trade trade) {
        BaseComponent[] texts = getTradeOverviewLore(trade);

        return Text.builder()
                .append(Text.builder().append(Text.of(ChatColor.DARK_AQUA, trade.getSides()[0].getOfflinePlayer().get().getName()))
                        .onHover(BaseComponentActions.showBaseComponent(texts[0]))
                        .create())
                .append(Text.builder().append(Text.of(ChatColor.DARK_AQUA, " & "))
                        .create())
                .append(Text.builder().append(Text.of(ChatColor.DARK_AQUA, trade.getSides()[1].getOfflinePlayer().get().getName()))
                        .onHover(BaseComponentActions.showBaseComponent(texts[1]))
                        .create())
                .create();
    }

    public static ArrayList<String> getPokemonLore(Pokemon pokemon) {
        ArrayList<String> lore = new ArrayList<>();
        if (pokemon.isEgg() && !Config.showEggStats) {
            lore.add(toLegacyText(Text.of(ChatColor.GRAY, "The stats of this egg are a mystery.")));
            return lore;
        }
        DecimalFormat df = new DecimalFormat("#0.##");
        int ivSum = pokemon.getStats().getIVs().getStat(BattleStatsType.HP) + pokemon.getStats().getIVs().getStat(BattleStatsType.ATTACK) + pokemon.getStats().getIVs().getStat(BattleStatsType.DEFENSE) + pokemon.getStats().getIVs().getStat(BattleStatsType.SPECIAL_ATTACK) + pokemon.getStats().getIVs().getStat(BattleStatsType.SPECIAL_DEFENSE) + pokemon.getStats().getIVs().getStat(BattleStatsType.SPEED);
        int evSum = pokemon.getStats().getEVs().getStat(BattleStatsType.HP) + pokemon.getStats().getEVs().getStat(BattleStatsType.ATTACK) + pokemon.getStats().getEVs().getStat(BattleStatsType.DEFENSE) + pokemon.getStats().getEVs().getStat(BattleStatsType.SPECIAL_ATTACK) + pokemon.getStats().getEVs().getStat(BattleStatsType.SPECIAL_DEFENSE) + pokemon.getStats().getEVs().getStat(BattleStatsType.SPEED);
        // Stats
        //String star = "\u2605";
        String nickname = pokemon.getNickname() == null ? pokemon.getSpecies().getLocalizedName() : pokemon.getNickname();
        //String shiny = pokemon.getIsShiny() ? star : "";
        String shiny = pokemon.isShiny() ? "Yes" : "No";
        int level = pokemon.getPokemonLevel();
        String nature = pokemon.getNature().getLocalizedName();
        String growth = pokemon.getGrowth().getLocalizedName();
        String ability = pokemon.getAbility().getLocalizedName();
        String originalTrainer = pokemon.getOriginalTrainer();
        String heldItem = "";
        if(pokemon.getHeldItem() != net.minecraft.item.ItemStack.EMPTY) {
            heldItem += pokemon.getHeldItem().getDisplayName();
        }
        else {
            heldItem += "None";
        }
        String breedable = unbreedable.isDataMatch(pokemon) ? "No" : "Yes";
        String tradeable = untradeable.isDataMatch(pokemon) ? "No" : "Yes";
        // EVs
        int hpEV = pokemon.getStats().getEVs().getStat(BattleStatsType.HP);
        int attackEV = pokemon.getStats().getEVs().getStat(BattleStatsType.ATTACK);
        int defenceEV = pokemon.getStats().getEVs().getStat(BattleStatsType.DEFENSE);
        int spAttkEV = pokemon.getStats().getEVs().getStat(BattleStatsType.SPECIAL_ATTACK);
        int spDefEV = pokemon.getStats().getEVs().getStat(BattleStatsType.SPECIAL_DEFENSE);
        int speedEV = pokemon.getStats().getEVs().getStat(BattleStatsType.SPEED);
        String totalEVs = df.format((long)((int)((double)evSum / 510.0D * 100.0D))) + "%";
        // IVs
        int hpIV = pokemon.getStats().getIVs().getStat(BattleStatsType.HP);
        int attackIV = pokemon.getStats().getIVs().getStat(BattleStatsType.ATTACK);
        int defenceIV = pokemon.getStats().getIVs().getStat(BattleStatsType.DEFENSE);
        int spAttkIV = pokemon.getStats().getIVs().getStat(BattleStatsType.SPECIAL_ATTACK);
        int spDefIV = pokemon.getStats().getIVs().getStat(BattleStatsType.SPECIAL_DEFENSE);
        int speedIV = pokemon.getStats().getIVs().getStat(BattleStatsType.SPEED);
        String totalIVs = df.format((long)((int)((double)ivSum / 186.0D * 100.0D))) + "%";
        // Moves
        String move1 = pokemon.getMoveset().attacks[0] != null ? "" + pokemon.getMoveset().attacks[0] : "None";
        String move2 = pokemon.getMoveset().attacks[1] != null ? "" + pokemon.getMoveset().attacks[1] : "None";
        String move3 = pokemon.getMoveset().attacks[2] != null ? "" + pokemon.getMoveset().attacks[2] : "None";
        String move4 = pokemon.getMoveset().attacks[3] != null ? "" + pokemon.getMoveset().attacks[3] : "None";

        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Nickname: ", ChatColor.LIGHT_PURPLE, nickname)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Shiny: ", ChatColor.LIGHT_PURPLE, shiny)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Level: ", ChatColor.LIGHT_PURPLE, level)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Nature: ", ChatColor.LIGHT_PURPLE, nature)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Growth: ", ChatColor.LIGHT_PURPLE, growth)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Ability: ", ChatColor.LIGHT_PURPLE, ability)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "OT: ", ChatColor.LIGHT_PURPLE, originalTrainer)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Held Item: ", ChatColor.LIGHT_PURPLE, heldItem)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Breedable: ", ChatColor.LIGHT_PURPLE, breedable)));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Tradeable: ", ChatColor.LIGHT_PURPLE, tradeable)));
        lore.add(toLegacyText(Text.of()));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "IVs: ", ChatColor.GRAY, "(", ChatColor.RED, totalIVs, ChatColor.GRAY, ")")));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, "Att: ", ChatColor.GREEN, attackIV, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, "Sp.Att: ", ChatColor.GREEN, spAttkIV)));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, "Def: ", ChatColor.GREEN, defenceIV, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, "Sp.Def: ", ChatColor.GREEN, spDefIV)));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, "HP: ", ChatColor.GREEN, hpIV, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, "Speed: ", ChatColor.GREEN, speedIV)));
        lore.add(toLegacyText(Text.of()));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "EVs: ", ChatColor.GRAY, "(", ChatColor.RED, totalEVs, ChatColor.GRAY, ")")));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, "Att: ", ChatColor.GREEN, attackEV, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, "Sp.Att: ", ChatColor.GREEN, spAttkEV)));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, "Def: ", ChatColor.GREEN, defenceEV, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, "Sp.Def: ", ChatColor.GREEN, spDefEV)));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, "HP: ", ChatColor.GREEN, hpEV, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, "Speed: ", ChatColor.GREEN, speedEV)));
        lore.add(toLegacyText(Text.of()));
        lore.add(toLegacyText(Text.of(ChatColor.DARK_AQUA, "Moves:")));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, move1, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, move2)));
        lore.add(toLegacyText(Text.of(ChatColor.AQUA, move3, ChatColor.DARK_GRAY, " | ", ChatColor.AQUA, move4)));

        return lore;
    }

    public static boolean isPlayerOccupied(Player bukkitPlayer) {
        ServerPlayerEntity forgePlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player.getUniqueId());
        if (forgePlayer != null) {
            BattleController bc = BattleRegistry.getSpectatedBattle(forgePlayer);
            if (bc != null) {
                return true;
            }
            bc = BattleRegistry.getBattle(forgePlayer);

            return bc != null;
        }
    }

    public static LocalDateTime convertToUTC(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static void recallAllPokemon(PlayerPartyStorage storage) {
        storage.getTeam().forEach(pokemon -> {
            Optional<PixelmonEntity> entity = pokemon.getPixelmonEntity();
            entity.ifPresent(PixelmonEntity::unloadEntity);
        });
    }

    @SuppressWarnings("unchecked")
    public static LinkedHashMap<ItemStack, Pokemon>[] generatePCMaps(Side side) {
        PCStorage pcStorage = StorageProxy.getPCForPlayer(side.getOfflinePlayer().get().getUniqueId());
        List<Pokemon> pcPokemon = getAllPokemon(pcStorage);
        PlayerPartyStorage partyStorage = StorageProxy.getParty(side.getOfflinePlayer().get().getUniqueId());
        List<Pokemon> partyPokemon = getAllPokemon(partyStorage);

        LinkedHashMap<ItemStack, Pokemon> partyMap = new LinkedHashMap<>();
        LinkedHashMap<ItemStack, Pokemon> pcMap = new LinkedHashMap<>();

        for (Pokemon pokemon : partyPokemon) {
            partyMap.put(ItemUtils.Pokemon.getPokemonIcon(pokemon), pokemon);
        }
        for (Pokemon pokemon : pcPokemon) {
            pcMap.put(ItemUtils.Pokemon.getPokemonIcon(pokemon), pokemon);
        }

        return new LinkedHashMap[]{partyMap, pcMap};
    }

    public static List<Pokemon> getAllPokemon(PCStorage storage) {
        List<Pokemon> pokemon = new ArrayList<>();
        for (Pokemon p : storage.getAll()) {
            if (p != null) {
                pokemon.add(p);
            }
        }

        return pokemon;
    }

    public static List<Pokemon> getAllPokemon(PlayerPartyStorage storage) {
        List<Pokemon> pokemon = new ArrayList<>();
        for (Pokemon p : storage.getAll()) {
            if (p != null) {
                pokemon.add(p);
            }
        }

        return pokemon;
    }

    public static boolean giveItem(Player player, ReadWriteNBT snapshot) {
        Inventory inv = player.getInventory();
        ItemStack item = NBT.itemStackFromNBT(snapshot);

        return inv.addItem(item).isEmpty();
    }
}
