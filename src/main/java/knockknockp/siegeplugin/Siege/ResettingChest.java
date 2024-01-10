package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class ResettingChest {
    private final RegisteredChestData registeredChestData;
    private final ItemStack[] contents;
    public int taskId = -1;

    private long remainingCoolDown;
    private final long coolDown;

    private final String label;
    private ArmorStand armourStand = null;

    public ResettingChest(RegisteredChestData registeredChestData, ItemStack[] itemStacks, long coolDown, String label) {
        this.registeredChestData = registeredChestData;
        this.contents = itemStacks;
        this.coolDown = remainingCoolDown = coolDown;
        this.label = label;
    }

    public void start() {
        Location armourStandLocation = registeredChestData.chest.getLocation();
        armourStandLocation.setX(armourStandLocation.getBlockX() + 0.5d);
        armourStandLocation.setY(armourStandLocation.getBlockY() - 0.5d);
        armourStandLocation.setZ(armourStandLocation.getBlockZ() + 0.5d);

        armourStand = (ArmorStand) (registeredChestData.chest.getWorld().spawnEntity(armourStandLocation, EntityType.ARMOR_STAND));
        armourStand.setInvisible(true);
        armourStand.setInvulnerable(true);
        armourStand.setCollidable(false);
        armourStand.setGravity(false);
        armourStand.setSilent(false);
        armourStand.setCustomNameVisible(true);
        armourStand.setCustomName(customName());

        reset();
    }

    public void tick() {
        if (--remainingCoolDown <= 0) {
            reset();
        }

        armourStand.setCustomName(customName());
    }

    public void reset() {
        registeredChestData.chest.getBlockInventory().setContents(contents);
        remainingCoolDown = coolDown;
        Bukkit.getLogger().info(String.format("Resetted team %s's chest.", registeredChestData.team));
    }

    private String customName() {
        return String.format(registeredChestData.team.toChatColor() + "%s: %d초 남음", label, (remainingCoolDown / 20));
    }

    public void stop() {
        if (armourStand != null) {
            armourStand.remove();
            armourStand = null;
        }
    }
}