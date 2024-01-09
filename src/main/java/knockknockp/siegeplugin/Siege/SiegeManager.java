package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class SiegeManager {
    private final JavaPlugin javaPlugin;

    private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private final Scoreboard scoreboard;
    {
        assert (scoreboardManager != null);
        scoreboard = scoreboardManager.getNewScoreboard();
    }

    public Map<Player, TeamPlayer> players = new HashMap<>();
    public final Map<Teams, SiegeTeam> teams = new HashMap<>();
    public List<Assigner> assigners = new ArrayList<>();

    public final List<ResettingChest> chests = new ArrayList<>();

    private final String redString = Teams.RED.toString(), blueString = Teams.BLUE.toString();
    private final Objective scoreObjective = scoreboard.registerNewObjective("scoreObjective", Criteria.DUMMY, "Score");
    private final Score redScore = scoreObjective.getScore(redString), blueScore = scoreObjective.getScore(blueString);

    public Map<Location, BlockModification> modifiedBlocks = new HashMap<>();

    public boolean isGameRunning = false;

    public SiegeManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;

        SiegeTeam redTeam = new SiegeTeam(scoreboard.registerNewTeam(redString)), blueTeam = new SiegeTeam(scoreboard.registerNewTeam(blueString));
        redTeam.team.setPrefix(Teams.RED.toChatColor() + redString + ChatColor.WHITE + " ");
        blueTeam.team.setPrefix(Teams.BLUE.toChatColor() + blueString + ChatColor.WHITE + " ");

        redTeam.team.setAllowFriendlyFire(false);
        blueTeam.team.setAllowFriendlyFire(false);

        teams.put(Teams.RED, redTeam);
        teams.put(Teams.BLUE, blueTeam);
        reset();
    }

    public void assignPlayerToTeam(Player player, Teams team) {
        player.setScoreboard(scoreboard);

        teams.get(team).team.addEntry(player.getName());
        if (!players.containsKey(player)) {
            TeamPlayer teamPlayer = new TeamPlayer(player, team);
            players.put(player, teamPlayer);
        } else {
            players.get(player).team = team;
        }

        player.sendMessage(String.format(team.toChatColor() + "You are now part of team %s.", team));
    }

    public void setBase(Teams team, Location location1, Location location2) {
        SiegeTeam siegeTeam = teams.get(team);
        siegeTeam.base[0] = location1;
        siegeTeam.base[1] = location2;

        Bukkit.getPluginManager().callEvent(new BaseSetEvent(team));
    }

    public void addAssigner(Teams team, Location location) {
        assigners.add(new Assigner(this, team, location));
    }

    public void setWool(Teams team, int index, Location location) {
        teams.get(team).wools[index] = location;
        Bukkit.getPluginManager().callEvent(new WoolSetEvent(team));
    }

    public void setDeposit(Teams team, Location location) {
        teams.get(team).deposit = location;
        Bukkit.getPluginManager().callEvent(new DepositSetEvent(team));
    }

    public void setSpawn(Teams team, Location location) {
        teams.get(team).spawn = location;
        Bukkit.getPluginManager().callEvent(new SpawnSetEvent(team));
    }

    private void reset() {
        redScore.setScore(0);
        blueScore.setScore(0);
        scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (TeamPlayer teamPlayer : players.values()) {
            Player player = teamPlayer.player;

            player.setBedSpawnLocation(teams.get(teamPlayer.team).spawn, true);
            player.getInventory().clear();

            for (PotionEffect potionEffect : player.getActivePotionEffects()) {
                player.removePotionEffect(potionEffect.getType());
            }
        }

        for (BlockModification blockModification : modifiedBlocks.values()) {
            blockModification.revert();
        }

        for (Teams team : teams.keySet()) {
            SiegeTeam siegeTeam = teams.get(team);
            for (Location wools : siegeTeam.wools) {
                if (wools == null) {
                    continue;
                }

                wools.getBlock().setType(team.toWool());
            }

            Location deposit = siegeTeam.deposit;
            if (deposit == null) {
                continue;
            }
            deposit.getBlock().setType(Material.AIR);
        }

        BukkitScheduler bukkitScheduler = Bukkit.getServer().getScheduler();
        for (ResettingChest resettingChest : chests) {
            resettingChest.stop();
            if (resettingChest.taskId >= 0) {
                bukkitScheduler.cancelTask(resettingChest.taskId);
                resettingChest.taskId = -1;
            }
        }
    }

    public void fullReset() {
        stop();

        scoreObjective.setDisplaySlot(null);
        for (TeamPlayer teamPlayer : players.values()) {
            teams.get(teamPlayer.team).team.removeEntry(teamPlayer.player.getName());
        }

        for (Assigner assigner : assigners) {
            Bukkit.getLogger().info(assigner.armorStand.getCustomName());
            assigner.remove();
        }
        assigners.clear();

        players.clear();
        chests.clear();
    }

    public void start(CommandSender commandSender) {
        for (Teams team : teams.keySet()) {
            if (!teams.get(team).validate()) {
                commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "Team %s failed to validate! Check all the values. Halting the start command.", team));
                return;
            }
        }

        reset();
        isGameRunning = true;

        BukkitScheduler bukkitScheduler = Bukkit.getServer().getScheduler();
        for (ResettingChest resettingChest : chests) {
            resettingChest.start();
            resettingChest.taskId = bukkitScheduler.scheduleSyncRepeatingTask(javaPlugin, resettingChest::tick, 0, 1);
        }

        for (TeamPlayer teamPlayer : players.values()) {
            teamPlayer.player.teleport(teams.get(teamPlayer.team).spawn);
        }

        commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Started the mini game.");
    }

    public void stop() {
        isGameRunning = false;
        reset();
    }

    public void incrementScore(Teams team) {
        if (team == Teams.RED) {
            redScore.setScore(redScore.getScore() + 1);
        } else {
            blueScore.setScore(blueScore.getScore() + 1);
        }

        if (redScore.getScore() == 3) {
            win(Teams.RED);
        } else if (blueScore.getScore() == 3) {
            win(Teams.BLUE);
        }
    }

    private void win(Teams team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int red = redScore.getScore(), blue = blueScore.getScore();

            String teamName = "빨강팀";
            if (team == Teams.BLUE) {
                teamName = "청팀";
            }
            player.sendTitle(team.toChatColor() + teamName + " 승리", String.format(ChatColor.YELLOW + "%d - %d", Math.max(red, blue), Math.min(red, blue)), 2, 70, 10);
        }
    }
}