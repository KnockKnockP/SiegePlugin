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

import java.util.*;

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
    redWoolItem = new WandInventoryItem(inventory,
        WandInventoryItem.RED_WOOL,
        () -> setWool(Teams.RED),
        Teams.RED.toWool(),
        (Teams.RED.toChatColor() + "Red Wool"),
        "Sets Selection 0 as team red's wool."),
    blueWoolItem = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_WOOL,
        () -> setWool(Teams.BLUE),
        Teams.BLUE.toWool(),
        (Teams.BLUE.toChatColor() + "Blue Wool"),
        "Sets Selection 0 as team blue's wool."),
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
    neutralChestItem = new WandInventoryItem(inventory,
        WandInventoryItem.NEUTRAL_CHEST,
        () -> setChest(Teams.NEUTRAL),
        Teams.NEUTRAL.toShulker(),
        (Teams.NEUTRAL.toChatColor()) + "Neutral Chest",
        "Sets Selection 0 as a team neutral's chest."),
    redChestItem = new WandInventoryItem(inventory,
        WandInventoryItem.RED_CHEST,
        () -> setChest(Teams.RED),
        Teams.RED.toShulker(),
        (Teams.RED.toChatColor()) + "Red Chest",
        "Sets Selection 0 as a team red's chest."),
    blueChestItem = new WandInventoryItem(inventory,
        WandInventoryItem.BLUE_CHEST,
        () -> setChest(Teams.BLUE),
        Teams.BLUE.toShulker(),
        (Teams.BLUE.toChatColor()) + "Blue Chest",
        "Sets Selection 0 as a team blue's chest."),
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
        wandInventoryItems.put(redWoolItem.getIndex(), redWoolItem);
        wandInventoryItems.put(blueWoolItem.getIndex(), blueWoolItem);
        wandInventoryItems.put(redDepositItem.getIndex(), redDepositItem);
        wandInventoryItems.put(blueDepositItem.getIndex(), blueDepositItem);
        wandInventoryItems.put(redSpawnItem.getIndex(), redSpawnItem);
        wandInventoryItems.put(blueSpawnItem.getIndex(), blueSpawnItem);
        wandInventoryItems.put(redAssignerItem.getIndex(), redAssignerItem);
        wandInventoryItems.put(blueAssignerItem.getIndex(), blueAssignerItem);
        wandInventoryItems.put(neutralChestItem.getIndex(), neutralChestItem);
        wandInventoryItems.put(redChestItem.getIndex(), redChestItem);
        wandInventoryItems.put(blueChestItem.getIndex(), blueChestItem);
        wandInventoryItems.put(redTeamItem.getIndex(), redTeamItem);
        wandInventoryItems.put(blueTeamItem.getIndex(), blueTeamItem);
    }

    private final List<ClientSideEntity> highlightedRegisteredChests = new ArrayList<>();

    public Wand(SiegeManager siegeManager, Player player) {
        this.siegeManager = siegeManager;
        this.player = player;
    }

    public void setSelect(Block block, int index) {
        Location location = block.getLocation();
        selections[index] = location;

        selectionItems[index].setIcon(block.getType());
        selectionItems[index].setDescription(SiegeChatColors.WAND_CHAT_COLOR + LocationExtensions.toBlockTriple(location));

        player.sendMessage(String.format(SiegeChatColors.WAND_CHAT_COLOR + "Set %s as Selection %d", LocationExtensions.toBlockTriple(location), index));
    }

    private void addAssigner(Teams team) {
        siegeManager.addTeamAssigner(team, player.getLocation());
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

    private void setWool(Teams team) {
        if (selections[0] != null) {
            siegeManager.setWool(team, selections[0]);
            player.sendMessage(String.format(team.toChatColor() + "Set %s as the team %s's wool.",
                LocationExtensions.toBlockTriple(selections[0]),
                team));
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

    private void setChest(Teams team) {
        if (selections[0] != null) {
            siegeManager.setChestTeam(selections[0], team);
            player.sendMessage(String.format(team.toChatColor() + "Set %s as the team %s's chest.",
                LocationExtensions.toBlockTriple(selections[0]),
                team));
        }
    }

    public void highlightRegisteredChests() {
        for (RegisteredChest registeredChest : siegeManager.registeredChests.values()) {
            Location location = registeredChest.getLocation();
            location.setX(location.getX() + 0.5f);
            location.setY(location.getY() + 0.25f);
            location.setZ(location.getZ() + 0.5f);

            ClientSideSlime clientSideSlime = new ClientSideSlime(player, location);
            clientSideSlime.setMetaStatus((byte)(ClientSideEntity.ENTITY_META_DATA_FLAG_IS_INVISIBLE | ClientSideEntity.ENTITY_META_DATA_FLAG_IS_GLOWING));
            clientSideSlime.setGlowColor(registeredChest.getTeam().toChatColor());
            clientSideSlime.setSize(1);
            highlightedRegisteredChests.add(clientSideSlime);
        }
    }

    public void updateHighlightRegisteredChests() {
        if (highlightedRegisteredChests.isEmpty()) {
            return;
        }

        unHighlightRegisteredChests();
        highlightRegisteredChests();
    }

    public void unHighlightRegisteredChests() {
        for (ClientSideEntity clientSideEntity : highlightedRegisteredChests) {
            clientSideEntity.remove();
        }
        highlightedRegisteredChests.clear();
    }
}