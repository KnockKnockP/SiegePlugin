package knockknockp.siegeplugin.Siege;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RegisteredChestListChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public RegisteredChestListChangedEvent() {}

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}