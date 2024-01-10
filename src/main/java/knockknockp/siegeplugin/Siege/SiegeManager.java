package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class SiegeManager {
    public final JavaPlugin javaPlugin;

    private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private final Scoreboard scoreboard;
    {
        assert (scoreboardManager != null);
        scoreboard = scoreboardManager.getNewScoreboard();
    }

    public Map<Player, TeamPlayer> players = new HashMap<>();
    public final Map<Teams, SiegeTeam> teams = new HashMap<>();

    public final Map<Location, RegisteredChest> registeredChests = new HashMap<>();

    private final String redString = Teams.RED.toString(), blueString = Teams.BLUE.toString();
    private final Objective scoreObjective = scoreboard.registerNewObjective("scoreObjective", Criteria.DUMMY, "Score");
    private final Score redScore = scoreObjective.getScore(redString), blueScore = scoreObjective.getScore(blueString);

    public Map<Location, BlockModification> modifiedBlocks = new HashMap<>();

    public boolean isGameRunning = false;
    private int timeLimitInSeconds = (60 * 10), timeLeftInSeconds, timeCountTaskId = -1;
    private final BossBar timeBar = Bukkit.createBossBar("남은 시간", BarColor.WHITE, BarStyle.SEGMENTED_20);

    public Map<String, Kit> kits = new HashMap<>();
    public Map<Player, Kit> kitsBeingEdited = new HashMap<>();

    public List<Assigner> assigners = new ArrayList<>();

    public SiegeManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;

        SiegeTeam redTeam = new SiegeTeam(scoreboard.registerNewTeam(redString)), blueTeam = new SiegeTeam(scoreboard.registerNewTeam(blueString));
        redTeam.team.setColor(Teams.RED.toChatColor());
        blueTeam.team.setColor(Teams.BLUE.toChatColor());

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

    public void addTeamAssigner(Teams team, Location location) {
        assigners.add(new TeamAssigner(this, location, team));
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

    private RegisteredChest addIfNonChest(Location location) {
        if (registeredChests.containsKey(location)) {
            return registeredChests.get(location);
        }

        RegisteredChest registeredChest = new RegisteredChest((Chest)(location.getBlock().getState()), Teams.NEUTRAL);
        registeredChests.put(location, registeredChest);
        return registeredChest;
    }

    public void setChestTeam(Location location, Teams team) {
        addIfNonChest(location).setTeam(team);
    }

    public void setResettingChest(Location location, ItemStack[] itemStacks, long coolDown, String label) {
        addIfNonChest(location).setResetting(itemStacks, coolDown, label);
    }

    public boolean unregisterChest(Location location) {
        RegisteredChest registeredChest = registeredChests.remove(location);
        if (registeredChest == null) {
            return false;
        }

        ResettingChest resettingChest = registeredChest.resettingChest;
        if (resettingChest != null) {
            resettingChest.stop();
        }
        return true;
    }

    public boolean createKit(String name) {
        if (kits.containsKey(name)) {
            return false;
        }

        kits.put(name, new Kit(name));
        return true;
    }

    public void editKit(Player player, String name) throws KitNotFoundException, KitBeingEditedException {
        Kit kit = kits.get(name);
        if (kit == null) {
            throw new KitNotFoundException(String.format("Kit of name %s does not exist!", name));
        }

        if (kitsBeingEdited.get(player) != null) {
            throw new KitBeingEditedException(String.format("Kit of name %s is being edited.", name));
        }

        kitsBeingEdited.put(player, kit);
        kit.edit(player);
    }

    public boolean deleteKit(String name) {
        Kit kit = kits.get(name);
        if (kit == null) {
            return false;
        }

        kits.remove(name);
        for (Assigner assigner : assigners) {
            if (assigner instanceof KitAssigner) {
                if (((KitAssigner)(assigner)).getKit() == kit) {
                    assigners.remove(assigner);
                    break;
                }
            }
            assigner.remove();
        }
        return true;
    }

    public void giveKit(Player player, String name) throws KitNotFoundException {
        Kit kit = kits.get(name);
        if (kit == null) {
            throw new KitNotFoundException(String.format("Kit of name %s does not exist!", name));
        }

        kit.giveTo(player);
    }

    public void addKitAssigner(String name, Location location) throws KitNotFoundException {
        Kit kit = kits.get(name);
        if (kit == null) {
            throw new KitNotFoundException(String.format("Kit of name %s does not exist!", name));
        }

        assigners.add(new KitAssigner(this, location, kit));
    }

    public void setTimeLimit(int seconds) {
        timeLimitInSeconds = seconds;
    }

    private void reset() {
        redScore.setScore(0);
        blueScore.setScore(0);
        scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        timeLeftInSeconds = timeLimitInSeconds;
        timeBar.removeAll();
        timeBar.setProgress(1);

        for (TeamPlayer teamPlayer : players.values()) {
            Player player = teamPlayer.player;

            timeBar.addPlayer(player);

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
        for (RegisteredChest registeredChest : registeredChests.values()) {
            ResettingChest resettingChest = registeredChest.resettingChest;
            if (resettingChest == null) {
                continue;
            }

            resettingChest.stop();
            if (resettingChest.taskId >= 0) {
                bukkitScheduler.cancelTask(resettingChest.taskId);
                resettingChest.taskId = -1;
            }
        }

        stopCountingSeconds();
    }

    public void fullReset() {
        stop();

        scoreObjective.setDisplaySlot(null);
        timeBar.removeAll();

        for (TeamPlayer teamPlayer : players.values()) {
            teams.get(teamPlayer.team).team.removeEntry(teamPlayer.player.getName());
        }

        for (Assigner assigner : assigners) {
            assigner.remove();
        }
        assigners.clear();

        players.clear();
        registeredChests.clear();
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
        for (RegisteredChest registeredChest : registeredChests.values()) {
            ResettingChest resettingChest = registeredChest.resettingChest;
            if (resettingChest == null) {
                continue;
            }

            resettingChest.start();
            resettingChest.taskId = bukkitScheduler.scheduleSyncRepeatingTask(javaPlugin, resettingChest::tick, 0, 1);
        }

        timeCountTaskId = bukkitScheduler.scheduleSyncRepeatingTask(javaPlugin, this::countSecond, 20, 20);

        for (TeamPlayer teamPlayer : players.values()) {
            teamPlayer.player.teleport(teams.get(teamPlayer.team).spawn);

            if (teamPlayer.kit != null) {
                teamPlayer.kit.giveTo(teamPlayer.player);
            }
        }

        commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Started the mini game.");
    }

    public void stop() {
        isGameRunning = false;
        reset();
    }

    private void countSecond() {
        --timeLeftInSeconds;
        timeBar.setProgress((double)(timeLeftInSeconds) / timeLimitInSeconds);

        if (timeLeftInSeconds == 0) {
            stopCountingSeconds();

            for (TeamPlayer teamPlayer : players.values()) {
                teamPlayer.player.sendTitle("타임 오버", "제한 시간을 초과했습니다.", 0, 60, 0);
            }

            Bukkit.getServer().broadcastMessage("Time over!");
        }
    }

    private void stopCountingSeconds() {
        if (timeCountTaskId >= 0) {
            Bukkit.getServer().getScheduler().cancelTask(timeCountTaskId);
            timeCountTaskId = -1;
        }
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
            player.sendTitle(team.toChatColor() + teamName + " 승리", String.format(ChatColor.YELLOW + "%d - %d", Math.max(red, blue), Math.min(red, blue)), 5, 90, 5);
        }
    }
}