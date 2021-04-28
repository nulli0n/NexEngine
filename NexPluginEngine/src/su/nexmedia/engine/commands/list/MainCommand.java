package su.nexmedia.engine.commands.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.commands.api.IGeneralCommand;

public final class MainCommand<P extends NexPlugin<P>> extends IGeneralCommand<P> {

    public MainCommand(@NotNull P plugin) {
        super(plugin, plugin.getLabels());
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    @NotNull
    public String description() {
        return "";
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    public void perform(@NotNull CommandSender sender, String label, @NotNull String[] args) {
        // We do not need to put here anything as this command has defaultCommand in
        // CommandManager.
    }
}
