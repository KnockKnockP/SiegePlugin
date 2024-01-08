package knockknockp.siegeplugin.Siege;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandSiege implements CommandExecutor {
    private final SiegeManager siegeManager;

    public CommandSiege(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        final String firstArgument = args[0];
        switch (firstArgument) {
            case "team": {
                if (args.length < 3) {
                    return false;
                }

                String playerNameToFind = args[1];
                Player player = Bukkit.getPlayer(playerNameToFind);
                if (player == null) {
                    commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A player named %s does not exist or is offline.", playerNameToFind));
                    return true;
                }

                Teams parsedTeam = parseTeam(commandSender, args[2]);
                if (parsedTeam == null) {
                    return false;
                }

                siegeManager.assignPlayerToTeam(player, parsedTeam);
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Added player %s to team %s", player.getName(), parsedTeam));
                break;
            }
            case "assigner": {
                if (args.length < 2) {
                    return false;
                }

                Teams parsedTeam = parseTeam(commandSender, args[1]);
                if (parsedTeam == null) {
                    return false;
                }

                if (!checkIfPlayer(commandSender)) {
                    return true;
                }

                siegeManager.assigners.add(new Assigner(siegeManager, parsedTeam, (Player)(commandSender)));
                break;
            }
            case "base": {
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
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set base locations for team %s", parsedTeam));
                break;
            }
            case "wool": {
                if (args.length < 6) {
                    return false;
                }

                Location location = tryParseCoordinate(commandSender, args[1], args[2], args[3]);
                if (location == null) {
                    return false;
                }

                int index;
                try {
                    index = Integer.parseInt(args[4]);
                } catch (Exception exception) {
                    Bukkit.getLogger().warning(SiegeChatColors.ERROR_CHAT_COLOR + "Wool index parse failed.");
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
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set coordinate as team %s's No. %d wool.", parsedTeam, index));
                break;
            }
            case "deposit": {
                if (args.length < 5) {
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
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set coordinate as team %s's deposit.", parsedTeam));
                break;
            }
            case "chest": {
                if (args.length < 7) {
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

                long coolDown;
                try {
                    coolDown = Long.parseLong(args[5]);
                } catch (Exception exception) {
                    Bukkit.getLogger().warning(SiegeChatColors.ERROR_CHAT_COLOR + "Failed to parse cool down.");
                    return false;
                }

                if (coolDown <= -1) {
                    return false;
                }

                Block block = location.getBlock();
                if (block.getType() != Material.CHEST) {
                    commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A chest was not found at coordinate %d %d %d.", location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                    return true;
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 6; i < (args.length - 1); ++i) {
                    stringBuilder.append(args[i]);
                    stringBuilder.append(' ');
                }
                stringBuilder.append(args[(args.length - 1)]);

                siegeManager.chests.add(new ResettingChest((Chest) (block.getState()), parsedTeam, coolDown, stringBuilder.toString()));
                commandSender.sendMessage(String.format(
                        SiegeChatColors.SUCCESS_CHAT_COLOR + "Registered chest at %d %d %d for team %s with a cool down of %d ticks.",
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        parsedTeam,
                        coolDown));
                break;
            }
            case "unregister": {
                if (args.length < 5) {
                    return false;
                }

                if (!args[1].equals("chest")) {
                    return false;
                }

                Location location = parseCoordinate(commandSender, new String[]{args[2], args[3], args[4]});
                if (location == null) {
                    return false;
                }

                for (ResettingChest resettingChest : siegeManager.chests) {
                    if (resettingChest.chest.getLocation().equals(location)) {
                        siegeManager.chests.remove(resettingChest);
                        commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Unregistered chest at %d %d %d", location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                        return true;
                    }
                }

                commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "Could not find chest registered at %d %d %d", location.getBlockX(), location.getBlockY(), location.getBlockZ()));
                break;
            }
            case "spawn": {
                if (args.length < 5) {
                    return false;
                }

                Location location = parseCoordinate(commandSender, new String[]{args[1], args[2], args[3]});
                if (location == null) {
                    return false;
                }

                Teams parsedTeam = parseTeam(commandSender, args[4]);
                if (parsedTeam == null) {
                    return false;
                }

                siegeManager.teams.get(parsedTeam).spawn = location;
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set %d %d %d as team %s's spawn position.",
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ(),
                        parsedTeam));
                break;
            }
            case "full":
                if (args.length < 2) {
                    return false;
                }

                if (args[1].equals("reset")) {
                    siegeManager.stop();
                    siegeManager.fullReset();
                    commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Fully resetted the mini game.");
                }
                break;
            case "start":
                siegeManager.start();
                commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Started the mini game.");
                break;
            case "stop":
                siegeManager.stop();
                commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Stopped and resetted the mini game.");
                break;
            case "stats":
                int totalPlayers = siegeManager.players.size(), redPlayers = 0, bluePlayers = 0;
                for (TeamPlayer teamPlayer : siegeManager.players.values()) {
                    if (teamPlayer.team == Teams.RED) {
                        ++redPlayers;
                    } else {
                        ++bluePlayers;
                    }
                }
                commandSender.sendMessage(String.format(ChatColor.YELLOW +         "Siege Mini Game Statistics:\n" +
                                                        ChatColor.YELLOW +         "    Players Assigned:\n" +
                                                        ChatColor.LIGHT_PURPLE +   "        Total: %d\n" +
                                                        Teams.RED.toChatColor() +  "        Red: %d\n" +
                                                        Teams.BLUE.toChatColor() + "        Blue: %d", totalPlayers, redPlayers, bluePlayers));
                break;
            case "test":
                final String[] commands = new String[]{
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
                        "siege deposit -94 90 -194 blue",
                        "siege chest -98 89 -182 neutral 60 중립 상자",
                        "siege chest -94 89 -173 red 60 빨강 상자",
                        "siege chest -99 89 -193 blue 60 파랑 상자",
                        "siege spawn -95 89 -170 red",
                        "siege spawn -96 89 -194 blue"
                };
                for (final String commandLine : commands) {
                    Bukkit.getServer().dispatchCommand(commandSender, commandLine);
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean checkIfPlayer(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(SiegeChatColors.ERROR_CHAT_COLOR + "This command can only be executed by a player.");
            return false;
        }
        return true;
    }

    private Teams parseTeam(CommandSender commandSender, String teamNameToParse) {
        switch (teamNameToParse) {
            case "neutral":
                return Teams.NEUTRAL;
            case "red":
                return Teams.RED;
            case "blue":
                return Teams.BLUE;
        }
        commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A team named %s does not exist.", teamNameToParse));
        return null;
    }

    private Location parseCoordinate(CommandSender commandSender, String[] coordinateToParse) {
        if (!checkIfPlayer(commandSender)) {
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
            Bukkit.getLogger().warning(SiegeChatColors.ERROR_CHAT_COLOR + "Coordinate parse failed.");
            return null;
        }
        return location;
    }
}