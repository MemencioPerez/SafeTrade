package io.github.cjcool06.safetrade.commands;

import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.utils.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.bukkit.command.CommandSender;
// import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.action.TextActions;
import net.md_5.bungee.api.ChatColor;

import java.net.MalformedURLException;
import java.net.URL;

public class WikiCommand implements ChildCommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("Get the wiki"))
                .permission("safetrade.common.wiki")
                .executor(new WikiCommand())
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {

        try {
            SafeTrade.sendMessageToCommandSource(sender, PrefixType.SAFETRADE, Text.builder().append(Text.of(ChatColor.GOLD, ChatColor.BOLD, "Click me for wiki")).onClick(TextActions.openUrl(new URL("https://github.com/CJcool06/SafeTrade/wiki"))).build());
        } catch (MalformedURLException me) {
            SafeTrade.sendMessageToCommandSource(sender, PrefixType.SAFETRADE, Text.of(ChatColor.RED, "A problem has occurred, please report this to an administrator."));
        }

        return true;
    }
}
