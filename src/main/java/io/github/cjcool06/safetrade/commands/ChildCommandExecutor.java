package io.github.cjcool06.safetrade.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface ChildCommandExecutor {

    boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args);
}
