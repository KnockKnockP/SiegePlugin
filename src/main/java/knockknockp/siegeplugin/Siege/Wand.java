package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class Wand {
    public boolean opened;

    private final SiegeManager siegeManager;
    private final Player player;

    public static ItemStack wandItem = new ItemStack(Material.GOLDEN_AXE);
    static {
        ItemMeta itemMeta = wandItem.getItemMeta();
        assert (itemMeta != null);
        itemMeta.setDisplayName(ChatColor.AQUA + "Siege Wand");
        itemMeta.setLore(Arrays.asList(ChatColor.DARK_GRAY + "This is a men kissing tool.",
                SiegeChatColors.WAND_CHAT_COLOR + "Left click on a block to save it's position as Selection 0.",
                SiegeChatColors.WAND_CHAT_COLOR + "Right click on a block to save it's position as Selection 1.",
                SiegeChatColors.WAND_CHAT_COLOR + "Right click on an air to open the menu."));
        itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, false);
        wandItem.setItemMeta(itemMeta);
    }

    public Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, (ChatColor.AQUA + "Siege Wand"));

    private final Location[] selections = new Location[2];
    {
        selections[0] = null;
        selections[1] = null;
    }
    private final WandInventoryItem[] selectionItems = new WandInventoryItem[2];
    {
        String description = (SiegeChatColors.ERROR_CHAT_COLOR + "No Selection Yet");
        selectionItems[0] = new WandInventoryItem(inventory,
            WandInventoryItem.SELECT_0,
            null,
            Material.BARRIER,
            (SiegeChatColors.WAND_CHAT_COLOR + "Selection 0"),
            description);
        selectionItems[1] = new WandInventoryItem(inventory,
            WandInventoryItem.SELECT_1,
            null,
            Material.BARRIER,
            (SiegeChatColors.WAND_CHAT_COLOR + "Selection 1"),
            description);
    }

    private final WandInventoryItem redBaseItem = new WandInventoryItem(inventory,
        WandInventoryItem.RED_BASE,
        () -> setBase(Teams.RED),
        Teams.RED.toBanner(),
        (Teams.RED.toChatColor() + "Red Base"),
        "Sets the selection as the red base."),
    blueBaseItem = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_BASE,
        () -> setBase(Teams.BLUE),
        Teams.BLUE.toBanner(),
        (Teams.BLUE.toChatColor() + "Blue Base"),
        "Sets the selection as the blue base."),
    redWool0Item = new WandInventoryItem(inventory,
        WandInventoryItem.RED_WOOL_0,
        () -> setWool(Teams.RED, 0),
        Teams.RED.toWool(),
        (Teams.RED.toChatColor() + "Red Wool 0"),
        "Sets Selection 0 as team red's wool 0."),
    redWool1Item = new WandInventoryItem(inventory,
        WandInventoryItem.RED_WOOL_1,
        () -> setWool(Teams.RED, 1),
        Teams.RED.toWool(),
        (Teams.RED.toChatColor() + "Red Wool 1"),
        "Sets Selection 0 as team red's wool 1."),
    redWool2Item = new WandInventoryItem(inventory,
        WandInventoryItem.RED_WOOL_2,
        () -> setWool(Teams.RED, 2),
        Teams.RED.toWool(),
        (Teams.RED.toChatColor() + "Red Wool 2"),
        "Sets Selection 0 as team red's wool 2."),
    blueWool0Item = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_WOOL_0,
        () -> setWool(Teams.BLUE, 0),
        Teams.BLUE.toWool(),
        (Teams.BLUE.toChatColor() + "Blue Wool 0"),
        "Sets Selection 0 as team blue's wool 0."),
    blueWool1Item = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_WOOL_1,
        () -> setWool(Teams.BLUE, 1),
        Teams.BLUE.toWool(),
        (Teams.BLUE.toChatColor() + "Blue Wool 1"),
        "Sets Selection 0 as team blue's wool 1."),
    blueWool2Item = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_WOOL_2,
        () -> setWool(Teams.BLUE, 2),
        Teams.BLUE.toWool(),
        (Teams.BLUE.toChatColor() + "Blue Wool 2"),
        "Sets Selection 0 as team blue's wool 2."),
    redDepositItem = new WandInventoryItem(inventory,
        WandInventoryItem.RED_DEPOSIT,
        () -> setDeposit(Teams.RED),
        Teams.RED.toCandle(),
        (Teams.RED.toChatColor()) + "Red Deposit",
        "Sets Selection 0 as team red's deposit."),
    blueDepositItem = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_DEPOSIT,
        () -> setDeposit(Teams.BLUE),
        Teams.BLUE.toCandle(),
        (Teams.BLUE.toChatColor()) + "Blue Deposit",
        "Sets Selection 0 as team blue's deposit."),
    redSpawnItem = new WandInventoryItem(inventory,
        WandInventoryItem.RED_SPAWN,
        () -> setSpawn(Teams.RED),
        Teams.RED.toBed(),
        (Teams.RED.toChatColor()) + "Red Spawn",
        "Sets team red's spawn point to the player's position."),
    blueSpawnItem = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_SPAWN,
        () -> setSpawn(Teams.BLUE),
        Teams.BLUE.toBed(),
        (Teams.BLUE.toChatColor()) + "Blue Spawn",
        "Sets team blue's spawn point to the player's position."),
    redAssignerItem = new WandInventoryItem(inventory,
        WandInventoryItem.RED_ASSIGNER,
        () -> addAssigner(Teams.RED),
        Teams.RED.toDye(),
        (Teams.RED.toChatColor()) + "Red Assigner",
        "Spawns team red's assigner at the player's position."),
    blueAssignerItem = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_ASSIGNER,
        () -> addAssigner(Teams.BLUE),
        Teams.BLUE.toDye(),
        (Teams.BLUE.toChatColor()) + "Blue Assigner",
        "Spawns team blue's assigner at the player's position."),
    redTeamItem = new WandInventoryItem(inventory,
        WandInventoryItem.RED_TEAM,
        null,
        Teams.RED.toTerracotta(),
        (Teams.RED.toChatColor() + "Red Team"),
        ""),
    blueTeamItem = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_TEAM,
        null,
        Teams.BLUE.toTerracotta(),
        (Teams.BLUE.toChatColor() + "Blue Team"),
        "");

    public Map<Integer, WandInventoryItem> wandInventoryItems = new HashMap<>();
    {
        wandInventoryItems.put(redBaseItem.getIndex(), redBaseItem);
        wandInventoryItems.put(blueBaseItem.getIndex(), blueBaseItem);
        wandInventoryItems.put(redWool0Item.getIndex(), redWool0Item);
        wandInventoryItems.put(redWool1Item.getIndex(), redWool1Item);
        wandInventoryItems.put(redWool2Item.getIndex(), redWool2Item);
        wandInventoryItems.put(blueWool0Item.getIndex(), blueWool0Item);
        wandInventoryItems.put(blueWool1Item.getIndex(), blueWool1Item);
        wandInventoryItems.put(blueWool2Item.getIndex(), blueWool2Item);
        wandInventoryItems.put(redDepositItem.getIndex(), redDepositItem);
        wandInventoryItems.put(blueDepositItem.getIndex(), blueDepositItem);
        wandInventoryItems.put(redSpawnItem.getIndex(), redSpawnItem);
        wandInventoryItems.put(blueSpawnItem.getIndex(), blueSpawnItem);
        wandInventoryItems.put(redAssignerItem.getIndex(), redAssignerItem);
        wandInventoryItems.put(blueAssignerItem.getIndex(), blueAssignerItem);
        wandInventoryItems.put(redTeamItem.getIndex(), redTeamItem);
        wandInventoryItems.put(blueTeamItem.getIndex(), blueTeamItem);
    }

    public Wand(SiegeManager siegeManager, Player player) {
        this.siegeManager = siegeManager;
        this.player = player;
    }

    public void setSelect(Block block, int index) {
        Location location = block.getLocation();
        selections[index] = location;

        selectionItems[index].setIcon(block.getType());
        selectionItems[index].setDescription(SiegeChatColors.WAND_CHAT_COLOR + LocationExtensions.toBlockTriple(location));

        player.sendMessage(String.format(SiegeChatColors.WAND_CHAT_COLOR + "Set %s as Position %d", LocationExtensions.toBlockTriple(location), index));
    }

    private void addAssigner(Teams team) {
        siegeManager.addAssigner(team, player.getLocation());
        player.sendMessage(String.format(team.toChatColor() + "Spawned team %s's assigner.", team));
    }

    private void setBase(Teams team) {
        if ((selections[0] != null) && (selections[1] != null)) {
            siegeManager.setBase(team, selections[0], selections[1]);
            player.sendMessage(String.format(team.toChatColor() + "Set the area from %s to %s as the team %s's base.",
                LocationExtensions.toBlockTriple(selections[0]),
                LocationExtensions.toBlockTriple(selections[1]),
                team));
        }
    }

    private void setWool(Teams team, int index) {
        if (selections[0] != null) {
            siegeManager.setWool(team, index, selections[0]);
            player.sendMessage(String.format(team.toChatColor() + "Set %s as the team %s's No. %d wool.",
                LocationExtensions.toBlockTriple(selections[0]),
                team,
                index));
        }
    }

    private void setDeposit(Teams team) {
        if (selections[0] != null) {
            siegeManager.setDeposit(team, selections[0]);
            player.sendMessage(String.format(team.toChatColor() + "Set %s as the team %s's deposit.",
                LocationExtensions.toBlockTriple(selections[0]),
                team));
        }
    }

    private void setSpawn(Teams team) {
        Location location = player.getLocation();
        siegeManager.setSpawn(team, location);
        player.sendMessage(String.format(team.toChatColor() + "Set %s as the team %s's spawn position.",
            LocationExtensions.toBlockTriple(location),
            team));
    }
}