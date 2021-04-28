package su.nexmedia.engine.commands.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.StringUT;

public abstract class IGeneralCommand<P extends NexPlugin<P>> extends IAbstractCommand<P>
        implements CommandExecutor, TabExecutor {

    private Map<String, ISubCommand<P>> subCommands;
    private ISubCommand<P> defaultCommand;

    public IGeneralCommand(@NotNull P plugin, @NotNull List<String> aliases) {
        this(plugin, aliases.toArray(new String[aliases.size()]));
    }

    public IGeneralCommand(@NotNull P plugin, @NotNull String[] aliases) {
        this(plugin, aliases, null);
    }

    public IGeneralCommand(@NotNull P plugin, @NotNull List<String> aliases, @Nullable String permission) {
        this(plugin, aliases.toArray(new String[aliases.size()]), permission);
    }

    public IGeneralCommand(@NotNull P plugin, @NotNull String[] aliases, @Nullable String permission) {
        super(plugin, aliases, permission);
        this.subCommands = new LinkedHashMap<>();
    }

    public void addSubCommand(@NotNull ISubCommand<P> cmd) {
        for (String alias : cmd.labels()) {
            this.subCommands.put(alias, cmd);
        }
        cmd.setParent(this);
    }

    public void addDefaultCommand(@NotNull ISubCommand<P> cmd) {
        this.addSubCommand(cmd);
        this.defaultCommand = cmd;
    }

    public void clearSubCommands() {
        this.subCommands.clear();
    }

    public void removeSubCommand(@NotNull String alias) {
        this.subCommands.values().removeIf(cmd -> ArrayUtils.contains(cmd.labels(), alias));
    }

    @NotNull
    public Collection<ISubCommand<P>> getSubCommands() {
        return new LinkedHashSet<>(this.subCommands.values()); // HashSet to avoid duplicates
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (this.subCommands.isEmpty() || (args.length == 0 && this.defaultCommand == null)) {
            this.execute(sender, label, args);
            return true;
        }

        ISubCommand<P> command = this.defaultCommand;
        if (args.length > 0 && this.subCommands.containsKey(args[0])) {
            command = this.subCommands.get(args[0]);
        }
        if (command == null) {
            return false;
        }

        command.execute(sender, label, args);
        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || args.length == 0) {
            return Collections.emptyList();
        }

        if (this.subCommands.isEmpty()) {
            if (!this.hasPerm(sender)) {
                return Collections.emptyList();
            }
            List<String> list = this.getTab((Player) sender, args.length, args);
            return StringUT.getByFirstLetters(args[args.length - 1], list);
        }

        if (args.length == 1) {
            List<String> sugg = new ArrayList<>();
            for (ISubCommand<P> sub : this.getSubCommands()) {
                if (sub.hasPerm(sender)) {
                    sugg.addAll(Arrays.asList(sub.labels()));
                }
            }
            return StringUT.getByFirstLetters(args[0], sugg);
        }

        ISubCommand<P> sub = this.subCommands.get(args[0]);
        if (sub == null || !sub.hasPerm(sender))
            return Collections.emptyList();

        List<String> list = sub.getTab((Player) sender, args.length - 1, args);
        return StringUT.getByFirstLetters(args[args.length - 1], list);
    }
}
