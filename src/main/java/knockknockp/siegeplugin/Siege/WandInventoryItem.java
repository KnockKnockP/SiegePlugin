package knockknockp.siegeplugin.Siege;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public final class WandInventoryItem {
    public static final int SELECT_0 = 0, SELECT_1 = 1, NEUTRAL_CHEST = 3, RED_CHEST = 4, BLUE_CHEST = 5, RED_DEPOSIT = 7, BLUE_DEPOSIT = 8,
                            RED_TEAM = 9, BLUE_TEAM = 10, RED_WOOL = 12, BLUE_WOOL = 13, RED_SPAWN = 16, BLUE_SPAWN = 17,
                            RED_BASE = 18, BLUE_BASE = 19, RED_ASSIGNER = 25, BLUE_ASSIGNER = 26;

    private final Inventory inventory;
    private final int index;
    private final Runnable onClick;
    private ItemStack itemStack;
    private String name;
    private List<String> description;

    public WandInventoryItem(Inventory inventory, int index, Runnable onClick, Material material, String name, String ...description) {
        this.inventory = inventory;
        this.index = index;
        this.onClick = onClick;
        this.name = name;
        this.description = Arrays.asList(description);

        setIcon(material);
    }

    public void setIcon(Material material) {
        itemStack = new ItemStack(material);
        setName(name);
        setDescription(description);
    }

    public void setName(String newName) {
        name = newName;

        ItemMeta itemMeta = getItemMeta();
        itemMeta.setDisplayName(newName);
        setItemMeta(itemMeta);
    }

    public void setDescription(String ...newDescription) {
        setDescription(Arrays.asList(newDescription));
    }

    public void setDescription(List<String> newDescription) {
        description = newDescription;

        ItemMeta itemMeta = getItemMeta();
        itemMeta.setLore(description);
        setItemMeta(itemMeta);
    }

    public void onClick() {
        if (onClick != null) {
            onClick.run();
        }
    }

    public int getIndex() {
        return index;
    }

    private ItemMeta getItemMeta() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert (itemMeta != null);
        return itemMeta;
    }

    private void setItemMeta(ItemMeta itemMeta) {
        itemStack.setItemMeta(itemMeta);
        inventory.setItem(index, itemStack);
    }
}