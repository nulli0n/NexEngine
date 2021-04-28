package su.nexmedia.engine.manager;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;

public abstract class IListener<P extends NexPlugin<P>> implements AbstractListener {

    @NotNull
    public final P plugin;

    public IListener(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }
}
