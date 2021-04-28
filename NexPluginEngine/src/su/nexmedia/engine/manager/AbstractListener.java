package su.nexmedia.engine.manager;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface AbstractListener extends Listener {

    public void registerListeners();

    public default void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
}
