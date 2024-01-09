package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;

public final class LocationExtensions {
    public static String toBlockTriple(Location location) {
        if (location == null) {
            return "null";
        }
        return String.format("%d %d %d", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
}