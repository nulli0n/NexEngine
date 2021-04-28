package su.nexmedia.engine.manager;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.manager.api.Loadable;

public abstract class IManager<P extends NexPlugin<P>> extends IListener<P> implements Loadable {

    public IManager(@NotNull P plugin) {
        super(plugin);
    }
}
