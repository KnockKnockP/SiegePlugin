package knockknockp.siegeplugin.Siege;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class WoolListener implements Listener {
    private final SiegeManager siegeManager;

    private static boolean isWool(Material material) {
        return (material == Material.RED_WOOL) || (material == Material.BLUE_WOOL);
    }

    public WoolListener(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        TeamPlayer teamPlayer = siegeManager.players.get(blockPlaceEvent.getPlayer());
        if (teamPlayer == null) {
            return;
        }

        Block block = blockPlaceEvent.getBlock();
        Material material = block.getType();
        if (isWool(material)) {
            if (block.getLocation().equals(siegeManager.teams.get(teamPlayer.team).deposit)) {
                if (teamPlayer.team.toWool() == material) {
                    return;
                }

                teamPlayer.player.getInventory().setContents(teamPlayer.savedInventory);
                siegeManager.incrementScore(teamPlayer.team);

                teamPlayer.player.removePotionEffect(PotionEffectType.GLOWING);

                for (TeamPlayer tp : siegeManager.players.values()) {
                    Player player = tp.player;
                    Sound sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
                    if (tp.team == teamPlayer.team) {
                        sound = Sound.BLOCK_NOTE_BLOCK_CHIME;
                    }
                    player.playSound(player.getLocation(), sound, 1, 1);
                }
            } else {
                blockPlaceEvent.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        TeamPlayer teamPlayer = siegeManager.players.get(blockBreakEvent.getPlayer());
        if (teamPlayer == null) {
            return;
        }

        Teams team = teamPlayer.team, enemy = Teams.RED;
        if (team == Teams.RED) {
            enemy = Teams.BLUE;
        }

        Block block = blockBreakEvent.getBlock();
        if (!isWool(block.getType())) {
            return;
        }

        for (Location location : siegeManager.teams.get(enemy).wools) {
            if (!location.equals(block.getLocation())) {
                continue;
            }

            PlayerInventory inventory = teamPlayer.player.getInventory();
            if (inventory.contains(Teams.RED.toWool()) || inventory.contains(Teams.BLUE.toWool())) {
                return;
            }

            blockBreakEvent.setCancelled(false);
            blockBreakEvent.setDropItems(false);

            teamPlayer.savedInventory = inventory.getContents().clone();

            ItemStack[] armours = inventory.getArmorContents().clone();
            inventory.clear();
            inventory.setArmorContents(armours);

            teamPlayer.player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 1000000, 1000000, false, false));
            teamPlayer.player.getInventory().setItemInMainHand(new ItemStack(enemy.toWool()));

            for (TeamPlayer player : siegeManager.players.values()) {
                if (player.team == enemy) {
                    Player playerToWarn = player.player;
                    playerToWarn.playSound(playerToWarn.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1, 0.5f);

                    playerToWarn.sendTitle(team.toChatColor() + "양털 약탈됨!", "강조된 적을 처치해서 되돌리세요.", 0, 80, 0);
                    playerToWarn.sendMessage(team.toChatColor() + "Your team's wool has been stolen!");
                }
            }
            return;
        }
    }

    @EventHandler
    public void onWoolDrop(PlayerDropItemEvent playerDropItemEvent) {
        if (siegeManager.isGameRunning && isWool(playerDropItemEvent.getItemDrop().getItemStack().getType())) {
            playerDropItemEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        ItemStack clickedItem = inventoryClickEvent.getCurrentItem();
        if (clickedItem == null) {
            return;
        }

        if (siegeManager.isGameRunning && isWool(clickedItem.getType())) {
            inventoryClickEvent.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        if (!siegeManager.isGameRunning) {
            return;
        }

        List<ItemStack> itemStacks = playerDeathEvent.getDrops();
        cleanUp:
        for (ItemStack itemStack : itemStacks) {
            Material wool = itemStack.getType();
            if (!isWool(wool)) {
                continue;
            }

            Teams team = MaterialExtensions.woolToTeam(wool);
            if (team == null) {
                continue;
            }

            for (Location woolLocation : siegeManager.teams.get(team).wools) {
                if (woolLocation.getBlock().getType() != wool) {
                    woolLocation.getBlock().setType(wool);

                    Player player = playerDeathEvent.getEntity();
                    Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                    FireworkMeta fireworkMeta = firework.getFireworkMeta();
                    fireworkMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(team.toColor()).withFlicker().build());
                    firework.setFireworkMeta(fireworkMeta);
                    firework.detonate();

                    Bukkit.getServer().broadcastMessage(team.toChatColor() + "The wool has been returned to the base.");
                    break cleanUp;
                }
            }
        }

        itemStacks.clear();
    }
}