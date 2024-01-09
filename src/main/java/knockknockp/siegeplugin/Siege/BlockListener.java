package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockListener implements Listener {
    private final SiegeManager siegeManager;

    public BlockListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        Location location = blockPlaceEvent.getBlockPlaced().getLocation();
        if (!siegeManager.modifiedBlocks.containsKey(location)) {
            siegeManager.modifiedBlocks.put(location, new BlockModification(blockPlaceEvent.getBlockReplacedState()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        Location location = blockBreakEvent.getBlock().getLocation();
        if (!siegeManager.modifiedBlocks.containsKey(location)) {
            siegeManager.modifiedBlocks.put(location, new BlockModification(blockBreakEvent.getBlock().getState()));
        }
    }
}