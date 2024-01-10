package knockknockp.siegeplugin.Siege;

import org.bukkit.block.Chest;

public final class RegisteredChestData {
    public Chest chest;
    public Teams team;

    public RegisteredChestData(Chest chest, Teams team) {
        this.chest = chest;
        this.team = team;
    }
}