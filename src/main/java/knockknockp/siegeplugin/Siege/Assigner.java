package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Assigner {
    protected final SiegeManager siegeManager;
    protected final ArmorStand armorStand;

    public Assigner(SiegeManager siegeManager, Location location) {
        this.siegeManager = siegeManager;

        armorStand = (ArmorStand)(Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.ARMOR_STAND));
        armorStand.setInvulnerable(false);
        armorStand.setCollidable(false);
        armorStand.setSilent(true);
        armorStand.setCustomNameVisible(true);
    }

    public void onInteract(Player player) {}

    public void remove() {
        armorStand.remove();
    }
}