package knockknockp.siegeplugin.Siege;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class WandListener implements Listener {
    private final SiegeManager siegeManager;

    public final Map<Player, Wand> wands = new HashMap<>();

    public WandListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent playerItemHeldEvent) {
        Player player = playerItemHeldEvent.getPlayer();
        if (!player.hasPermission(SiegePermissions.siegeManagement)) {
            return;
        }

        Wand wand = wands.get(player);
        if (wand == null) {
            return;
        }

        ItemStack itemStack = player.getInventory().getItem(playerItemHeldEvent.getNewSlot());
        if ((itemStack == null) || !itemStack.isSimilar(Wand.wandItem)) {
            wand.unHighlightRegisteredChests();
        } else {
            wand.highlightRegisteredChests();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
        Player player = playerInteractEvent.getPlayer();
        if (!player.hasPermission(SiegePermissions.siegeManagement)) {
            return;
        }

        ItemStack eventItem = playerInteractEvent.getItem();
        if ((eventItem == null) || (!eventItem.isSimilar(Wand.wandItem))) {
            return;
        }

        Wand wand = addOrGetWand(player);

        Action action = playerInteractEvent.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            wand.setSelect(Objects.requireNonNull(playerInteractEvent.getClickedBlock()), 0);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            wand.setSelect(Objects.requireNonNull(playerInteractEvent.getClickedBlock()), 1);
        } else if (action == Action.RIGHT_CLICK_AIR) {
            updateDescription(Teams.RED);
            updateDescription(Teams.BLUE);
            player.openInventory(wand.inventory);
        }

        playerInteractEvent.setCancelled(true);
    }

    public Wand addOrGetWand(Player player) {
        if (wands.containsKey(player)) {
            return wands.get(player);
        } else {
            Wand wand = new Wand(siegeManager, player);
            wands.put(player, wand);
            return wand;
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent inventoryOpenEvent) {
        Wand wand = wands.get((Player)(inventoryOpenEvent.getPlayer()));
        if (wand == null) {
            return;
        }

        if (inventoryOpenEvent.getInventory() == wand.inventory) {
            wand.opened = true;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        Wand wand = wands.get((Player)(inventoryCloseEvent.getPlayer()));
        if (wand == null) {
            return;
        }

        if (inventoryCloseEvent.getInventory() == wand.inventory) {
            wand.opened = false;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player)(inventoryClickEvent.getWhoClicked());
        if (!player.hasPermission(SiegePermissions.siegeManagement)) {
            return;
        }

        Wand wand = wands.get(player);
        if (wand == null) {
            return;
        }

        if (inventoryClickEvent.getInventory() == wand.inventory) {
            inventoryClickEvent.setCancelled(true);

            ItemStack selected = inventoryClickEvent.getCurrentItem();
            if (selected == null) {
                return;
            }

            WandInventoryItem wandInventoryItem = wand.wandInventoryItems.get(inventoryClickEvent.getSlot());
            if (wandInventoryItem == null) {
                return;
            }

            wandInventoryItem.onClick();
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent playerDropItemEvent) {
        Player player = playerDropItemEvent.getPlayer();
        if (!wands.containsKey(player)) {
            return;
        }

        if (!playerDropItemEvent.getItemDrop().getItemStack().isSimilar(Wand.wandItem)) {
            return;
        }

        playerDropItemEvent.getItemDrop().remove();
        wands.get(player).unHighlightRegisteredChests();
        wands.remove(player);
    }

    @EventHandler
    public void onTeamSettingsChanged(TeamSettingsChangedEvent teamSettingsChangedEvent) {
        updateDescription(Teams.RED);
        updateDescription(Teams.BLUE);
    }

    private void updateDescription(Teams team) {
        for (Wand wand : wands.values()) {
            int index = WandInventoryItem.RED_TEAM;
            if (team == Teams.BLUE) {
                index = WandInventoryItem.BLUE_TEAM;
            }
            wand.wandInventoryItems.get(index).setDescription(siegeManager.teams.get(team).buildDescription());
        }
    }

    @EventHandler
    public void onRegisteredChestListChanged(RegisteredChestListChangedEvent registeredChestListChangedEvent) {
        for (Wand wand : wands.values()) {
            wand.updateHighlightRegisteredChests();
        }
    }
}