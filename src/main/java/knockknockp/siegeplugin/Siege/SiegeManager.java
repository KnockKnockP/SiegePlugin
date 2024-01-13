package knockknockp.siegeplugin.Siege;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class SiegeManager {
    public final JavaPlugin javaPlugin;

    public WandListener wandListener;

    private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    private final Scoreboard scoreboard;
    {
        assert (scoreboardManager != null);
        scoreboard = scoreboardManager.getNewScoreboard();
    }
    private final Objective scoreObjective = scoreboard.registerNewObjective("scoreObjective", Criteria.DUMMY, "Scores");
    private final Score redScore = scoreObjective.getScore(Teams.RED.toString()),
        blueScore = scoreObjective.getScore(Teams.BLUE.toString());

    public Team redEntitiesTeam = scoreboard.registerNewTeam("RedEntities"),
        blueEntitiesTeam = scoreboard.registerNewTeam("BlueEntities");

    public Map<Player, TeamPlayer> players = new HashMap<>();
    public final Map<Teams, SiegeTeam> teams = new HashMap<>();

    public final Map<Location, RegisteredChest> registeredChests = new HashMap<>();

    public Map<Location, BlockModification> modifiedBlocks = new HashMap<>();

    public boolean isGameRunning = false;
    private int timeLimitInSeconds = (60 * 10), timeLeftInSeconds, timeCountTaskId = -1;
    private final BossBar timeBar = Bukkit.createBossBar("남은 시간", BarColor.WHITE, BarStyle.SEGMENTED_20);

    public int respawnTimeInSeconds = 5;

    public Map<String, Kit> kits = new HashMap<>();
    public Map<Player, Kit> kitsBeingEdited = new HashMap<>();

    public List<Assigner> assigners = new ArrayList<>();

    private final List<Label> labels = new ArrayList<>();

    private final Map<UUID, PermissionAttachment> playersWithPermissions = new HashMap<>();

    public SiegeManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;

        SiegeTeam redTeam = new SiegeTeam(scoreboard.registerNewTeam(Teams.RED.toString())),
                  blueTeam = new SiegeTeam(scoreboard.registerNewTeam(Teams.BLUE.toString()));
        redTeam.team.setColor(Teams.RED.toChatColor());
        blueTeam.team.setColor(Teams.BLUE.toChatColor());

        redTeam.team.setAllowFriendlyFire(false);
        blueTeam.team.setAllowFriendlyFire(false);

        teams.put(Teams.RED, redTeam);
        teams.put(Teams.BLUE, blueTeam);

        redEntitiesTeam.setColor(Teams.RED.toChatColor());
        blueEntitiesTeam.setColor(Teams.BLUE.toChatColor());
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

        Bukkit.getPluginManager().callEvent(new TeamSettingsChangedEvent());
    }

    public void addTeamAssigner(Teams team, Location location) {
        assigners.add(new TeamAssigner(this, location, team));
    }

    public void setWool(Teams team, Location location) {
        Teams enemy = Teams.RED;
        if (team == Teams.RED) {
            enemy = Teams.BLUE;
        }

        List<Location> teamWools = teams.get(team).wools, enemyWools = teams.get(enemy).wools;
        enemyWools.remove(location);
        if (!teamWools.contains(location)) {
            teamWools.add(location);
        }

        Bukkit.getPluginManager().callEvent(new TeamSettingsChangedEvent());
    }

    public boolean unregisterWool(Location location) {
        boolean found = false;
        found:
        for (SiegeTeam siegeTeam : teams.values()) {
            for (Location wool : siegeTeam.wools) {
                if (wool.equals(location)) {
                    siegeTeam.wools.remove(wool);
                    found = true;
                    break found;
                }
            }
        }

        if (!found) {
            return false;
        }

        Bukkit.getPluginManager().callEvent(new TeamSettingsChangedEvent());
        return true;
    }

    public void setDeposit(Teams team, Location location) {
        teams.get(team).deposit = location;
        Bukkit.getPluginManager().callEvent(new TeamSettingsChangedEvent());
    }

    public void setSpawn(Teams team, Location location) {
        teams.get(team).spawn = location;
        Bukkit.getPluginManager().callEvent(new TeamSettingsChangedEvent());
    }

    private RegisteredChest addIfNonChest(Location location) {
        if (registeredChests.containsKey(location)) {
            return registeredChests.get(location);
        }

        RegisteredChest registeredChest = new RegisteredChest((Chest)(location.getBlock().getState()), Teams.NEUTRAL);
        registeredChests.put(location, registeredChest);
        Bukkit.getPluginManager().callEvent(new RegisteredChestListChangedEvent());
        return registeredChest;
    }

    public void setChestTeam(Location location, Teams team) {
        addIfNonChest(location).setTeam(team);
        Bukkit.getPluginManager().callEvent(new RegisteredChestListChangedEvent());
    }

    public void setResettingChest(Location location, ItemStack[] itemStacks, long coolDown, String label) {
        addIfNonChest(location).setResetting(itemStacks, coolDown, label);
        Bukkit.getPluginManager().callEvent(new RegisteredChestListChangedEvent());
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

        Bukkit.getPluginManager().callEvent(new RegisteredChestListChangedEvent());
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

    public void setRespawnTime(int seconds) {
        respawnTimeInSeconds = seconds;
    }

    public void permitPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (playersWithPermissions.get(uuid) == null) {
            PermissionAttachment permissionAttachment = player.addAttachment(javaPlugin);
            permissionAttachment.setPermission(SiegePermissions.siegeManagement, true);
            playersWithPermissions.put(uuid, permissionAttachment);
        }
    }

    public void forbidPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment permissionAttachment = playersWithPermissions.get(uuid);
        if (permissionAttachment == null) {
            return;
        }

        permissionAttachment.unsetPermission(SiegePermissions.siegeManagement);
        playersWithPermissions.remove(uuid);
    }

    public void start(CommandSender commandSender) {
        for (Teams team : teams.keySet()) {
            if (!teams.get(team).validate()) {
                commandSender.sendMessage(String.format(SiegeChatColors.ERROR_CHAT_COLOR + "Team %s failed to validate! Check all the values. Halting the start command.", team));
                return;
            }
        }

        reset();
        setup();
        isGameRunning = true;

        for (Entity entity : Objects.requireNonNull(teams.get(Teams.RED).base[0].getWorld()).getEntities()) {
            if (entity instanceof Item) {
                entity.remove();
            }
        }

        BukkitScheduler bukkitScheduler = Bukkit.getServer().getScheduler();
        for (RegisteredChest registeredChest : registeredChests.values()) {
            ResettingChest resettingChest = registeredChest.resettingChest;
            if (resettingChest == null) {
                continue;
            }

            resettingChest.start();
            resettingChest.taskId = bukkitScheduler.scheduleSyncRepeatingTask(javaPlugin, resettingChest::countSecond, 0, 20);
        }

        timeCountTaskId = bukkitScheduler.scheduleSyncRepeatingTask(javaPlugin, this::countSecond, 20, 20);

        for (TeamPlayer teamPlayer : players.values()) {
            Player player = teamPlayer.player;
            player.teleport(teams.get(teamPlayer.team).spawn);

            if (teamPlayer.kit != null) {
                teamPlayer.kit.giveTo(player);
            }

            timeBar.addPlayer(player);

            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(20);

            Teams enemy = Teams.RED;
            if (teamPlayer.team == Teams.RED) {
                enemy = Teams.BLUE;
            }
            player.sendTitle(enemy.toChatColor() + "상대 목표" + ChatColor.WHITE + "를 약탈해서",
                teamPlayer.team.toChatColor() + "본인 베이스" + ChatColor.WHITE + "에 설치하세요", 0, 60, 0);
        }

        commandSender.sendMessage(SiegeChatColors.SUCCESS_CHAT_COLOR + "Started the mini game.");
    }

    private void setup() {
        timeLeftInSeconds = timeLimitInSeconds;
        timeBar.setProgress(1);

        scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        redScore.setScore(0);
        blueScore.setScore(0);

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
            for (Location wool : siegeTeam.wools) {
                if (wool == null) {
                    continue;
                }

                wool.getBlock().setType(team.toWool());
                labels.add(new Label(team.toChatColor() + "목표", wool, true));
            }

            Location deposit = siegeTeam.deposit;
            if (deposit == null) {
                continue;
            }
            deposit.getBlock().setType(Material.AIR);
            labels.add(new Label(team.toChatColor() + "여기에 상대 목표를 설치", siegeTeam.deposit, true));
        }
    }

    public void reset() {
        stop();

        timeBar.removeAll();
        scoreObjective.setDisplaySlot(null);

        for (BlockModification blockModification : modifiedBlocks.values()) {
            blockModification.revert();
        }
        modifiedBlocks.clear();

        for (Label label : labels) {
            label.remove();
        }
        labels.clear();
    }

    public void fullReset() {
        reset();

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

    public void stop() {
        isGameRunning = false;
        stopCountingSeconds();

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
    }

    private void countSecond() {
        --timeLeftInSeconds;
        timeBar.setProgress((double)(timeLeftInSeconds) / timeLimitInSeconds);

        if (timeLeftInSeconds <= 0) {
            stopCountingSeconds();

            for (TeamPlayer teamPlayer : players.values()) {
                teamPlayer.player.sendTitle("타임 오버", "제한 시간을 초과했습니다.", 0, 60, 0);
            }

            Bukkit.getServer().broadcastMessage("Time over!");
            stop();
        }
    }

    private void stopCountingSeconds() {
        if (timeCountTaskId >= 0) {
            Bukkit.getServer().getScheduler().cancelTask(timeCountTaskId);
            timeCountTaskId = -1;
        }
    }

    public void incrementScore(Teams team) {
        Teams enemy = Teams.RED;
        if (team == Teams.RED) {
            redScore.setScore(redScore.getScore() + 1);
            enemy = Teams.BLUE;
        } else {
            blueScore.setScore(blueScore.getScore() + 1);
        }

        int enemyWools = teams.get(enemy).wools.size();
        if (redScore.getScore() == enemyWools) {
            win(Teams.RED);
        } else if (blueScore.getScore() == enemyWools) {
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
            stop();
        }
    }
}