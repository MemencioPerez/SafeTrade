package io.github.cjcool06.safetrade.obj;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.cjcool06.safetrade.SafeTrade;
import io.github.cjcool06.safetrade.api.enums.CommandType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A CommandWrapper encapsulates the functionality of Bukkit's command execution
 * that suits {@link PlayerStorage}.
 *
 * This wrapper is serialisable.
 */
public class CommandWrapper {
    public final String cmd;
    public final CommandType commandType;

    public CommandWrapper(String cmd, CommandType commandType) {
        this.cmd = cmd;
        this.commandType = commandType;
    }

    /**
     * Execute the command as {@link org.bukkit.command.ConsoleCommandSender}.
     */
    public void consoleExecute() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    /**
     * Execute the command as the {@link Player}.
     *
     * @param player The player
     */
    public void sudoExecute(Player player) {
        Bukkit.dispatchCommand(player, cmd);
    }

    public void toContainer(JsonObject jsonObject) {
        jsonObject.add("Command", new JsonPrimitive(cmd));
        jsonObject.add("CommandType", new JsonPrimitive(commandType.name()));
    }

    public static CommandWrapper fromContainer(JsonObject jsonObject) {
        try {
            String cmd = jsonObject.get("Command").getAsString();
            CommandType commandType = jsonObject.get("CommandType").getAsString().equals("CONSOLE") ? CommandType.CONSOLE : CommandType.SUDO;

            return new CommandWrapper(cmd, commandType);
        } catch (Exception e) {
            SafeTrade.getPluginLogger().warning("There was a problem deserialising a CommandWrapper from a container." +
                    "\n" + e.getMessage() + "\n");
            e.printStackTrace();
            return null;
        }
    }
}