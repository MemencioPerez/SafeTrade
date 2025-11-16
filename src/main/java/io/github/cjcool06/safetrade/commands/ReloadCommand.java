package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.config.Config;
import io.github.cjcool06.safetrade.utils.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.bukkit.command.CommandSender;
// import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import net.md_5.bungee.api.ChatColor;

public class ReloadCommand implements ChildCommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Reloads config"))
                .permission("safetrade.admin.reload")
                .executor(new ReloadCommand())
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        Config.load();
        SafeTrade.sendMessageToCommandSource(sender, PrefixType.CONFIG, Text.of(ChatColor.GRAY, "Config reloaded."));

        return true;
    }
}
