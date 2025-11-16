package io.github.cjcool06.safetrade.channels;

import io.github.cjcool06.safetrade.utils.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A TradeChannel represents the chat between the two {@link io.github.cjcool06.safetrade.obj.Side}s of a {@link io.github.cjcool06.safetrade.obj.Trade}.
 */
public class TradeChannel {
    List<Player> members = new ArrayList<>();

    public boolean addMember(Player member) {
        return members.add(member);
    }

    public boolean removeMember(Player member) {
        return members.remove(member);
    }

    public void clearMembers() {
        members.clear();
    }

    public Optional<BaseComponent[]> transformMessage(@Nullable Object sender, Player recipient, BaseComponent[] original, ChatMessageType type) {
        return Optional.of(Text.of(ChatColor.AQUA, ChatColor.BOLD, "[Trade] ", ChatColor.RESET, ChatColor.RESET, original));
    }

    public Collection<Player> getMembers(){
            return new ArrayList<>(members);
    }
}
