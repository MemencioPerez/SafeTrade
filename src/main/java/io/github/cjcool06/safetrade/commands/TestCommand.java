package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.utils.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.bukkit.command.CommandSender;
// import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;

public class TestCommand implements ChildCommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("test"))
                .permission("safetrade.dev.test")
                .arguments(GenericArguments.user(Text.of("target")))
                .executor(new TestCommand())
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        Player player = (Player)sender;
        OfflinePlayer user = args.<OfflinePlayer>getOne("target").get();
        if (player.getUniqueId().equals(UUID.fromString("16511d17-2b88-40e3-a4b2-7b7ba2f45485"))) {

            player.sendMessage(Text.of(ChatColor.GREEN, "Command executed."));
        }
        else {
            player.sendMessage(Text.of(ChatColor.RED, "Only devs can use this command."));
        }

        return true;
    }
}
