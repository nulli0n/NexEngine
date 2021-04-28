package su.nexmedia.engine.modules;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.commands.api.IGeneralCommand;

public class IModuleExecutor<P extends NexPlugin<P>> extends IGeneralCommand<P> {

    protected IModule<P> module;

    public IModuleExecutor(@NotNull IModule<P> module, @NotNull String[] labels) {
        this(module, labels, null);
    }

    public IModuleExecutor(@NotNull IModule<P> module, @NotNull String[] labels, @Nullable String permission) {
        super(module.plugin, labels, permission);
        this.module = module;
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return "";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

    }
}
