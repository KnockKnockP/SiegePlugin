package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public final class ChestListener implements Listener {
    private static class Commons {
        TeamPlayer teamPlayer;
        RegisteredChest registeredChest;
        boolean hasPermission, isResettingChest;
    }

    private final SiegeManager siegeManager;

    public ChestListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        Location location = blockBreakEvent.getBlock().getLocation();

        if (!siegeManager.isGameRunning) {
            Player player = blockBreakEvent.getPlayer();
            if (player.hasPermission("siege.management")) {
                if (siegeManager.unregisterChest(location)) {
                    player.sendMessage("The broken chest has been unregistered.");
                }
                return;
            }
            return;
        }

        RegisteredChest registeredChest = siegeManager.registeredChests.get(location);
        if (registeredChest == null) {
            return;
        }

        blockBreakEvent.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        Commons commons = new Commons();
        if (!getCommons(inventoryClickEvent, commons)) {
            return;
        }

        Inventory clickedInventory = inventoryClickEvent.getClickedInventory();
        InventoryView inventoryView = inventoryClickEvent.getView();
        if (clickedInventory == inventoryView.getTopInventory()) {
            if (!commons.hasPermission) {
                inventoryClickEvent.setCancelled(true);
                return;
            }

            if (commons.isResettingChest) {
                switch (inventoryClickEvent.getAction()) {
                    case PLACE_ONE:
                    case PLACE_SOME:
                    case PLACE_ALL:
                    case HOTBAR_SWAP:
                        inventoryClickEvent.setCancelled(true);
                        break;
                }
            }
        } else if (clickedInventory == inventoryView.getBottomInventory()) {
            if (!commons.hasPermission && inventoryClickEvent.getClick() == ClickType.DOUBLE_CLICK) {
                inventoryClickEvent.setCancelled(true);
                return;
            }

            if (inventoryClickEvent.isShiftClick() && (!commons.hasPermission || commons.isResettingChest)) {
                inventoryClickEvent.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent inventoryDragEvent) {
        Commons commons = new Commons();
        if (!getCommons(inventoryDragEvent, commons)) {
            return;
        }

        if (inventoryDragEvent.getInventory() != inventoryDragEvent.getView().getTopInventory()) {
            return;
        }

        if (!commons.hasPermission || commons.isResettingChest) {
            inventoryDragEvent.setCancelled(true);
        }
    }

    private boolean getCommons(InventoryInteractEvent inventoryInteractEvent, Commons commons) {
        if (!siegeManager.isGameRunning) {
            return false;
        }

        commons.teamPlayer = siegeManager.players.get(Bukkit.getServer().getPlayer(inventoryInteractEvent.getWhoClicked().getName()));
        if (commons.teamPlayer == null) {
            inventoryInteractEvent.setCancelled(true);
            return false;
        }

        commons.registeredChest = findChestFromInventory(inventoryInteractEvent.getView().getTopInventory());
        if (commons.registeredChest == null) {
            return false;
        }

        Teams chestTeam = commons.registeredChest.getTeam();
        commons.hasPermission = ((chestTeam == commons.teamPlayer.team) || (chestTeam == Teams.NEUTRAL));
        commons.isResettingChest = (commons.registeredChest.resettingChest != null);
        return true;
    }

    private RegisteredChest findChestFromInventory(Inventory inventory) {
        for (RegisteredChest registeredChest : siegeManager.registeredChests.values()) {
            if (registeredChest.getLocation().equals(inventory.getLocation())) {
                return registeredChest;
            }
        }
        return null;
    }
}