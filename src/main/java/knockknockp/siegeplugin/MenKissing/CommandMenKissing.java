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
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(SiegeChatColors.ERROR_CHAT_COLOR + "Only humans can like men kissing.");
            return true;
        }

        Bukkit.getServer().broadcastMessage(String.format(ChatColor.AQUA + "%s likes men kissing!", commandSender.getName()));
        return true;
    }
}