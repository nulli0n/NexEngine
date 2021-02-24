package su.nexmedia.engine.data.event;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexDataPlugin;
import su.nexmedia.engine.data.users.IAbstractUser;

public class EngineUserUnloadEvent<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends EngineUserEvent<P, U> {

	public EngineUserUnloadEvent(@NotNull P plugin, @NotNull U user) {
		super(plugin, user);
	}
}
