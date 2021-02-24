package su.nexmedia.engine.data.event;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexDataPlugin;
import su.nexmedia.engine.data.users.IAbstractUser;

public class EngineUserLoadEvent<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends EngineUserEvent<P, U> {

	public EngineUserLoadEvent(@NotNull P plugin, @NotNull U user) {
		super(plugin, user);
	}
}
