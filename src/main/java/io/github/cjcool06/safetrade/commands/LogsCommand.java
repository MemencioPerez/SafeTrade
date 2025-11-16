package io.github.cjcool06.safetrade.commands;

import com.google.common.collect.Lists;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.PrefixType;
import io.github.cjcool06.safetrade.managers.DataManager;
import io.github.cjcool06.safetrade.obj.Log;
import io.github.cjcool06.safetrade.utils.Text;
import io.github.cjcool06.safetrade.utils.LogUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.bukkit.command.CommandSender;
// import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.bukkit.OfflinePlayer;
import io.github.cjcool06.safetrade.obj.PaginationList;
import net.md_5.bungee.api.chat.BaseComponent;
import org.spongepowered.api.text.action.TextActions;
import net.md_5.bungee.api.ChatColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LogsCommand implements ChildCommandExecutor {

    public static CommandSpec getSpec() {
        return CommandSpec.builder()
                .description(Text.of("List player logs"))
                .permission("safetrade.admin.logs.view")
                .arguments(
                        GenericArguments.user(Text.of("target")),
                        GenericArguments.optional(GenericArguments.user(Text.of("target2"))))
                .executor(new LogsCommand())
                .build();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull CommandArgs args) {
        OfflinePlayer target = args.<OfflinePlayer>getOne("target").get();
        OfflinePlayer target2 = args.<OfflinePlayer>getOne("target2").isPresent() ? args.<OfflinePlayer>getOne("target2").get().equals(target) ? null : args.<OfflinePlayer>getOne("target2").get() : null;

        SafeTrade.sendMessageToCommandSource(sender, PrefixType.LOG, Text.of(ChatColor.GRAY, "Getting logs, please wait..."));

        showLogs(sender, target, target2);

        return true;
    }

    public static void showLogs(CommandSource sender, OfflinePlayer target, @Nullable OfflinePlayer target2) {
        // Async
        Bukkit.getScheduler().runTaskAsynchronously(SafeTrade.getPlugin(), () -> {
            List<BaseComponent[]> contents = new ArrayList<>();
            ArrayList<Log> logs = target2 == null ? DataManager.getLogs(target) : LogUtils.getLogsOf(target, target2);
            for (Log log : logs) {

                // If the player has the permission "safetrade.admin.logs.delete" they will be able to delete logs.

                // Legacy logs
                if (log.getParticipantUUID() == null) {
                    contents.add(Text.builder().append(
                            sender.hasPermission("safetrade.admin.logs.delete") ?
                                    Text.builder().append(Text.of(ChatColor.RED, "[", ChatColor.DARK_RED, "-", ChatColor.RED, "] "))
                                            .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Click to delete log")))
                                            .onClick(TextActions.executeCallback(dummySender -> {
                                                DataManager.removeLog(target, log);
                                                showLogs(sender, target, target2);
                                            })).build()
                                    : Text.of()
                    ).append(log.getText()).build());
                }

                // Current logs
                else {
                    contents.add(
                            Text.builder().append(
                                sender.hasPermission("safetrade.admin.logs.delete") ?
                                Text.builder().append(Text.of(ChatColor.RED, "[", ChatColor.DARK_RED, "-", ChatColor.RED, "] "))
                                .onHover(TextActions.showText(Text.of(ChatColor.GRAY, "Click to delete log")))
                                .onClick(TextActions.executeCallback(dummySender -> {
                                    DataManager.removeLog(target, log);
                                    showLogs(sender, target, target2);
                                })).build()
                                : Text.of()
                            ).append(log.getDisplayTextWithDate()).build());
                }
            }
            List<BaseComponent[]> reversedContents = Lists.reverse(contents);

            PaginationList.builder()
                    .title(Text.of(" ", ChatColor.GREEN, target.getName() + "'s" + (target2 != null ? (" & " + target2.getName()+ "'s") : "") + " Logs "))
                    .contents(reversedContents)
                    .padding(Text.of(ChatColor.GRAY, "-", ChatColor.RESET))
                    .sendTo(sender);
        });
    }
}
