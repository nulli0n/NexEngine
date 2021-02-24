package su.nexmedia.engine.data.users;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexDataPlugin;
import su.nexmedia.engine.data.event.EngineUserCreatedEvent;
import su.nexmedia.engine.data.event.EngineUserLoadEvent;
import su.nexmedia.engine.data.event.EngineUserUnloadEvent;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.manager.IManager;
import su.nexmedia.engine.manager.api.task.ITask;

public abstract class IUserManager<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends IManager<P> {

	private Map<String, U> activeUsers;
	private Set<@NotNull U> toSave;
	private SaveTask saveTask;
	
	private Set<UUID> isPassJoin;
	private Set<UUID> toCreate;
	
	public IUserManager(@NotNull P plugin) {
		super(plugin);
	}
	
	@Override
	public void setup() {
		this.activeUsers = new HashMap<>();
		this.toSave = ConcurrentHashMap.newKeySet();
		this.isPassJoin = ConcurrentHashMap.newKeySet();
		this.toCreate = ConcurrentHashMap.newKeySet();
		
		this.registerListeners();
		
		this.saveTask = new SaveTask(plugin);
		this.saveTask.start();
	}
	
	@Override
	public void shutdown() {
		this.unregisterListeners();
		
		if (this.saveTask != null) {
			this.saveTask.stop();
			this.saveTask = null;
		}
		this.autosave();
		this.activeUsers.clear();
		this.isPassJoin.clear();
		this.toCreate.clear();
	}
	
