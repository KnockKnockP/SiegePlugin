package knockknockp.siegeplugin.Siege;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class TeamPlayer {
    public Player player;
    public Teams team;
    public Kit kit;
    public ItemStack[] savedInventory = null;

    public TeamPlayer(Player player, Teams team) {
        this.player = player;
        this.team = team;
    }
}