package su.nexmedia.engine.modules;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.core.config.CoreConfig;

public abstract class IExternalModule<P extends NexPlugin<P>> extends IModule<P> {

    public IExternalModule(@NotNull P plugin) {
        super(plugin);
    }

    @NotNull
    public abstract LoadPriority getPriority();

    @Override
    @NotNull
    public String getPath() {
        return CoreConfig.MODULES_PATH_EXTERNAL + this.getId() + "/";
    }

    public static enum LoadPriority {
        HIGH, LOW,;
    }
}
