package knockknockp.siegeplugin.Siege;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class KitListener implements Listener {
    private final SiegeManager siegeManager;

    public KitListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        Player player = (Player)(inventoryCloseEvent.getPlayer());
        Kit kit = siegeManager.kitsBeingEdited.get(player);
        if (kit == null) {
            return;
        }

        siegeManager.kitsBeingEdited.remove(player);
        kit.save();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
        Player player = playerRespawnEvent.getPlayer();
        player.getInventory().clear();
        siegeManager.players.get(player).kit.giveTo(player);
    }
}