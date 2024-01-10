package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

public final class RegisteredChest {
    private final RegisteredChestData registeredChestData;
    public ResettingChest resettingChest = null;

    public RegisteredChest(Chest chest, Teams team) {
        registeredChestData = new RegisteredChestData(chest, team);
    }

    public void setResetting(ItemStack[] itemStacks, long coolDown, String label) {
        if (resettingChest != null) {
            resettingChest.reset();
            resettingChest.stop();
        }

        resettingChest = new ResettingChest(registeredChestData, itemStacks, coolDown, label);
    }

    public Teams getTeam() {
        return registeredChestData.team;
    }

    public void setTeam(Teams team) {
        registeredChestData.team = team;
    }

    public Location getLocation() {
        return registeredChestData.chest.getLocation();
    }
}