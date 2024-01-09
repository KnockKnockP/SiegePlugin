package knockknockp.siegeplugin.Siege;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public final class WandInventoryItem {
    public static final int SELECT_0 = 0, SELECT_1 = 1, RED_WOOL_0 = 3, RED_WOOL_1 = 4, RED_WOOL_2 = 5, RED_SPAWN = 7, BLUE_SPAWN = 8,
                            RED_BASE = 9, BLUE_BASE = 10, BLUE_WOOL_0 = 12, BLUE_WOOL_1 = 13, BLUE_WOOL_2 = 14, RED_ASSIGNER = 16, BLUE_ASSIGNER = 17,
                            RED_DEPOSIT = 18, BLUE_DEPOSIT = 19, RED_TEAM = 25, BLUE_TEAM = 26;

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