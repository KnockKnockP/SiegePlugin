package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {
    private final SiegeManager siegeManager;

    public PlayerListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
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