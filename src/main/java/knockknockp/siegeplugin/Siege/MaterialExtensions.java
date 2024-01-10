package knockknockp.siegeplugin.Siege;

import org.bukkit.Material;

public final class MaterialExtensions {
    public static Teams woolToTeam(Material wool) {
        for (Teams team : Teams.values()) {
            if (team.toWool() == wool) {
                return team;
            }
        }

        return null;
    }
}