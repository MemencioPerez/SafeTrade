package io.github.cjcool06.safetrade.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public final class Text {

    private Text() {}

    public static BaseComponent[] of(Object... args) {
        ComponentBuilder builder = new ComponentBuilder();
        for (Object arg : args) {
            if (arg instanceof String) {
                builder.append((String) arg);
            } else if (arg instanceof ChatColor) {
                if (arg.equals(ChatColor.MAGIC)) {
                    builder.obfuscated(true);
                } else if (arg.equals(ChatColor.BOLD)) {
                    builder.bold(true);
                } else if (arg.equals(ChatColor.STRIKETHROUGH)) {
                    builder.strikethrough(true);
                } else if (arg.equals(ChatColor.UNDERLINE)) {
                    builder.underlined(true);
                } else if (arg.equals(ChatColor.ITALIC)) {
                    builder.italic(true);
                } else if (arg.equals(ChatColor.RESET)) {
                    builder.reset();
                } else {
                    builder.color((ChatColor) arg);
                }
            } else {
                throw new IllegalArgumentException("Invalid argument: " + arg);
            }
        }
        return builder.create();
    }

    public static ComponentBuilder builder() {
        return new ComponentBuilder();
    }

    public static ComponentBuilder builder(String text) {
        return new ComponentBuilder(text);
    }
}
