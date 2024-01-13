package knockknockp.siegeplugin.Siege;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class TESTCOMMAND implements CommandExecutor {
    final static String[] commands = new String[] {
        //"plugman reload SiegePlugin",
        "time set 0",
        "weather clear",
        "difficulty peaceful",
        "siege team KnockKnockP red",
        "siege team Player0 blue",
        "siege base -93 93 -168 -100 88 -174 red",
        "siege base -100 93 -198 -93 88 -192 blue",
        "siege wool -94 90 -169 red",
        "siege wool -96 90 -169 red",
        "siege wool -98 90 -169 red",
        "siege wool -99 90 -197 blue",
        "siege wool -97 90 -197 blue",
        "siege wool -95 90 -197 blue",
        "siege deposit -99 90 -172 red",
        "siege deposit -94 90 -194 blue",
        "siege team_chest -98 89 -182 neutral",
        "siege team_chest -94 89 -173 red",
        "siege team_chest -99 89 -193 blue",
        "siege resetting_chest -98 89 -182 2 중립 상자",
        "siege resetting_chest -94 89 -173 3 빨강 상자",
        "siege resetting_chest -99 89 -193 4 파랑 상자",
        "siege team_chest -94 89 -171 red",
        "siege team_chest -99 89 -195 blue",
        "siege resetting_chest -93 89 -187 5 네더라이트 상자",
        "siege spawn -95 89 -170 red",
        "siege spawn -96 89 -194 blue",
        "siege time 60",
        "siege respawn 1"
        //"siege wand"
    };

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        for (String commandLine : commands) {
            commandSender.sendMessage(commandLine);
            ((Player)(commandSender)).performCommand(commandLine);
        }
        return true;
    }
}