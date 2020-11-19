package su.nexmedia.engine.manager.editor;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.constants.JStrings;

public abstract class EditorHandler<P extends NexPlugin<P>> extends IListener<P> {

	private Class<?> type;
	private NGUI<P> main;
	
	public EditorHandler(@NotNull P plugin, Class<?> type, @Nullable NGUI<P> main) {
		super(plugin);
		if (!type.isEnum()) {
			throw new IllegalArgumentException("Type must be Enum!");
		}
		this.type = type;
		this.main = main;
		
		this.registerListeners();
	}
	
	public void shutdown() {
		this.unregisterListeners();
	}
	
	public void open(@NotNull Player p, int page) {
		if (this.main == null) return;
		
		this.main.open(p, page);
	}
	
	@Nullable
	public NGUI<P> getMainEditor() {
		return this.main;
	}
	
	protected abstract boolean onType(
			@NotNull Player p, 
			@Nullable Object editObject, 
			@NotNull Enum<?> type, 
			@NotNull String msg);
	
	public final void endEdit (@NotNull Player player) {
		EditorManager.endEdit(player);
	}	
	
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
    	Player p = e.getPlayer();
    	
    	Map.Entry<Enum<?>, Object> editor = EditorManager.getEditor(p);
    	if (editor == null) return;
    	
    	Enum<?> type = editor.getKey();
    	if (!type.getClass().equals(this.type)) return;
    	
    	e.getRecipients().clear();
    	e.setCancelled(true);
    	
    	String msg = StringUT.color(e.getMessage());
    	if (msg.equalsIgnoreCase(JStrings.EXIT)) {
    		EditorManager.endEdit(p);
    		return;
    	}
    	
    	// Sync Output Handler
    	// to avoid exceptions when changing non-async objects
    	this.plugin.getServer().getScheduler().runTask(plugin, () -> {
			if (this.onType(p, editor.getValue(), type, msg)) {
		    	EditorManager.endEdit(p);
		    }
		});
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEditorCommand(PlayerCommandPreprocessEvent e) {
    	Player p = e.getPlayer();
    	String msg = StringUT.color(e.getMessage().substring(1));
    	Map.Entry<Enum<?>, Object> editor = EditorManager.getEditor(p);
    	if (editor != null || msg.equalsIgnoreCase(JStrings.EXIT)) {
    		e.setCancelled(true);
    	}
    	if (editor == null) return;
    	
    	Enum<?> type = editor.getKey();
    	if (!type.getClass().equals(this.type)) return;
    	
    	if (msg.equalsIgnoreCase(JStrings.EXIT)) {
    		EditorManager.endEdit(p);
    		return;
    	}
    	
    	// Sync Output Handler
    	// to avoid exceptions when changing non-async objects
    	this.plugin.getServer().getScheduler().runTask(plugin, () -> {
			if (this.onType(p, editor.getValue(), type, msg)) {
		    	EditorManager.endEdit(p);
		    }
		});
    }
}
