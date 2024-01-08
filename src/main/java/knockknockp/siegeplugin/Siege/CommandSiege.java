package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandSiege implements CommandExecutor {
    private final SiegeManager siegeManager;

    public CommandSiege(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
        return;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        final String firstArgument = args[0];
        if (firstArgument.equals("team")) {
            if (args.length < 3) {
                return false;
            }

            String playerNameToFind = args[1];
            Player player = Bukkit.getPlayer(playerNameToFind);
            if (player == null) {
                commandSender.sendMessage(String.format("A player named %s does not exist or is offline.", playerNameToFind));
                return true;
            }
            player.setScoreboard(siegeManager.scoreboard);

            Teams parsedTeam = parseTeam(commandSender, args[2]);
            if (parsedTeam == Teams.RED) {
                setTeamCommand(commandSender, player, Teams.RED);
            } else if (parsedTeam == Teams.BLUE) {
                setTeamCommand(commandSender, player, Teams.BLUE);
            } else {
                return false;
            }
            return true;
        } else if (firstArgument.equals("base")) {
            if (args.length < 8) {
                return false;
            }

            Location location1 = tryParseCoordinate(commandSender, args[1], args[2], args[3]);
            if (location1 == null) {
                return false;
            }

            Location location2 = tryParseCoordinate(commandSender, args[4], args[5], args[6]);
            if (location2 == null) {
                return false;
            }

            Teams parsedTeam = parseTeam(commandSender, args[7]);
            if (parsedTeam == null) {
                return false;
            }

            siegeManager.setBase(parsedTeam, location1, location2);
            commandSender.sendMessage(String.format("Set base locations for team %s", parsedTeam));
            return true;
        } else if (firstArgument.equals("wool")) {
            if (args.length < 6) {
                return false;
            }

            Location location = tryParseCoordinate(commandSender, args[1], args[2], args[3]);
            if (location == null) {
                return false;
            }

            int index = -1;
            try {
                index = Integer.parseInt(args[4]);
            } catch (Exception exception) {
                Bukkit.getLogger().warning("Wool index parse failed.");
                return false;
            }
            if (index <= -1) {
                return false;
            }

            Teams parsedTeam = parseTeam(commandSender, args[5]);
            if (parsedTeam == null) {
                return false;
            }

            siegeManager.teams.get(parsedTeam).wools[index] = location;
            commandSender.sendMessage(String.format("Set coordinate as team %s's No. %d wool.", parsedTeam, index));
            return true;
        } else if (firstArgument.equals("deposit")) {
            if (args.length < 4) {
                return false;
            }

            Location location = tryParseCoordinate(commandSender, args[1], args[2], args[3]);
            if (location == null) {
                return false;
            }

            Teams parsedTeam = parseTeam(commandSender, args[4]);
            if (parsedTeam == null) {
                return false;
            }

            siegeManager.teams.get(parsedTeam).deposit = location;
            commandSender.sendMessage(String.format("Set coordinate as team %s's deposit.", parsedTeam));
            return true;
        } else if (firstArgument.equals("start")) {
            siegeManager.start();
            commandSender.sendMessage("Started the siege mini game.");
            return true;
        } else if (firstArgument.equals("test")) {
            final String[] commands = new String[] {
                    "time set 0",
                    "weather clear",
                    "difficulty peaceful",
                    "siege team KnockKnockP red",
                    "siege team Player1 blue",
                    "siege base -93 93 -168 -100 88 -174 red",
                    "siege base -100 93 -198 -93 88 -192 blue",
                    "siege wool -94 90 -169 0 red",
                    "siege wool -96 90 -169 1 red",
                    "siege wool -98 90 -169 2 red",
                    "siege wool -99 90 -197 0 blue",
                    "siege wool -97 90 -197 1 blue",
                    "siege wool -95 90 -197 2 blue",
                    "siege deposit -99 90 -172 red",
                    "siege deposit -94 90 -194 blue"
            };
            for (final String commandLine : commands) {
                Bukkit.getServer().dispatchCommand(commandSender, commandLine);
            }
            return true;
        }
        return false;
    }

    private Teams parseTeam(CommandSender commandSender, String teamNameToParse) {
        if (teamNameToParse.equals("red")) {
            return Teams.RED;
        } else if (teamNameToParse.equals("blue")) {
            return Teams.BLUE;
        }
        commandSender.sendMessage(String.format("A team named %s does not exist.", teamNameToParse));
        return null;
    }

    private Location parseCoordinate(CommandSender commandSender, String[] coordinateToParse) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be executed by a player.");
            return null;
        }

        double[] xyz = new double[3];
        for (int i = 0; i < 3; ++i) {
            xyz[i] = Integer.parseInt(coordinateToParse[i]);
        }
        return new Location(((Player)(commandSender)).getWorld(), xyz[0], xyz[1], xyz[2]);
    }

    private Location tryParseCoordinate(CommandSender commandSender, String coordinate1, String coordinate2, String coordinate3) {
        Location location = null;
        try {
            location = parseCoordinate(commandSender, new String[] { coordinate1, coordinate2, coordinate3 });
        } catch (Exception exception) {
            Bukkit.getLogger().info(exception.getMessage());
        }
        if (location == null) {
            Bukkit.getLogger().warning("Coordinate parse failed.");
            return null;
        }
        return location;
    }

    private void setTeamCommand(CommandSender commandSender, Player player, Teams team) {
        final String name = player.getName();
        siegeManager.teams.get(team).team.addEntry(player.getName());
        if (!siegeManager.players.containsKey(player)) {
            TeamPlayer teamPlayer = new TeamPlayer(player, team);
            siegeManager.players.put(player, teamPlayer);
        } else {
            siegeManager.players.get(player).team = team;
        }

        commandSender.sendMessage(String.format("Added player %s to team %s", name, team));
        return;
    }
}