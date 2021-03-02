package su.nexmedia.engine.commands.api;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.CollectionsUT;

public abstract class IAbstractCommand<P extends NexPlugin<P>> {

	@NotNull protected P plugin;
	protected String[] aliases;
	protected String permission;
	
	public IAbstractCommand(@NotNull P plugin, @NotNull List<String> aliases) {
		this(plugin, aliases.toArray(new String[aliases.size()]));
	}
	
	public IAbstractCommand(@NotNull P plugin, @NotNull String[] aliases) {
		this(plugin, aliases, null);
	}
	
	public IAbstractCommand(@NotNull P plugin, @NotNull List<String> aliases, @Nullable String permission) {
		this(plugin, aliases.toArray(new String[aliases.size()]), permission);
	}
	
	public IAbstractCommand(@NotNull P plugin, @NotNull String[] aliases, @Nullable String permission) {
		this.plugin = plugin;
		this.aliases = aliases;
		this.permission = permission;
	}
    
	@NotNull
	public final String[] labels() {
		return this.aliases;
	}
	
	@Nullable
	public final String getPermission() {
		return this.permission;
	}
	
	@NotNull
	public abstract String usage();
	
	@NotNull
	public abstract String description();
	
	public abstract boolean playersOnly();

	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String @NotNull [] args) {
		return Collections.emptyList();
	}
	
	protected abstract void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args);
	
	public final void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (this.playersOnly() && !(sender instanceof Player)) {
			this.errSender(sender);
			return;
		}
		if (!this.hasPerm(sender)) {
			this.errPerm(sender);
			return;
		}
		this.perform(sender, label, args);
	}
	
    public final boolean hasPerm(@NotNull CommandSender sender) {
    	return this.permission == null || sender.hasPermission(this.permission);
    }
	
	protected final void printUsage(@NotNull CommandSender sender) {
		String mainLabel;
		String subLabel;
		if (this instanceof IGeneralCommand) {
			mainLabel = this.labels()[0];
			subLabel = "";
		}
		else if (this instanceof ISubCommand) {
			mainLabel = ((ISubCommand<?>)this).parent.labels()[0];
			subLabel = this.labels()[0];
		}
		else return;
		
		plugin.lang().Core_Command_Usage
			.replace("%usage%", usage())
			.replace("%cmd%", subLabel)
			.replace("  ", " ")
			.replace("%label%", mainLabel)
			.send(sender);
	}
	
	protected final void errPerm(@NotNull CommandSender sender) {
		plugin.lang().Error_NoPerm.send(sender);
	}
	
	protected final void errItem(@NotNull CommandSender sender) {
		plugin.lang().Error_NoItem.send(sender);
	}
	
	protected final void errPlayer(@NotNull CommandSender sender) {
		plugin.lang().Error_NoPlayer.send(sender);
	}
	
	protected final void errSender(@NotNull CommandSender sender) {
		plugin.lang().Error_Sender.send(sender);
	}
	
	protected final void errType(@NotNull CommandSender sender, @NotNull Class<?> clazz) {
		plugin.lang().Error_Type
		.replace("%types%", CollectionsUT.getEnums(clazz)).send(sender);
	}
	
    protected final double getNumD(@NotNull CommandSender sender, @NotNull String input, double def) {
        return getNumD(sender, input, def, false);
    }
    
    protected final double getNumD(@NotNull CommandSender sender, @NotNull String input, double def, boolean allowNega) {
        try {
            double amount = Double.parseDouble(input);
            if (amount < 0.0 && !allowNega) {
                throw new NumberFormatException();
            }
            return amount;
        }
        catch (NumberFormatException ex) {
        	plugin.lang().Error_Number.replace("%num%", input).send(sender);
            return def;
        }
    }
    
    protected final int getNumI(@NotNull CommandSender sender, @NotNull String input, int def) {
        return getNumI(sender, input, def, false);
    }
    
    protected final int getNumI(@NotNull CommandSender sender, @NotNull String input, int def, boolean nega) {
        return (int) getNumD(sender, input, def, nega);
    }
}
