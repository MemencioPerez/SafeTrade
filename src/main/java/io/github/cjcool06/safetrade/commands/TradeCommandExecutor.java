package io.github.cjcool06.safetrade.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class TradeCommandExecutor implements CommandExecutor {

    private final TradeCommand tradeCommand;
    private final HashMap<String, ChildCommandExecutor> childCommandExecutors = new HashMap<>();

    public TradeCommandExecutor() {
        tradeCommand = new TradeCommand();
        childCommandExecutors.put("open", new OpenCommand());
        childCommandExecutors.put("end", new EndCommand());
        childCommandExecutors.put("storage", new StorageCommand());
        childCommandExecutors.put("logs", new LogsCommand());
        childCommandExecutors.put("view", new ViewCommand());
        childCommandExecutors.put("reload", new ReloadCommand());
        childCommandExecutors.put("wiki", new WikiCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandArgs commandArgs = parseCommandArgs(args);
        if (commandArgs.<String>getOne("subcommand").isPresent()) {
            childCommandExecutors.get(args[0]).execute(sender, commandArgs);
        } else {
            tradeCommand.execute(sender, commandArgs);
        }

        return true;
    }

    private CommandArgs parseCommandArgs(@NotNull String[] args) {
        CommandArgs commandArgs = new CommandArgs();
        for (String arg : args) {
            Player targetPlayer = Bukkit.getPlayer(arg);
            if (targetPlayer != null) {
                if (commandArgs.<Player>getOne("target").isPresent()) {
                    commandArgs.put("target2", targetPlayer);
                } else {
                    commandArgs.put("target", targetPlayer);
                }
            } else if (childCommandExecutors.containsKey(arg)) {
                commandArgs.put("subcommand", arg);
            } else if (arg.equalsIgnoreCase("clear")) {
                commandArgs.put("options", "clear");
            }
        }
        return commandArgs;
    }
}
