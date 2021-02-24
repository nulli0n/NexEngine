package su.nexmedia.engine.manager.api.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.manager.types.ClickType;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.ActionManipulator;

public abstract class NGUI <P extends NexPlugin<P>> extends IListener<P> implements InventoryHolder {

	protected static final String VALUE_USER_ID = "user_item_";
	
	protected final Set<String> LOCKED_CACHE = new HashSet<>();
	
	protected UUID uuid;
	protected String title;
	protected int size;
	private LinkedHashMap<String, GuiItem> items;
	private Map<Integer, String> slotRefer;
	
	protected Map<String, Map<Integer, String>> userSlotRefer;
	protected Map<String, int[]> userPage;
	protected Set<Player> viewers;
	
	protected int animTick = 0;
	protected boolean animProgress;
	protected int animMaxFrame = -1;
	protected int animFrameCount = 0;
	protected Map<String, Integer> animItemFrames;
	private AnimationTask animTask;
	
	public NGUI(@NotNull P plugin, @NotNull String title, int size) {
		super(plugin);
		
		this.setTitle(title);
		this.setSize(size);
		
		this.uuid = UUID.randomUUID();
		this.items = new LinkedHashMap<>();
		this.slotRefer = new HashMap<>();
		
		this.userSlotRefer = new HashMap<>();
		this.userPage = new HashMap<>();
		this.viewers = new HashSet<>();
		
		this.animTick = 0;
		if (this.isAnimated()) {
			this.animProgress = false;
			
			this.animItemFrames = new HashMap<>();
			this.animTask = new AnimationTask();
			this.animTask.start();
		}
		
		this.registerListeners();
	}
	
	public NGUI(@NotNull P plugin, @NotNull JYML cfg, @NotNull String path) {
		super(plugin);
		if (!path.isEmpty() && !path.endsWith(".")) path += ".";
		
		String title = cfg.getString(path + "title", "");
		int size = cfg.getInt(path + "size", 54);
		
		this.setTitle(title);
		this.setSize(size);
		
		this.uuid = UUID.randomUUID();
		this.items = new LinkedHashMap<>();
		this.slotRefer = new HashMap<>();
		
		this.userSlotRefer = new HashMap<>();
		this.userPage = new HashMap<>();
		this.viewers = new HashSet<>();
		
		this.animTick = cfg.getInt(path + "animation.tick", 0);
		if (this.isAnimated()) {
			this.animProgress = cfg.getBoolean(path + "animation.progressive");
			
			this.animItemFrames = new HashMap<>();
			this.animTask = new AnimationTask();
			this.animTask.start();
		}
		
		this.registerListeners();
	}
	
	public void shutdown() {
		this.viewers.forEach(p -> p.closeInventory());
		this.clear();
	}

	protected final void clear() {
		if (this.animTask != null) {
			this.animTask.stop();
			this.animTask = null;
		}
		this.viewers.clear();
		this.items.clear();
		this.userPage.clear();
		this.slotRefer.clear();
		this.userSlotRefer.clear();
		if (this.animItemFrames != null) {
			this.animItemFrames.clear();
			this.animItemFrames = null;
		}
		this.unregisterListeners();
	}
	
	protected abstract void onCreate(@NotNull Player player, @NotNull Inventory inv, int page);

	protected void onReady(@NotNull Player player, @NotNull Inventory inv, int page) {
		
	}

	public void reopen() {
		this.getViewers().forEach(player -> {
			this.open(player, this.getUserPage(player, 0));
		});
	}
	
	// TODO Experimental
	public void refill() {
		this.getViewers().forEach(player -> {
			InventoryView view = player.getOpenInventory();
			Inventory top = view.getTopInventory();
			if (!(top.getHolder() instanceof NGUI<?>)) return;
			
			for (int slot = 0; slot < top.getSize(); slot++) {
				top.setItem(slot, null);
			}
			
			int page = this.getUserPage(player, 0);
			this.clearUserCache(player);
			this.onCreate(player, top, page);
			this.fillGUI(top, player);
			this.onReady(player, top, page);
			
			this.viewers.add(player);
		});
	}
	
	public void open(@NotNull Player player, int page) {
		if (player.isSleeping()) return;
		
		page = Math.max(1, page);
		
		int maxPage = this.getUserPage(player, 1);
		if (maxPage >= 1) page = Math.min(page, maxPage);
		
		// When we call .openInventory method
		// It runs InventoryCloseEvent if player has opened inventory.
		// So, we clear user cache here and lock
		// player cache until this GUI will be opened
		// to guarantee that no content will be wiped,
		// and for safe gui sliding.
		// 
		// So, this system prevents cache clearing when it's not intended.
		String key = player.getName();
		
		// Only clear old items if player updates current opened GUI.
		// So we can pre-add items to the GUI before open it for the first time to player.
		if (this.viewers.contains(player)) {
			this.clearUserCache(player);
			this.LOCKED_CACHE.add(key);
		}
		
		// Setup animation max. frames for progressive animation type.
		// So all animated items will be animated one after another.
		if (this.animMaxFrame < 0 && this.isAnimationProgressive()) {
			Optional<GuiItem> opt = this.getContent().values().stream().max((item1, item2) -> {
				return item1.getAnimationMaxFrame() - item2.getAnimationMaxFrame();
			});
			this.animMaxFrame = opt.isPresent() ? opt.get().getAnimationMaxFrame() : 0;
		}
		
		Inventory inv = this.getInventory();
		this.onCreate(player, inv, page);
		this.fillGUI(inv, player);
		this.onReady(player, inv, page);
		this.viewers.add(player);
		player.openInventory(inv);
		
		// Unlock cache to allow clear on next open
		this.LOCKED_CACHE.remove(key);
	}

