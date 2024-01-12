package knockknockp.siegeplugin.Siege;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CommandSiege implements CommandExecutor, TabCompleter {
    private final SiegeManager siegeManager;

    public CommandSiege(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    private static List<String> getAllOnlinePlayersNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private static List<String> buildLocationTab(int length, Location location, int ...args) {
        if (length == args[0]) {
            return Collections.singletonList(String.valueOf(location.getBlockX()));
        } else if (length == args[1]) {
            return Collections.singletonList(String.valueOf(location.getBlockY()));
        } else if (length == args[2]) {
            return Collections.singletonList(String.valueOf(location.getBlockZ()));
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "kit": {
                    if (args.length == 2) {
                        return Arrays.asList("list", "create", "edit", "delete", "get", "assigner");
                    } else if (args.length == 3) {
                        switch (args[1]) {
                            case "create":
                            case "edit":
                            case "delete":
                            case "get":
                            case "assigner":
                                return new ArrayList<>(siegeManager.kits.keySet());
                        }
                    }
                    return null;
                }
                case "team": {
                    if (args.length == 2) {
                        return getAllOnlinePlayersNames();
                    } else if (args.length == 3) {
                        return Arrays.asList("red", "blue");
                    }
                    return null;
                }
                case "assigner": {
                    if (args.length == 2) {
                        return Arrays.asList("red", "blue");
                    }
                    return null;
                }
                case "base": {
                    if (args.length == 8) {
                        return Arrays.asList("red", "blue");
                    }

                    Location location = ((Player)(commandSender)).getLocation();
                    List<String> coordinates1 = buildLocationTab(args.length, location, 2, 3, 4);
                    if (coordinates1 != null) {
                        return coordinates1;
                    }

                    return buildLocationTab(args.length, location, 5, 6, 7);
                }
                case "wool":
                case "deposit":
                case "spawn": {
                    Location location = ((Player)(commandSender)).getLocation();
                    List<String> coordinates = buildLocationTab(args.length, location, 2, 3, 4);
                    if (coordinates != null) {
                        return coordinates;
                    }

                    if (args.length == 5) {
                        return Arrays.asList("red", "blue");
                    }

                    return null;
                }
                case "team_chest": {
                    Location location = ((Player)(commandSender)).getLocation();
                    List<String> coordinates = buildLocationTab(args.length, location, 2, 3, 4);
                    if (coordinates != null) {
                        return coordinates;
                    }

                    if (args.length == 5) {
                        return Arrays.asList("neutral", "red", "blue");
                    }

                    return null;
                }
                case "resetting_chest":
                case "unregister_chest": {
                    return buildLocationTab(args.length, ((Player)(commandSender)).getLocation(), 2, 3, 4);
                }
                case "permit":
                case "forbid":
                    return getAllOnlinePlayersNames();
                case "gamemode":
                    if (args.length == 2) {
                        return Arrays.asList("survival", "creative", "adventure", "spectator");
                    }
                    return null;
            }
        }

        return Arrays.asList("wand", "kit", "team", "assigner", "base", "wool", "deposit", "spawn",
            "team_chest", "resetting_chest", "unregister_chest", "time", "start", "stop", "reset", "full_reset", "permit", "forbid",
            "gamemode", "version", "stats");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        switch (args[0]) {
            case "wand": {
                if (!checkIfPlayer(commandSender)) {
                    return true;
                }

                Player player = (Player)(commandSender);
                player.getInventory().addItem(Wand.wandItem);
                siegeManager.wandListener.addOrGetWand(player);
                siegeManager.wandListener.wands.get(player).highlightRegisteredChests();
                break;
            }
            case "kit": {
                if (args.length < 2) {
                    return false;
                }

                switch (args[1]) {
                    case "list": {
                        StringBuilder stringBuilder = new StringBuilder(SiegeChatColors.SUCCESS_CHAT_COLOR + "List of all kits:\n");
                        for (Kit kit : siegeManager.kits.values()) {
                            stringBuilder.append(String.format("%s\n", kit.getName()));
                        }
                        commandSender.sendMessage(stringBuilder.toString());
                        break;
                    }
                    case "create": {
                        if (args.length < 3) {
                            return false;
                        }

                        String kitName = args[2];
                        if (!siegeManager.createKit(kitName)) {
                            commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A kit of name %s already exists!", kitName));
                            return true;
                        }

                        commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Created a kit of name %s.", kitName));
                        break;
                    }
                    case "edit": {
                        if (args.length < 3) {
                            return false;
                        }

                        String kitName = args[2];
                        try {
                            siegeManager.editKit((Player)(commandSender), kitName);
                        } catch (KitNotFoundException kitNotFoundException) {
                            commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A kit of name %s does not exist!", kitName));
                        } catch(KitBeingEditedException kitBeingEditedException) {
                            //I did not expect this plugin to get this big; If I knew this would happen I would have written better code than this shit.
                            commandSender.sendMessage(SiegeChatColors.ERROR_CHAT_COLOR + "Only one person at time can edit the kit!");
                        }
                        break;
                    }
                    case "delete": {
                        if (args.length < 3) {
                            return false;
                        }

                        String kitName = args[2];
                        if (!siegeManager.deleteKit(kitName)) {
                            commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "Kit of name %s does not exist!", kitName));
                            return true;
                        }

                        commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Kit %s has been deleted.", kitName));
                        break;
                    }
                    case "get": {
                        if (args.length < 3) {
                            return false;
                        }

                        String kitName = args[2];
                        try {
                            siegeManager.giveKit((Player) (commandSender), kitName);
                        } catch (KitNotFoundException kitNotFoundException) {
                            commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A kit of name %s does not exist!", kitName));
                        }
                        break;
                    }
                    case "assigner": {
                        if (args.length < 3) {
                            return false;
                        }

                        String kitName = args[2];
                        try {
                            siegeManager.addKitAssigner(kitName, ((Player)(commandSender)).getLocation());
                        } catch (KitNotFoundException kitNotFoundException) {
                            commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A kit of name %s does not exist!", kitName));
                        }
                        break;
                    }
                }
                break;
            }
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

                siegeManager.addTeamAssigner(parsedTeam, ((Player)(commandSender)).getLocation());
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

                siegeManager.setWool(parsedTeam, location);
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set coordinate as team %s's wool.", parsedTeam));
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

                siegeManager.setDeposit(parsedTeam, location);
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set coordinate as team %s's deposit.", parsedTeam));
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

                siegeManager.setSpawn(parsedTeam, location);
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set %s as team %s's spawn position.",
                    LocationExtensions.toBlockTriple(location), parsedTeam));
                break;
            }
            case "team_chest": {
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

                Block block = location.getBlock();
                if (block.getType() != Material.CHEST) {
                    commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A chest was not found at coordinate %s.", LocationExtensions.toBlockTriple(location)));
                    return true;
                }

                siegeManager.setChestTeam(location, parsedTeam);
                commandSender.sendMessage(String.format(
                        SiegeChatColors.SUCCESS_CHAT_COLOR + "Registered chest at %s for team %s.",
                        LocationExtensions.toBlockTriple(location),
                        parsedTeam));
                break;
            }
            case "resetting_chest": {
                if (args.length < 6) {
                    return false;
                }

                Location location = tryParseCoordinate(commandSender, args[1], args[2], args[3]);
                if (location == null) {
                    return false;
                }

                Block block = location.getBlock();
                if (block.getType() != Material.CHEST) {
                    commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A chest was not found at coordinate %s.", LocationExtensions.toBlockTriple(location)));
                    return true;
                }

                long coolDown;
                try {
                    coolDown = Long.parseLong(args[4]);
                } catch (Exception exception) {
                    Bukkit.getLogger().warning(SiegeChatColors.ERROR_CHAT_COLOR + "Failed to parse cool down.");
                    return false;
                }

                if (coolDown <= -1) {
                    return false;
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 5; i < (args.length - 1); ++i) {
                    stringBuilder.append(args[i]);
                    stringBuilder.append(' ');
                }
                stringBuilder.append(args[(args.length - 1)]);
                String parsedLabel = stringBuilder.toString();

                siegeManager.setResettingChest(location,
                    ((Chest)(block.getState())).getBlockInventory().getContents(),
                    coolDown,
                    parsedLabel);

                commandSender.sendMessage(String.format(
                    SiegeChatColors.SUCCESS_CHAT_COLOR + "Set resetting chest at %s with a cool down of %d ticks, a label of %s.",
                    LocationExtensions.toBlockTriple(location),
                    coolDown,
                    parsedLabel));
                break;
            }
            case "unregister_chest": {
                if (args.length < 4) {
                    return false;
                }

                Location location = parseCoordinate(commandSender, new String[] { args[1], args[2], args[3] });
                if (location == null) {
                    return false;
                }

                if (siegeManager.unregisterChest(location)) {
                    commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Unregistered chest at %s", LocationExtensions.toBlockTriple(location)));
                    return true;
                } else {
                    commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "Could not find chest registered at %s", LocationExtensions.toBlockTriple(location)));
                    return false;
                }
            }
            case "time": {
                if (args.length < 2) {
                    return false;
                }

                int seconds = -1;
                try {
                    seconds = Integer.parseInt(args[1]);
                } catch (Exception exception) {
                    commandSender.sendMessage(SiegeChatColors.ERROR_CHAT_COLOR + "Failed to parse seconds.");
                }

                if (seconds <= -1) {
                    return false;
                }

                siegeManager.setTimeLimit(seconds);
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Set the time limit to %d seconds", seconds));
                break;
            }
            case "start": {
                siegeManager.start(commandSender);
                break;
            }
            case "stop": {
                siegeManager.stop();
                commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Stopped the mini game.");
                break;
            }
            case "reset": {
                siegeManager.reset();
                commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Stopped and resetted the mini game.");
                break;
            }
            case "full_reset": {
                siegeManager.fullReset();
                commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Stopped and fully resetted the mini game.");
                break;
            }
            case "permit": {
                if (args.length < 2) {
                    return false;
                }

                Player player = Bukkit.getServer().getPlayer(args[1]);
                if (player == null) {
                    commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A player of name %s does not exist."));
                    return true;
                }

                siegeManager.permitPlayer(player);
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Permitted player %s from using this command.", player.getName()));
                break;
            }
            case "forbid": {
                if (args.length < 2) {
                    return false;
                }

                Player player = Bukkit.getServer().getPlayer(args[1]);
                if (player == null) {
                    commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "A player of name %s does not exist."));
                    return true;
                }

                siegeManager.forbidPlayer(player);
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "Forbade player %s from using this command.", player.getName()));
                break;
            }
            case "gamemode": {
                if (args.length < 2) {
                    return false;
                }

                if (!checkIfPlayer(commandSender)) {
                    return true;
                }

                GameMode gameMode;
                switch (args[1]) {
                    case "survival":
                        gameMode = GameMode.SURVIVAL;
                        break;
                    case "creative":
                        gameMode = GameMode.CREATIVE;
                        break;
                    case "adventure":
                        gameMode = GameMode.ADVENTURE;
                        break;
                    case "spectator":
                        gameMode = GameMode.SPECTATOR;
                        break;
                    default:
                        return false;
                }

                ((Player)(commandSender)).setGameMode(gameMode);
                break;
            }
            case "version": {
                PluginDescriptionFile pluginDescriptionFile = siegeManager.javaPlugin.getDescription();
                commandSender.sendMessage(String.format(SiegeChatColors.SUCCESS_CHAT_COLOR + "SiegePlugin: %s\n" +
                        "API: %s\n" +
                        "Made with love, headache, men kissing, and spaghetti noodles.",
                    pluginDescriptionFile.getVersion(), pluginDescriptionFile.getAPIVersion()));
                break;
            }
            case "stats": {
                int totalPlayers = siegeManager.players.size(), redPlayers = 0, bluePlayers = 0;
                for (TeamPlayer teamPlayer : siegeManager.players.values()) {
                    if (teamPlayer.team == Teams.RED) {
                        ++redPlayers;
                    } else {
                        ++bluePlayers;
                    }
                }

                int totalAssigners = siegeManager.assigners.size();
                List<TeamAssigner> teamAssigners = new ArrayList<>();
                List<KitAssigner> kitAssigners = new ArrayList<>();
                for (Assigner assigner : siegeManager.assigners) {
                    if (assigner instanceof TeamAssigner) {
                        teamAssigners.add((TeamAssigner) (assigner));
                    } else {
                        kitAssigners.add((KitAssigner) (assigner));
                    }
                }

                int kitAssignersSize = kitAssigners.size(), redAssigners = 0, blueAssigners = 0;
                for (TeamAssigner teamAssigner : teamAssigners) {
                    Teams assignerTeam = teamAssigner.getTeam();
                    if (assignerTeam == Teams.RED) {
                        ++redAssigners;
                    } else if (assignerTeam == Teams.BLUE) {
                        ++blueAssigners;
                    }
                }

                int totalChests = siegeManager.registeredChests.size(), neutralChests = 0, redChests = 0, blueChests = 0;
                for (RegisteredChest registeredChest : siegeManager.registeredChests.values()) {
                    Teams team = registeredChest.getTeam();
                    if (team == Teams.NEUTRAL) {
                        ++neutralChests;
                    } else if (team == Teams.RED) {
                        ++redChests;
                    } else {
                        ++blueChests;
                    }
                }

                commandSender.sendMessage(String.format(ChatColor.GOLD + "Siege Mini Game Statistics:\n" +
                        "    Players Assigned:\n" +
                        ChatColor.LIGHT_PURPLE + "        Total: %d\n" +
                        Teams.RED.toChatColor() + "        Red: %d\n" +
                        Teams.BLUE.toChatColor() + "        Blue: %d\n" +
                        ChatColor.GOLD + "    Assigners:\n" +
                        ChatColor.DARK_PURPLE + "        Total: %d\n" +
                        Teams.RED.toChatColor() + "        Red: %d\n" +
                        Teams.BLUE.toChatColor() + "        Blue: %d\n" +
                        ChatColor.DARK_GRAY + "        Kit: %d\n" +
                        ChatColor.GOLD + "    Registered Chests:\n" +
                        ChatColor.DARK_PURPLE + "        Total: %d\n" +
                        Teams.NEUTRAL.toChatColor() + "        Neutral: %d\n" +
                        Teams.RED.toChatColor() + "        Red: %d\n" +
                        Teams.BLUE.toChatColor() + "        Blue: %d\n", totalPlayers, redPlayers, bluePlayers,
                    totalAssigners, redAssigners, blueAssigners, kitAssignersSize,
                    totalChests, neutralChests, redChests, blueChests));
                break;
            }
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