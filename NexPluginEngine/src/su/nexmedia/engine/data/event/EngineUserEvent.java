package su.nexmedia.engine.data.event;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexDataPlugin;
import su.nexmedia.engine.data.users.IAbstractUser;
import su.nexmedia.engine.manager.api.event.IEvent;

public abstract class EngineUserEvent<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends IEvent {

	private @NotNull P plugin;
	private @NotNull U user;
	
	public EngineUserEvent(@NotNull P plugin, @NotNull U user) {
		this.plugin = plugin;
		this.user = user;
	}
	
	@NotNull
	public P getPlugin() {
		return this.plugin;
	}
	
	@NotNull
	public U getUser() {
		return this.user;
	}
}
