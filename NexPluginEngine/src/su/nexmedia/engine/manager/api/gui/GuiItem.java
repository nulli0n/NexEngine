package su.nexmedia.engine.manager.api.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.manager.types.ClickType;
import su.nexmedia.engine.utils.actions.ActionManipulator;

public class GuiItem {

    private String id;
    private Enum<?> type;
    private ItemStack item;

    private boolean animAutoPlay;
    private int animStartFrame;
    private TreeMap<Integer, ItemStack> animFrames;

    private String permission;
    private int[] slots;
    private GuiClick click;
    private Map<ClickType, ActionManipulator> customClicks;

    public GuiItem(@NotNull String id, @Nullable Enum<?> type, @NotNull ItemStack item,

            boolean animAutoPlay, int animStartFrame, @NotNull TreeMap<Integer, ItemStack> animFrames,
            @NotNull Map<ClickType, ActionManipulator> customClicks,

            @Nullable String permission, int[] slots) {
        this.setId(id);
        this.setType(type);
        this.setItem(item);
        this.setSlots(slots);

        this.setAnimationAutoPlay(animAutoPlay);
        this.setAnimationStartFrame(animStartFrame);
        this.animFrames = new TreeMap<>();
        for (Entry<Integer, ItemStack> e : animFrames.entrySet()) {
            ItemStack frame = new ItemStack(e.getValue());
            this.replaceFrameGlobals(frame);

            this.animFrames.put(e.getKey(), frame);
        }

        this.setCustomClicks(customClicks);
        this.setPermission(permission);
    }

    public GuiItem(@NotNull GuiItem from) {
        this(from.getId(), from.getType(), from.getItemRaw(),

                from.isAnimationAutoPlay(), from.getAnimationStartFrame(), from.getAnimationFrames(),

                from.getCustomClicks(),

                from.getPermission(), from.getSlots());
        this.setClick(from.getClick());
    }

    private void replaceFrameGlobals(@NotNull ItemStack frame) {
        ItemMeta meta = frame.getItemMeta();
        if (meta == null)
            return;

        ItemMeta metaGlobal = this.item.getItemMeta();
        if (metaGlobal == null)
            return;

        String name = meta.getDisplayName().replace("%GLOBAL%", metaGlobal.getDisplayName());

        List<String> lore = meta.getLore();
        List<String> loreOrig = metaGlobal.getLore();
        List<String> lore2 = new ArrayList<>();
        if (lore != null) {
            for (String line : lore) {
                if (line.equalsIgnoreCase("%GLOBAL%")) {
                    if (loreOrig != null) {
                        lore2.addAll(loreOrig);
                    }
                    continue;
                }
                lore2.add(line);
            }
        }

        meta.setDisplayName(name);
        meta.setLore(lore2);
        frame.setItemMeta(meta);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    @Nullable
    public Enum<?> getType() {
        return this.type;
    }

    public void setType(@Nullable Enum<?> type) {
        this.type = type;
    }

    @NotNull
    public ItemStack getItemRaw() {
        return new ItemStack(this.item);
    }

    @NotNull
    public ItemStack getItem() {
        ItemStack item = this.getAnimationFrame(this.getAnimationStartFrame());
        if (item == null) {
            return this.getItemRaw();
        }
        return item;
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    //

    public boolean isAnimationAutoPlay() {
        return this.animAutoPlay;
    }

    public void setAnimationAutoPlay(boolean animAutoPlay) {
        this.animAutoPlay = animAutoPlay;
    }

    //

    public int getAnimationStartFrame() {
        return this.animStartFrame;
    }

    public void setAnimationStartFrame(int animStartFrame) {
        this.animStartFrame = animStartFrame;
    }

    //

    @NotNull
    public TreeMap<Integer, ItemStack> getAnimationFrames() {
        return this.animFrames;
    }

    @Nullable
    public ItemStack getAnimationFrame(int index) {
        if (this.animFrames.containsKey(index)) {
            return new ItemStack(this.animFrames.get(index));
        }
        return null;
    }

    public int getAnimationMaxFrame() {
        return this.animFrames.isEmpty() ? 0 : this.animFrames.lastKey();
    }

    public void addAnimationFrame(int index, @NotNull ItemStack frame) {
        this.animFrames.put(index, new ItemStack(frame));
    }

    public int[] getSlots() {
        return this.slots;
    }

    public void setSlots(int[] slot) {
        this.slots = slot;
    }

    @Nullable
    public String getPermission() {
        return this.permission;
    }

    public void setPermission(@Nullable String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(@NotNull Player p) {
        return this.permission == null || p.hasPermission(this.permission);
    }

    @NotNull
    public Map<ClickType, ActionManipulator> getCustomClicks() {
        return this.customClicks;
    }

    public void setCustomClicks(@NotNull Map<ClickType, ActionManipulator> customClicks) {
        this.customClicks = customClicks;
    }

    @Nullable
    public ActionManipulator getCustomClick(@NotNull ClickType clickType) {
        return this.getCustomClicks().get(clickType);
    }

    @Nullable
    public GuiClick getClick() {
        return this.click;
    }

    public void click(@NotNull Player p, @Nullable Enum<?> click, @NotNull InventoryClickEvent e) {
        if (this.click == null)
            return;

        this.click.click(p, click, e);
    }

    public void setClick(@Nullable GuiClick click) {
        this.click = click;
    }
}
