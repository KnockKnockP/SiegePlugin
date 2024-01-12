package knockknockp.siegeplugin.Siege;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class TeamAssigner extends Assigner {
    private final Teams team;

    public TeamAssigner(SiegeManager siegeManager, Location location, Teams team) {
        super(siegeManager, location);

        this.team = team;

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

        if (armorStand.getEquipment() == null) {
            return;
        }

        armorStand.getEquipment().setHelmet(armours[0], true);
        armorStand.getEquipment().setChestplate(armours[1], true);
        armorStand.getEquipment().setLeggings(armours[2], true);
        armorStand.getEquipment().setBoots(armours[3], true);

        armorStand.setCustomName(customName);
    }

    @Override
    public void onInteract(Player player) {
        siegeManager.assignPlayerToTeam(player, team);
    }

    public Teams getTeam() {
        return team;
    }
}