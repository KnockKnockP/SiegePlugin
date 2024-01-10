package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class Kit {
    public static final int  BOOTS = 0, LEGGINGS = 1, CHESTPLATE = 2, HELMET = 3, OFF_HAND = 4;

    private final String name;

    private final Inventory inventory;

    private ItemStack[] items = new ItemStack[(9 * 4)];

    private ItemStack offHand;
    private final ItemStack offHandPlaceHolderItem = makePlaceHolderItem("Off-hand", "Simply replace this to an off-hand object.");

    private final ItemStack[] armours = new ItemStack[4];
    private final ItemStack[] armourPlaceHolderItems = new ItemStack[4];
    {
        armourPlaceHolderItems[HELMET] = makePlaceHolderItem("Helmet", "Simply replace this to a helmet.");
        armourPlaceHolderItems[CHESTPLATE] = makePlaceHolderItem("Chestplate", "Simply replace this to a chestplate.");
        armourPlaceHolderItems[LEGGINGS] = makePlaceHolderItem("Leggings", "Simply replace this to leggings.");
        armourPlaceHolderItems[BOOTS] = makePlaceHolderItem("Boots", "Simply replace this to boots.");
    }

    private static ItemStack makePlaceHolderItem(String name, String ...descriptions) {
        ItemStack placeHolderItem = new ItemStack(Material.BARRIER);
        ItemMeta helmetPlaceHolderItemMeta = placeHolderItem.getItemMeta();
        assert (helmetPlaceHolderItemMeta != null);

        helmetPlaceHolderItemMeta.setDisplayName(name);
        helmetPlaceHolderItemMeta.setLore(Arrays.asList(descriptions));
        placeHolderItem.setItemMeta(helmetPlaceHolderItemMeta);
        return placeHolderItem;
    }

    public Kit(String name) {
        this.name = name;

        inventory = Bukkit.createInventory(null, (9 * 5), String.format(ChatColor.AQUA + "Kit %s", name));
        ItemStack[] initialItems = inventory.getStorageContents();
        initialItems[HELMET] = armourPlaceHolderItems[HELMET];
        initialItems[CHESTPLATE] = armourPlaceHolderItems[CHESTPLATE];
        initialItems[LEGGINGS] = armourPlaceHolderItems[LEGGINGS];
        initialItems[BOOTS] = armourPlaceHolderItems[BOOTS];
        initialItems[OFF_HAND] = offHandPlaceHolderItem;
        inventory.setStorageContents(initialItems);
    }

    public void edit(Player player) {
        player.openInventory(inventory);
    }

    public void save() {
        ItemStack[] itemStacks = inventory.getStorageContents();

        checkAndSaveArmour(itemStacks, HELMET);
        checkAndSaveArmour(itemStacks, CHESTPLATE);
        checkAndSaveArmour(itemStacks, LEGGINGS);
        checkAndSaveArmour(itemStacks, BOOTS);
        checkAndSaveOffHand(itemStacks);

        items = Arrays.copyOfRange(itemStacks, 9, (9 * 5));
    }

    private void checkAndSaveArmour(ItemStack[] itemStacks, int type) {
        ItemStack itemStack = itemStacks[type];
        boolean shouldReset = false;
        if (itemStack == null) {
            shouldReset = true;
        } else if (getArmourSlot(itemStack.getType()) != type) {
            shouldReset = true;
        }

        if (shouldReset) {
            ItemStack[] contents = inventory.getStorageContents();
            contents[type] = armourPlaceHolderItems[type];
            inventory.setStorageContents(contents);
        } else {
            armours[type] = itemStack;
        }
    }

    private int getArmourSlot(Material material) {
        switch (material) {
            case LEATHER_HELMET:
            case CHAINMAIL_HELMET:
            case IRON_HELMET:
            case GOLDEN_HELMET:
            case DIAMOND_HELMET:
            case NETHERITE_HELMET:
                return HELMET;
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case NETHERITE_CHESTPLATE:
                return CHESTPLATE;
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case NETHERITE_LEGGINGS:
                return LEGGINGS;
            case LEATHER_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case GOLDEN_BOOTS:
            case DIAMOND_BOOTS:
            case NETHERITE_BOOTS:
                return BOOTS;
        }
        return -1;
    }

    private void checkAndSaveOffHand(ItemStack[] itemStacks) {
        ItemStack itemStack = itemStacks[OFF_HAND];
        if ((itemStack == null) || (itemStack.isSimilar(offHandPlaceHolderItem))) {
            ItemStack[] contents = inventory.getStorageContents();
            contents[OFF_HAND] = offHandPlaceHolderItem;
            inventory.setStorageContents(contents);
            return;
        }

        offHand = itemStacks[OFF_HAND];
    }

    public void giveTo(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setStorageContents(items);
        playerInventory.setArmorContents(armours);
        playerInventory.setItemInOffHand(offHand);
    }

    public String getName() {
        return name;
    }

    public ItemStack[] getArmours() {
        return armours;
    }

    public ItemStack getHotbarItem(int index) {
        return items[((9) + index)];
    }

    public ItemStack getOffHand() {
        return offHand;
    }
}