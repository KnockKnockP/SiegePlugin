package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class ResettingChest {
    public Chest chest;
    private final ItemStack[] contents;
    public Teams team;
    public int taskId = -1;

    private long remainingCoolDown;
    private final long coolDown;

    private final String label;
    private ArmorStand armourStand = null;

    public ResettingChest(Chest chest, Teams team, long coolDown, String label) {
        this.chest = chest;
        this.contents = chest.getBlockInventory().getContents();
        this.team = team;
        this.coolDown = remainingCoolDown = coolDown;
        this.label = label;
    }

    public void start() {
        Location armourStandLocation = chest.getLocation();
        armourStandLocation.setX(armourStandLocation.getBlockX() + 0.5d);
        armourStandLocation.setY(armourStandLocation.getBlockY() - 0.5d);
        armourStandLocation.setZ(armourStandLocation.getBlockZ() + 0.5d);

        armourStand = (ArmorStand)(chest.getWorld().spawnEntity(armourStandLocation, EntityType.ARMOR_STAND));
        armourStand.setInvisible(true);
        armourStand.setInvulnerable(true);
        armourStand.setCollidable(false);
        armourStand.setGravity(false);
        armourStand.setSilent(false);
        armourStand.setCustomNameVisible(true);
        armourStand.setCustomName(customName());
    }

    public void tick() {
        if (--remainingCoolDown <= 0) {
            chest.getBlockInventory().setContents(contents);
            remainingCoolDown = coolDown;
            Bukkit.getLogger().info(String.format("Resetted team %s's chest.", team));
        }

        armourStand.setCustomName(customName());
    }

    private String customName() {
        return String.format(team.toChatColor() + "%s: %d초 남음", label, (remainingCoolDown / 20));
    }

    public void stop() {
        if (armourStand != null) {
            armourStand.remove();
            armourStand = null;
        }
    }
}