	@Override
	@NotNull
	public final Inventory getInventory() {
		return plugin.getServer().createInventory(this, this.getSize(), this.getTitle());
	}

	public final boolean isAnimated() {
		return this.isAnimationAllowed() && this.animTick > 0;
	}

	public boolean isAnimationAllowed() {
		return true;
	}
	
	public final boolean isAnimationProgressive() {
		return this.animProgress;
	}
	
	public boolean destroyWhenNoViewers() {
		return false;
	}
	
	protected final void setUserPage(@NotNull Player player, int current, int max) {
		String key = player.getName();
		this.userPage.put(key, new int[] {Math.max(1, current), max});
	}
	
	public final int getUserPage(@NotNull Player player, int index) {
		String key = player.getName();
		if (this.userPage.containsKey(key)) {
			index = Math.min(1, Math.max(0, index));
			return this.userPage.get(key)[index];
		}
		return 1; // -1
	}
	
	@NotNull
	protected final List<GuiItem> getUserItems(@NotNull Player player) {
		// List to save item order
		String name = player.getName();
		
		List<GuiItem> list = this.getContent().values().stream().filter(guiItem -> {
			String id = guiItem.getId();
			if (!guiItem.hasPermission(player)) return false;
			if (id.contains(VALUE_USER_ID) && !id.contains(name)) return false;
			return true;
			
		}).collect(Collectors.toList());
		
		return list;
	}

	@Nullable
	protected final GuiItem getButton(@NotNull Player player, int slot) {
		String id = this.getUserContent(player).getOrDefault(slot, this.slotRefer.get(slot));
		return id != null ? this.items.get(id) : null;
	}
	
	// TODO removeButton method.
	
	public final void addButton(@NotNull GuiItem guiItem) {
		String id = guiItem.getId();
		
		// TODO remove same item slot from user slot refer? like override?
		for (int slot : guiItem.getSlots()) {
			this.slotRefer.put(slot, id);
		}
		this.items.put(id, guiItem);
	}
	
	protected final void addButton(@NotNull Player player, @NotNull JIcon icon, int... slots) {
		String id = VALUE_USER_ID + player.getName() + this.items.size();
		ItemStack item = icon.build();
		
		GuiItem guiItem = new GuiItem(id, null, item, false, 0, new TreeMap<>(), new HashMap<>(), null, slots);
		guiItem.setClick(icon.getClick());
		
		Map<Integer, String> userMap = this.getUserContent(player);
		for (int slot : guiItem.getSlots()) {
			userMap.put(slot, id);
		}
		this.userSlotRefer.put(player.getName(), userMap);
		this.items.put(id, guiItem);
	}
	
	@NotNull
	protected final ItemStack getItem(@NotNull Inventory inv, int slot) {
		ItemStack item = inv.getItem(slot);
		return item == null ? new ItemStack(Material.AIR) : new ItemStack(item);
	}
	
	@NotNull
	protected final ItemStack takeItem(@NotNull Inventory inv, int slot) {
		ItemStack item = inv.getItem(slot);
		inv.setItem(slot, null);
		return item == null ? new ItemStack(Material.AIR) : item;
	}
	
	protected void fillGUI(@NotNull Inventory inv, @NotNull Player player) {
		// Auto paginator
		int page = this.getUserPage(player, 0);
		int pages = this.getUserPage(player, 1);
		
		for (GuiItem guiItem : this.getUserItems(player)) {
			
			if (guiItem.getType() == ContentType.NEXT) {
				if (page < 0 || pages < 0 || page >= pages)  {
					continue;
				}
			}
			if (guiItem.getType() == ContentType.BACK) {
				if (page <= 1) {
					continue;
				}
			}
			
			ItemStack item = null;
			
			this.replaceFrame(player, guiItem); // Method for interactive item frame changes on click
			
			if (this.isAnimated() && guiItem.isAnimationAutoPlay()) {
				String id = guiItem.getId();
				int frame = 0;
				if (this.animMaxFrame > 0) {
					frame = this.animFrameCount;
				}
				else {
					frame = this.animItemFrames.computeIfAbsent(id, frameStored -> 0);
				}
				item = guiItem.getAnimationFrame(frame);
			}
			if (item == null) {
				item = guiItem.getItem();
			}
			
			this.replaceMeta(player, item, guiItem);
			ItemUT.applyPlaceholderAPI(player, item);
			
			for (int slot : guiItem.getSlots()) {
				if (slot >= inv.getSize()) continue;
				inv.setItem(slot, item);
			}
		}
		this.replaceMeta(player, inv);
	}
	
