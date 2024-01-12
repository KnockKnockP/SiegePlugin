package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public final class ResettingChest {
    private final RegisteredChestData registeredChestData;
    private final ItemStack[] contents;
    public int taskId = -1;

    private long remainingCoolDown;
    private final long coolDown;

    private final String label;
    private Label labelArmourStand = null;

    public ResettingChest(RegisteredChestData registeredChestData, ItemStack[] itemStacks, long coolDown, String label) {
        this.registeredChestData = registeredChestData;
        this.contents = itemStacks;
        this.coolDown = remainingCoolDown = coolDown;
        this.label = label;
    }

    public void start() {
        labelArmourStand = new Label(label, registeredChestData.chest.getLocation(), true);
        reset();
    }

    public void countSecond() {
        if (remainingCoolDown-- <= 0) {
            reset();
        }

        labelArmourStand.setLabel(String.format(registeredChestData.team.toChatColor() + "%s: %d초 남음", label, remainingCoolDown));
    }

    public void reset() {
        registeredChestData.chest.getBlockInventory().setContents(contents);
        remainingCoolDown = coolDown;
        Bukkit.getLogger().info(String.format("Resetted team %s's chest.", registeredChestData.team));
    }

    public void stop() {
        if (labelArmourStand != null) {
            labelArmourStand.remove();
            labelArmourStand = null;
        }
    }
}