package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public final class Label {
    private ArmorStand armourStand = null;

    public Label(String label, Location location, boolean correctPosition) {
        Location armourStandLocation = location.clone();
        if (correctPosition) {
            armourStandLocation.setX(armourStandLocation.getBlockX() + 0.5d);
            armourStandLocation.setY(armourStandLocation.getBlockY() + 1);
            armourStandLocation.setZ(armourStandLocation.getBlockZ() + 0.5d);
        }

        World world = location.getWorld();
        if (world == null) {
            return;
        }

        armourStand = (ArmorStand)(world.spawnEntity(armourStandLocation, EntityType.ARMOR_STAND));
        armourStand.setInvisible(true);
        armourStand.setInvulnerable(true);
        armourStand.setCollidable(false);
        armourStand.setGravity(false);
        armourStand.setSilent(false);
        armourStand.setMarker(true);
        armourStand.setCustomNameVisible(true);
        armourStand.setCustomName(label);
    }

    public void setLabel(String label) {
        armourStand.setCustomName(label);
    }

    public void remove() {
        if (armourStand != null) {
            armourStand.remove();
            armourStand = null;
        }
    }
}