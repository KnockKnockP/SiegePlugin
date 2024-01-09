package knockknockp.siegeplugin.Siege;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SpawnSetEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Teams team;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public SpawnSetEvent(Teams team) {
        this.team = team;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public Teams getTeam() {
        return team;
    }
}