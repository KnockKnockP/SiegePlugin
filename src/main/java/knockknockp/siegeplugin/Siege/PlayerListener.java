package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerListener implements Listener {
    private final SiegeManager siegeManager;

    public PlayerListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        TeamPlayer teamPlayer = siegeManager.players.get(playerRespawnEvent.getPlayer());
        if (teamPlayer == null) {
            return;
        }

        teamPlayer.player.setGameMode(GameMode.SPECTATOR);

        int respawnTicks = 100;
        teamPlayer.player.sendTitle("리스폰 대기중", "", 0, respawnTicks, 0);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(siegeManager.javaPlugin, () ->
                Bukkit.getPluginManager().callEvent(new PlayerRespawnCoolDownDoneEvent(teamPlayer.player)),
            respawnTicks);
    }

    @EventHandler
    public void onPlayerRespawnCoolDownDoneEvent(PlayerRespawnCoolDownDoneEvent playerRespawnCoolDownDoneEvent) {
        Player player = playerRespawnCoolDownDoneEvent.getPlayer();
        TeamPlayer teamPlayer = siegeManager.players.get(player);

        player.teleport(siegeManager.teams.get(siegeManager.players.get(player).team).spawn);
        player.setGameMode(GameMode.SURVIVAL);

        player.getInventory().clear();
        if (teamPlayer.kit != null) {
            teamPlayer.kit.giveTo(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        TeamPlayer teamPlayer = siegeManager.players.get(playerQuitEvent.getPlayer());
        if (teamPlayer == null) {
            return;
        }

        siegeManager.teams.get(teamPlayer.team).team.removeEntry(teamPlayer.player.getName());
        siegeManager.players.remove(teamPlayer.player);

        Bukkit.getLogger().info("Handled player quit.");
    }
}