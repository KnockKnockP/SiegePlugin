package knockknockp.siegeplugin.Siege;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class KitAssigner extends Assigner {
    private final Kit kit;

    public KitAssigner(SiegeManager siegeManager, Location location, Kit kit) {
        super(siegeManager, location);
        this.kit = kit;

        armorStand.setCustomName(kit.getName());

        if (armorStand.getEquipment() == null) {
            return;
        }

        ItemStack[] armours = kit.getArmours();
        armorStand.getEquipment().setHelmet(armours[Kit.HELMET]);
        armorStand.getEquipment().setChestplate(armours[Kit.CHESTPLATE]);
        armorStand.getEquipment().setLeggings(armours[Kit.LEGGINGS]);
        armorStand.getEquipment().setBoots(armours[Kit.BOOTS]);
        armorStand.getEquipment().setItemInMainHand(kit.getHotbarItem(0));
        armorStand.getEquipment().setItemInOffHand(kit.getOffHand());
    }

    @Override
    public void onRightClick(Player player) {
        TeamPlayer teamPlayer = siegeManager.players.get(player);
        if (teamPlayer == null) {
            return;
        }

        teamPlayer.kit = kit;
        player.sendMessage(ChatColor.AQUA + String.format("Assigned kit %s.", kit.getName()));
    }

    public Kit getKit() {
        return kit;
    }
}