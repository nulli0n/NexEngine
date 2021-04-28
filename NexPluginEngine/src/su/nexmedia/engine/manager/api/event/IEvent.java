package su.nexmedia.engine.manager.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class IEvent extends Event {

    protected static final HandlerList handlers = new HandlerList();

    public IEvent() {
        this(false);
    }

    public IEvent(boolean async) {
        super(async);
    }

    @Override
    @NotNull
    public final HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