	protected void replaceFrame(@NotNull Player player, @NotNull GuiItem guiItem) {
		
	}
	
	protected void replaceMeta(@NotNull Player player, @NotNull Inventory inv) {
		
	}
	
	protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
		
	}
	
	protected abstract boolean ignoreNullClick();
	
	protected abstract boolean cancelClick(int slot);
	
	protected abstract boolean cancelPlayerClick();
	
	@NotNull
	public Set<Player> getViewers() {
		return new HashSet<>(this.viewers);
	}
	
	@NotNull
	public UUID getUUID() {
		return this.uuid;
	}
	
	@NotNull
	public final String getTitle() {
		return this.title;
	}
	
	public final void setTitle(@NotNull String title) {
		this.title = StringUT.color(title);
	}
	
	public final int getSize() {
		return this.size;
	}
	
	public final void setSize(int size) {
		this.size = size;
	}
	
	@NotNull
	public final LinkedHashMap<String, GuiItem> getContent() {
		return this.items;
	}
	
	@NotNull
	public final Map<Integer, String> getUserContent(@NotNull Player player) {
		return this.userSlotRefer.computeIfAbsent(player.getName(), map -> new HashMap<>());
	}
	
	protected final boolean clearUserCache(@NotNull Player player) {
		String key = player.getName();
		if (this.LOCKED_CACHE.contains(key)) {
			//System.out.println("Cache locked...");
			return false;
		}
		
		//System.out.println("Cache cleared!");
		
		for (GuiItem guiItem : new ArrayList<>(this.items.values())) {
			if (guiItem.getId().contains(key)) {
				this.items.remove(guiItem.getId());
			}
		}
		
		this.userSlotRefer.remove(key);
		this.userPage.remove(key);
		this.viewers.remove(player);
		return true;
	}
	
	protected final boolean isCacheLocked(@NotNull Player player) {
		return this.LOCKED_CACHE.contains(player.getName());
	}
	
	protected final boolean isPlayerInv(int slot) {
		return slot >= this.getSize();
	}
	
	protected void click(
			@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e) {
		
		GuiItem guiItem = this.getButton(player, slot);
		if (guiItem == null || !guiItem.hasPermission(player)) return;
		
		Enum<?> type = guiItem.getType();
		guiItem.click(player, type, e);
		
		// Execute custom user actions when click button.
		ClickType clickType = ClickType.from(e);
		ActionManipulator actions = guiItem.getCustomClick(clickType);
		if (actions != null) {
			actions.process(player);
		}
	}
	
	protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
		if (this.getViewers().isEmpty() && this.destroyWhenNoViewers()) {
			this.clear();
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEventClick(InventoryClickEvent e) {
		InventoryHolder ih = e.getInventory().getHolder();
		if (ih == null || !(ih.getClass().isInstance(this))) return;
		
		NGUI<?> g = (NGUI<?>) ih;
		if (!g.getUUID().equals(this.getUUID())) return;
		
		int slot = e.getRawSlot();
		
		if (this.cancelClick(slot)) {
			if (!this.isPlayerInv(slot) || this.cancelPlayerClick()) {
				e.setCancelled(true);
			}
		}
		
		//e.setCancelled(this.cancelClick());
		ItemStack item = e.getCurrentItem();
		if (this.ignoreNullClick() && (item == null || ItemUT.isAir(item))) return;
		
		this.click((Player) e.getWhoClicked(), item, e.getRawSlot(), e);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEventClose(InventoryCloseEvent e) {
		InventoryHolder ih = e.getInventory().getHolder();
		if (ih == null || !(ih.getClass().isInstance(this))) return;
		
		NGUI<?> g = (NGUI<?>) ih;
		if (!g.getUUID().equals(this.getUUID())) return;
		
		Player p = (Player) e.getPlayer();
		
		// Remove player-related buttons
		this.clearUserCache(p);
		
		this.onClose(p, e);
	}
	
	class AnimationTask extends ITask<P> {

		public AnimationTask() {
			super(NGUI.this.plugin, (long) NGUI.this.animTick, false);
		}

		@Override
		public void action() {
			if (NGUI.this.viewers.isEmpty()) return;
			
			if (NGUI.this.animMaxFrame > 0) {
				if (NGUI.this.animFrameCount++ >= NGUI.this.animMaxFrame) {
					NGUI.this.animFrameCount = 0;
				}
			}
			else {
				NGUI.this.animItemFrames.keySet().forEach(itemId -> {
					GuiItem guiItem = items.get(itemId);
					animItemFrames.compute(itemId, (id, frame) -> {
						frame += 1;
						if (frame > guiItem.getAnimationMaxFrame()) frame = 0;
						return frame;
					});
				});
			}
			
			NGUI.this.getViewers().forEach(player -> {
				Inventory inv = player.getOpenInventory().getTopInventory();
				NGUI.this.fillGUI(inv, player);
			});
		}
	}
}
