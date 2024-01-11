package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class ProtectedAreaListener implements Listener {
    private final SiegeManager siegeManager;

    private static boolean isInRange(int number, int a, int b) {
        return ((number >= Math.min(a, b)) && (number <= Math.max(a, b)));
    }

    public ProtectedAreaListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        TeamPlayer teamPlayer = siegeManager.players.get(blockPlaceEvent.getPlayer());
        if (teamPlayer == null) {
            return;
        }

        if (isInRange(blockPlaceEvent.getBlock().getLocation())) {
            blockPlaceEvent.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        TeamPlayer teamPlayer = siegeManager.players.get(blockBreakEvent.getPlayer());
        if (teamPlayer == null) {
            return;
        }

        if (isInRange(blockBreakEvent.getBlock().getLocation())) {
            blockBreakEvent.setCancelled(true);
        }
    }

    private boolean isInRange(Location block) {
        Location[] redBase = siegeManager.teams.get(Teams.RED).base;
        boolean isInRedBase;
        if ((redBase == null) || (redBase[0] == null) || (redBase[1] == null)) {
            isInRedBase = false;
        } else {
            isInRedBase = (isInRange(block.getBlockX(), redBase[0].getBlockX(), redBase[1].getBlockX()) &&
                           isInRange(block.getBlockY(), redBase[0].getBlockY(), redBase[1].getBlockY()) &&
                           isInRange(block.getBlockZ(), redBase[0].getBlockZ(), redBase[1].getBlockZ()));
        }

        //Red spy is in the base?
        Location[] blueBase = siegeManager.teams.get(Teams.BLUE).base;
        boolean isInBlueBase;
        if ((blueBase == null) || (blueBase[0] == null) || (blueBase[1] == null)) {
            isInBlueBase = false;
        } else {
            isInBlueBase = (isInRange(block.getBlockX(), blueBase[0].getBlockX(), blueBase[1].getBlockX()) &&
                            isInRange(block.getBlockY(), blueBase[0].getBlockY(), blueBase[1].getBlockY()) &&
                            isInRange(block.getBlockZ(), blueBase[0].getBlockZ(), blueBase[1].getBlockZ()));
        }
        return (isInRedBase || isInBlueBase);
    }
}