package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
        if (blockPlaceEvent.isCancelled()) {
            return;
        }

        if (!siegeManager.isGameRunning) {
            return;
        }

        Player player = blockPlaceEvent.getPlayer();
        if (siegeManager.players.get(player) == null) {
            return;
        }

        Location location = blockPlaceEvent.getBlockPlaced().getLocation();
        if (!siegeManager.modifiedBlocks.containsKey(location)) {
            siegeManager.modifiedBlocks.put(location, new BlockModification(blockPlaceEvent.getBlockReplacedState()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        if (blockBreakEvent.isCancelled()) {
            return;
        }

        if (!siegeManager.isGameRunning) {
            return;
        }

        Player player = blockBreakEvent.getPlayer();
        if (siegeManager.players.get(player) == null) {
            return;
        }

        Block brokenBlock = blockBreakEvent.getBlock();
        Location location = brokenBlock.getLocation();
        if (!siegeManager.modifiedBlocks.containsKey(location)) {
            siegeManager.modifiedBlocks.put(location, new BlockModification(brokenBlock.getState()));
        }
    }
}