package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class ChestListener implements Listener {
    private final SiegeManager siegeManager;

    public ChestListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        TeamPlayer teamPlayer = siegeManager.players.get(Bukkit.getServer().getPlayer(inventoryClickEvent.getWhoClicked().getName()));
        if (teamPlayer == null) {
            inventoryClickEvent.setCancelled(true);
            return;
        }

        for (ResettingChest resettingChest : siegeManager.chests) {
            if (resettingChest.chest.getLocation().equals(inventoryClickEvent.getInventory().getLocation())) {
                if ((resettingChest.team != teamPlayer.team) && (resettingChest.team != Teams.NEUTRAL)) {
                    inventoryClickEvent.setCancelled(true);
                    return;
                }
            }
        }
    }
}