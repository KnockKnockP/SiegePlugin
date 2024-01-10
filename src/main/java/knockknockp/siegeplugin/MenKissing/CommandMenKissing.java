package knockknockp.siegeplugin.MenKissing;

import knockknockp.siegeplugin.Siege.SiegeChatColors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandMenKissing implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String name;
        if (args.length > 0) {
            if (!commandSender.hasPermission("lesbian") && (!args[0].equals(commandSender.getName()))) {
                commandSender.sendMessage(SiegeChatColors.ERROR_CHAT_COLOR + "You must have permissions to run this command for others.");
                return true;
            }

            name = args[0];
            Player player = Bukkit.getServer().getPlayer(name);
            if (player == null) {
                commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "Player of the name %s does not exist.", name));
                return true;
            }
        } else {
            name = commandSender.getName();
        }
        Bukkit.getServer().broadcastMessage(String.format(ChatColor.AQUA + "%s likes men kissing!", name));
        return true;
    }
}