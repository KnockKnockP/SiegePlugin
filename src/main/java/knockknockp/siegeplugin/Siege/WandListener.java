package knockknockp.siegeplugin.Siege;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class WandListener implements Listener {
    private final SiegeManager siegeManager;

    private Player lastPlayer = null;
    private final Map<Player, Wand> wands = new HashMap<>();

    public WandListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
        ItemStack eventItem = playerInteractEvent.getItem();
        if ((eventItem == null) || (!eventItem.isSimilar(Wand.wandItem))) {
            return;
        }

        Player player = playerInteractEvent.getPlayer();
        Wand wand;
        if (!wands.containsKey(player)) {
            wand = new Wand(siegeManager, player);
            wands.put(player, wand);
        } else {
            wand = wands.get(player);
        }

        Action action = playerInteractEvent.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            wand.setSelect(Objects.requireNonNull(playerInteractEvent.getClickedBlock()), 0);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            wand.setSelect(Objects.requireNonNull(playerInteractEvent.getClickedBlock()), 1);
        } else if (action == Action.RIGHT_CLICK_AIR) {
            player.openInventory(wand.inventory);
        }

        playerInteractEvent.setCancelled(true);
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
        lastPlayer = (Player)(inventoryClickEvent.getWhoClicked());

        Wand wand = wands.get(lastPlayer);
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
    public void onInventoryMoveItem(InventoryMoveItemEvent inventoryMoveItemEvent) {
        if (lastPlayer == null) {
            return;
        }

        Wand wand = wands.get(lastPlayer);
        if (wand == null) {
            return;
        }

        if (wand.opened && (inventoryMoveItemEvent.getDestination() == wand.inventory)) {
            inventoryMoveItemEvent.setCancelled(true);
        }
        lastPlayer = null;
    }

    @EventHandler
    public void onBaseSet(BaseSetEvent baseSetEvent) {
        updateDescription(baseSetEvent.getTeam());
    }

    @EventHandler
    public void onWoolSet(WoolSetEvent woolSetEvent) {
        updateDescription(woolSetEvent.getTeam());
    }

    @EventHandler
    public void onDepositSet(DepositSetEvent depositSetEvent) {
        updateDescription(depositSetEvent.getTeam());
    }

    @EventHandler
    public void onSpawnSet(SpawnSetEvent spawnSetEvent) {
        updateDescription(spawnSetEvent.getTeam());
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
}