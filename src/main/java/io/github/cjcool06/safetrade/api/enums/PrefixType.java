package io.github.cjcool06.safetrade.api.enums;

import io.github.cjcool06.safetrade.utils.Text;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.ChatColor;

public enum PrefixType {
    SAFETRADE(Text.of(ChatColor.DARK_AQUA, ChatColor.BOLD, "SafeTrade", ChatColor.LIGHT_PURPLE, " >> ")),
    STORAGE(Text.of(ChatColor.DARK_AQUA, ChatColor.BOLD, "SafeTrade Storage", ChatColor.LIGHT_PURPLE, " >> ")),
    LOG(Text.of(ChatColor.DARK_AQUA, ChatColor.BOLD, "SafeTrade Log", ChatColor.LIGHT_PURPLE, " >> ")),
    OVERVIEW(Text.of(ChatColor.DARK_AQUA, ChatColor.BOLD, "SafeTrade Overview", ChatColor.LIGHT_PURPLE, " >> ")),
    CONFIG(Text.of(ChatColor.DARK_AQUA, ChatColor.BOLD, "SafeTrade Config", ChatColor.LIGHT_PURPLE, " >> ")),
    NONE(Text.of());

    private BaseComponent[] prefix;

    PrefixType(BaseComponent[] prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the prefix of the type as a {@link BaseComponent}.
     *
     * @return The prefix
     */
    public BaseComponent[] getPrefix() {
        return prefix;
    }
}
