package su.nexmedia.engine.manager.editor.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.manager.editor.EditorType;
import su.nexmedia.engine.manager.editor.object.IEditorActionsMain.ActionBuilder;
import su.nexmedia.engine.utils.ClickText;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.ClickText.ClickWord;
import su.nexmedia.engine.utils.actions.ActionCategory;
import su.nexmedia.engine.utils.actions.Parametized;

public class IEditorActionsParametized<P extends NexPlugin<P>> extends NGUI<P> {

    private ActionCategory sectionType;
    private IEditorActionsSection<P> sectionEditor;
    private Map<String, IEditorActionsParams<P>> paramEditors;

    private static int[] objSlots;
    private static String objName;
    private static List<String> objLore;

    public IEditorActionsParametized(@NotNull P plugin, @NotNull IEditorActionsSection<P> section,
            @NotNull ActionCategory category) {
        super(plugin, EditorManager.EDITOR_ACTIONS_PARAMETIZED, "");
        this.sectionEditor = section;
        this.sectionType = category;
        this.paramEditors = new HashMap<>();

        JYML cfg = EditorManager.EDITOR_ACTIONS_PARAMETIZED;
        objSlots = cfg.getIntArray("object-slots");
        objName = StringUT.color(cfg.getString("object-name", "%subject%"));
        objLore = StringUT.color(cfg.getStringList("object-lore"));

        GuiClick click = (p, type, e) -> {
            if (type == null)
                return;

            Class<?> clazz = type.getClass();
            if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case EXIT: {
                        p.closeInventory();
                        break;
                    }
                    case RETURN: {
                        getSectionEditor().open(p, 1);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            else if (clazz.equals(EditorType.class)) {
                EditorType type2 = (EditorType) type;
                if (type2 == EditorType.OBJECT_ACTIONS_PARAMETIZED_ADD) {
                    EditorManager.tipCustom(p, plugin.lang().Core_Editor_Actions_Subject_Add.getMsg());
                    EditorManager.startEdit(p, this, type2);
                    p.closeInventory();

                    List<Parametized> pzs = new ArrayList<>(
                            NexPlugin.getEngine().getActionsManager().getParametized(this.getSectionType()));
                    StringBuilder builder = new StringBuilder();
                    pzs.forEach(pz -> {
                        if (builder.length() > 0)
                            builder.append("&7 | ");
                        builder.append("%" + pz.getKey() + "%");
                    });

                    ClickText text = new ClickText(builder.toString());
                    pzs.forEach(pz -> {
                        ClickWord word = text.createPlaceholder("%" + pz.getKey() + "%", "&a" + pz.getKey());
                        word.hint(plugin.lang().Core_Editor_Actions_Subject_Hint
                                .replace("%description%", pz.getDescription()).asList());
                        word.execCmd(pz.getKey());
                    });

                    text.send(p);
                }
            }
        };

        for (String sId : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + sId, ContentType.class);
            if (guiItem == null)
                continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }

        for (String sId : cfg.getSection("editor")) {
            GuiItem guiItem = cfg.getGuiItem("editor." + sId, EditorType.class);
            if (guiItem == null)
                continue;

            Enum<?> type = guiItem.getType();
            if (type != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @Override
    public void shutdown() {
        if (this.paramEditors != null) {
            this.paramEditors.values().forEach(editor -> editor.shutdown());
            this.paramEditors.clear();
            this.paramEditors = null;
        }
        super.shutdown();
    }

    @NotNull
    public ActionCategory getSectionType() {
        return sectionType;
    }

    @NotNull
    public IEditorActionsSection<P> getSectionEditor() {
        return sectionEditor;
    }

    @Nullable
    public IEditorActionsParams<P> getParamEditor(@NotNull Parametized pz) {
        return this.paramEditors.get(pz.getKey().toLowerCase());
    }

    @Override
    protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {
        ActionBuilder builder = sectionEditor.getEditorMain().getActionBuilder(sectionEditor.getSectionId());
        if (builder == null) {
            plugin.warn("Invalid ActionBuilder for '" + sectionEditor.getSectionId() + "' section!");
            return;
        }

        int count = 0;
        for (Entry<Integer, Map<Parametized, Map<String, String>>> eTarget : builder.getParametized(sectionType)
                .entrySet()) {
            int pId = eTarget.getKey();
            Map<Parametized, Map<String, String>> mapTarget = eTarget.getValue();

            for (Map.Entry<Parametized, Map<String, String>> en : mapTarget.entrySet()) {
                Parametized pz = en.getKey();
                Map<String, String> params = en.getValue();

                ItemStack itemStack = new ItemStack(Material.CHEST);
                itemStack.setAmount(count + 1);

                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null)
                    continue;

                List<String> lore = new ArrayList<>();
                for (String line : objLore) {
                    if (line.contains("%param-name%")) {
                        if (pz.getParams().isEmpty()) {
                            lore.add(plugin.lang().Core_Editor_Actions_Subject_NoParams.getMsg());
                            continue;
                        }
                        params.forEach((key, value) -> {
                            lore.add(line.replace("%param-name%", key).replace("%param-value%", value));
                        });
                        continue;
                    }
                    if (line.equalsIgnoreCase("%description%")) {
                        lore.addAll(pz.getDescription());
                        continue;
                    }
                    lore.add(line);
                }

                meta.setDisplayName(objName.replace("%subject%", pz.getKey()));
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
                itemStack.setItemMeta(meta);

                JIcon icon = new JIcon(itemStack);
                icon.setClick((p2, type, e) -> {
                    // Delete subject on Shift-Right Click.
                    if (e.isRightClick()) {
                        if (e.isShiftClick()) {
                            builder.removeParametized(pz, this.getSectionType(), pId);
                            this.open(p2, 1);
                            this.getSectionEditor().getEditorMain().save(); // Save config
                        }
                        return;
                    }

                    // Do not open if no params can be added.
                    if (pz.getParams().isEmpty())
                        return;

                    // Create and open Params Editor GUI.
                    this.paramEditors
                            .computeIfAbsent(pz.getKey().toLowerCase(),
                                    editor -> new IEditorActionsParams<>(plugin, sectionEditor, sectionType, pz, pId))
                            .open(p2, 1);
                });

                this.addButton(p, icon, objSlots[count++]);
            }
        }
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }
}
