package su.nexmedia.engine.hooks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.manager.IManager;

public class HookManager extends IManager<NexEngine> {

	private Map<String, Set<NHook<?>>> hooks;
	
	public HookManager(@NotNull NexEngine plugin) {
		super(plugin);
	}
	
	@Override
	public void setup() {
		this.hooks = new HashMap<>();
	}
	
	@Override
	public void shutdown() {
		this.hooks.values().forEach(hooks -> hooks.forEach(hook -> hook.unhook()));
		this.hooks.clear();
	}

	public void shutdown(@NotNull NexPlugin<?> holder) {
		this.getHooks(holder).forEach(hook -> hook.unhook());
		this.hooks.remove(holder.getName());
	}
	
	@Nullable
	public <T extends NHook<?>> T register(@NotNull NexPlugin<?> holder, @NotNull String pluginName, @NotNull Class<T> clazz) {
		if (!Hooks.hasPlugin(pluginName)) return null;
		
		T hook;
		try {
			hook = clazz.getConstructor(holder.getClass()).newInstance(holder);
			if (hook != null) {
				hook.pluginName = pluginName;
				return this.register(holder, hook);
			}
		} 
		catch (Exception | NoClassDefFoundError e) {
			holder.error("Could not initialize hook for '" + clazz.getSimpleName() + "' !");
			e.printStackTrace();
		}
		return null;
	}
	
	@Nullable
	private <T extends NHook<?>> T register(@NotNull NexPlugin<?> holder, @NotNull T hook) {
		hook.hook();
    	holder.info("[Hook] " + hook.getPlugin() + ": " + hook.getState().getName());
    	
    	// Register only successful hooks.
    	if (hook.getState() == HookState.SUCCESS) {
    		this.getHooks(holder).add(hook);
    		return hook;
    	}
    	return null;
   }
	
	@NotNull
	public Set<NHook<?>> getHooks(@NotNull NexPlugin<?> holder) {
		return this.hooks.computeIfAbsent(holder.getName(), hooks -> new HashSet<>());
	}
	
	@Nullable
	public <T extends NHook<?>> T getHook(@NotNull NexPlugin<?> holder, @NotNull Class<T> clazz) {
		for (NHook<?> hook : this.getHooks(holder)) {
            if (clazz.isAssignableFrom(hook.getClass())) {
                return clazz.cast(hook);
            }
		}
        return holder.isEngine() ? null : this.getHook(this.plugin, clazz);
    }
	
	@Nullable
	public NHook<?> getHook(@NotNull NexPlugin<?> holder, @NotNull String name) {
		for (NHook<?> hook : this.getHooks(holder)) {
			if (hook.getPlugin().equalsIgnoreCase(name)) {
				return hook;
			}
		}
		return holder.isEngine() ? null : this.getHook(this.plugin, name);
    }
	
	public boolean isHooked(@NotNull NexPlugin<?> holder, @NotNull Class<? extends NHook<?>> clazz) {
		return this.getHook(holder, clazz) != null;
    }
	
	public boolean isHooked(@NotNull NexPlugin<?> holder, @NotNull String name) {
		return this.getHook(holder, name) != null;
    }
}
