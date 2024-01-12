package knockknockp.siegeplugin.Siege;

import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public final class AssignerListener implements Listener {
    private final SiegeManager siegeManager;
    public AssignerListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (!(entityDamageByEntityEvent.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player)(entityDamageByEntityEvent.getDamager());

        if (!(entityDamageByEntityEvent.getEntityType() == EntityType.ARMOR_STAND)) {
            return;
        }

        Assigner assigner = findAssigner((ArmorStand)(entityDamageByEntityEvent.getEntity()));
        if (assigner == null) {
            return;
        }

        assigner.onInteract(player);

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        entityDamageByEntityEvent.setCancelled(true);
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent playerArmorStandManipulateEvent) {
        Assigner assigner = findAssigner(playerArmorStandManipulateEvent.getRightClicked());
        if (assigner == null) {
            return;
        }

        assigner.onInteract(playerArmorStandManipulateEvent.getPlayer());
        playerArmorStandManipulateEvent.setCancelled(true);
    }

    private Assigner findAssigner(ArmorStand armorStand) {
        for (Assigner assigner : siegeManager.assigners) {
            if (assigner.armorStand == armorStand) {
                return assigner;
            }
        }

        return null;
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