package knockknockp.siegeplugin.Siege;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public final class AssignerListener implements Listener {
    private final SiegeManager siegeManager;
    public AssignerListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent playerArmorStandManipulateEvent) {
        for (Assigner assigner : siegeManager.assigners) {
            assigner.onRightClick(playerArmorStandManipulateEvent.getPlayer());
            playerArmorStandManipulateEvent.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
        for (Assigner assigner : siegeManager.assigners) {
            if (assigner.armorStand == entityDeathEvent.getEntity()) {
                siegeManager.assigners.remove(assigner);
                return;
            }
        }
    }
}