package knockknockp.siegeplugin.Siege;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Objects;

public final class Assigner {
    private final SiegeManager siegeManager;
    public final ArmorStand armorStand;
    public final Teams team;

    public Assigner(SiegeManager siegeManager, Teams team, Location location) {
        this.siegeManager = siegeManager;
        this.team = team;

        armorStand = (ArmorStand)(Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.ARMOR_STAND));
        armorStand.setInvulnerable(true);
        armorStand.setCollidable(false);
        armorStand.setSilent(true);
        armorStand.setCustomNameVisible(true);

        String customName = team.toChatColor().toString();
        final ItemStack[] armours = {
                new ItemStack(Material.LEATHER_HELMET),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_BOOTS)
        };
        Color color;

        if (team == Teams.RED) {
            customName += "빨강팀 참가";
            color = Color.RED;
        } else {
            customName += "청팀 참가";
            color = Color.BLUE;
        }

        for (ItemStack armour : armours) {
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)(armour.getItemMeta());
            assert (leatherArmorMeta != null);
            leatherArmorMeta.setColor(color);
            armour.setItemMeta(leatherArmorMeta);
        }

        Objects.requireNonNull(armorStand.getEquipment()).setHelmet(armours[0], true);
        armorStand.getEquipment().setChestplate(armours[1], true);
        armorStand.getEquipment().setLeggings(armours[2], true);
        armorStand.getEquipment().setBoots(armours[3], true);

        armorStand.setCustomName(customName);
    }

    public void assign(Player player) {
        siegeManager.assignPlayerToTeam(player, team);
    }

    public void remove() {
        armorStand.remove();
    }
}