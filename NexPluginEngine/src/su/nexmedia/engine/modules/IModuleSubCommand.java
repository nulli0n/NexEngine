package su.nexmedia.engine.modules;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.commands.api.ISubCommand;

public abstract class IModuleSubCommand<P extends NexPlugin<P>, M extends IModule<P>> extends ISubCommand<P> {

    protected @NotNull M module;
    
    public IModuleSubCommand(@NotNull M module, @NotNull List<String> aliases) {
        super(module.plugin, aliases.toArray(new String[aliases.size()]));
        this.module = module;
    }

    public IModuleSubCommand(@NotNull M module, @NotNull String[] aliases) {
        super(module.plugin, aliases, null);
        this.module = module;
    }

    public IModuleSubCommand(@NotNull M module, @NotNull List<String> aliases, @Nullable String permission) {
        super(module.plugin, aliases.toArray(new String[aliases.size()]), permission);
        this.module = module;
    }

    public IModuleSubCommand(@NotNull M module, @NotNull String[] aliases, @Nullable String permission) {
        super(module.plugin, aliases, permission);
        this.module = module;
    }
    
    @NotNull
    public M getModule() {
        return this.module;
    }
}
