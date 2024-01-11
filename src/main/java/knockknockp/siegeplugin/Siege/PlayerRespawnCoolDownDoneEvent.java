package knockknockp.siegeplugin.Siege;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlayerRespawnCoolDownDoneEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerRespawnCoolDownDoneEvent(Player player) {
        this.player = player;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }
}