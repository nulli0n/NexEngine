package su.nexmedia.engine.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.commands.api.ISubCommand;

public class EditorCommand<P extends NexPlugin<P>> extends ISubCommand<P> {

    public EditorCommand(@NotNull P plugin) {
        super(plugin, new String[] { "editor" }, plugin.getNameRaw() + ".cmd.editor");
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Core_Command_Editor_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    public void perform(@NotNull CommandSender sender, String label, @NotNull String[] args) {
        Player player = (Player) sender;
        this.plugin.openEditor(player);
    }

}