	public void loadOnlineUsers() {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			if (p == null) continue;
			this.getOrLoadUser(p);
		}
	}
	
	public void autosave() {
		int cacheFixCount = 0;
		for (U userOn : new HashSet<>(this.getActiveUsers())) {
			if (!userOn.isOnline()) {
				this.toSave.add(userOn);
				this.activeUsers.remove(userOn.getUUID().toString());
				cacheFixCount++;
				continue;
			}
			this.save(userOn);
		}
		
		int on = this.activeUsers.size();
		int off = this.toSave.size();
		this.toSave.forEach(userOff -> this.save(userOff));
		this.toSave.clear();
		
		plugin.info("Auto-save: Saved " + on + " online users | " + off + " offline users.");
		if (cacheFixCount > 0) {
			plugin.info("Saved and cleaned " + cacheFixCount + " offline loaded users.");
		}
	}
	
	public void save(@NotNull U user, boolean async) {
		if (async) {
			this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				this.save(user);
			});
			return;
		}
		this.save(user);
	}
	
	public void save(@NotNull U user) {
		this.plugin.getData().saveUser(user);
	}
	
	@NotNull
	protected abstract U createData(@NotNull Player player);
	
	@Nullable
	public U getOrLoadUser(@NotNull Player player) {
		if (Hooks.isNPC(player)) {
			throw new IllegalStateException("Could not load user data from an NPC!");
		}
		
		@Nullable U user = this.getOrLoadUser(player.getUniqueId().toString(), true);
		/*if (user == null) {
			throw new IllegalStateException("Could not load user data from an online player!");
		}*/
		return user;
	}
	
	@Nullable
	public final U getOrLoadUser(@NotNull String uuid, boolean isId) {
		Player playerHolder = null;
		if (isId) {
			playerHolder = plugin.getServer().getPlayer(UUID.fromString(uuid));
		}
		else {
			playerHolder = plugin.getServer().getPlayer(uuid);
			if (playerHolder != null) {
				isId = true;
				uuid = playerHolder.getUniqueId().toString();
			}
			// Check if user was already loaded, but offline and not unloaded
			for (U userOff : this.getActiveUsers()) {
				if (userOff.getName().equalsIgnoreCase(uuid)) {
					return userOff;
				}
			}
		}
		
		// Check if user is loaded.
		@Nullable U user = this.activeUsers.get(uuid);
		if (user != null) return user;
		
		// Check if user exists, but was unloaded and moved to save cache.
		for (U userOff : this.toSave) {
			if (userOff.getUUID().toString().equalsIgnoreCase(uuid) || userOff.getName().equalsIgnoreCase(uuid)) {
				this.toSave.remove(userOff);
				this.activeUsers.put(userOff.getUUID().toString(), userOff);
				return userOff;
			}
		}
		
		// Try to load user from the database.
		user = plugin.getData().getUser(uuid, isId);
		if (user != null) {
			final U user2 = user;
			this.activeUsers.put(user.getUUID().toString(), user2);
			
			// Игрок уже успел войти полностью на сервер (пройти JoinEvent)
			// поэтому кастомный ивент в JoinEvent вызван не будет, а значит
			// вызываем его здесь в основном потоке.
			if (this.isPassJoin.remove(user2.getUUID())) {
				this.plugin.getServer().getScheduler().runTask(plugin, () -> {
					this.onUserLoad(user2);
					EngineUserLoadEvent<P, U> event = new EngineUserLoadEvent<>(plugin, user2);
					plugin.getPluginManager().callEvent(event);
				});
			}
			return user2;
		}
		
		if (playerHolder == null) {
			return null;
		}
		
		user = this.createData(playerHolder);
		
		EngineUserCreatedEvent<P, U> event = new EngineUserCreatedEvent<>(plugin, user);
		plugin.getPluginManager().callEvent(event);
		
		final U user2 = user;
		
		this.plugin.info("Created new user data for: '" + uuid + "'");
		this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.plugin.getData().addUser(user2);
		});
		this.activeUsers.put(uuid, user2);
		this.toCreate.remove(user2.getUUID());
		return user2;
	}
	
	public final void unloadUser(@NotNull Player player) {
		this.unloadUser(player.getUniqueId().toString());
	}
	
	public final void unloadUser(@NotNull String uuid) {
		@Nullable U user = this.activeUsers.get(uuid);
		if (user == null) return;
		
		this.onUserUnload(user);
		
		EngineUserUnloadEvent<P, U> event = new EngineUserUnloadEvent<>(plugin, user);
		plugin.getPluginManager().callEvent(event);
		
		user.setLastOnline(System.currentTimeMillis());
		
		if (plugin.cfg().dataSaveInstant) {
			this.save(user, true);
		}
		else {
			this.toSave.add(user);
		}
		this.activeUsers.remove(uuid);
	}
	
	@Deprecated
	protected void onUserUnload(@NotNull U user) {
		
	}
	
	@Deprecated
	protected void onUserLoad(@NotNull U user) {
		
	}
	
	@NotNull
	public Map<String, @NotNull U> getActiveUsersMap() {
		return this.activeUsers;
	}

	@NotNull
	public Collection<@NotNull U> getActiveUsers() {
		return this.activeUsers.values();
	}
	
	@NotNull
	public Set<@NotNull U> getInactiveUsers() {
		return this.toSave;
	}
	
	public boolean isLoaded(@NotNull Player player) {
		return this.activeUsers.containsKey(player.getUniqueId().toString());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserLogin(AsyncPlayerPreLoginEvent e) {
		if (e.getLoginResult() != Result.ALLOWED) return;
		
		// For new players, prepare the UserManager to create new data on PlayerJoinEvent.
		if (!this.plugin.getData().isUserExists(e.getUniqueId().toString(), true)) {
			this.toCreate.add(e.getUniqueId());
			return;
		}
		
		// For old players, load the user data from the database in async mode.
		this.getOrLoadUser(e.getUniqueId().toString(), true);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onUserJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		// Добавляем игрока в джойн лист для дальнейших проверок.
		this.isPassJoin.add(player.getUniqueId());
		
		// Если игрок до сих пор не был загружен из БД и при этом запись о нем есть в базе,
		// мы выходим из метода, оставляя его в "джойн" листе, таким образом
		// при завершении загрузки из БД, в методе getOrLoadUser менеджер увидит
		// его и загрузит в память с вызовом кастомного ивента.
		if (!this.isLoaded(player) && !this.toCreate.contains(player.getUniqueId())) return;
		
		// Так как при загрузке данных в асихнронном режиме мы не можем получить объект игрока,
		// то мы получаем уже загруженные его данные здесь для вызова кастомного ивента.
		// Либо здесь же создаются новые данные если игрока не было в базе.
		@Nullable U user = this.getOrLoadUser(player);
		if (user == null) return;
		
		
		this.onUserLoad(user);
		// Call custom UserLoad event.
		EngineUserLoadEvent<P, U> event = new EngineUserLoadEvent<>(plugin, user);
		plugin.getPluginManager().callEvent(event);
		
		this.isPassJoin.remove(user.getUUID());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onUserQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		this.unloadUser(player);
	}
	
	class SaveTask extends ITask<P> {

		SaveTask(@NotNull P plugin) {
			super(plugin, plugin.cfg().dataSaveInterval * 60, true);
		}

		@Override
		public void action() {
			autosave();
		}
	}
}
