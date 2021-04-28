package su.nexmedia.engine.manager.editor.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.manager.api.IActionEditable;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.manager.editor.EditorType;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.ActionCategory;
import su.nexmedia.engine.utils.actions.ActionManipulator;
import su.nexmedia.engine.utils.actions.ActionSection;
import su.nexmedia.engine.utils.actions.Parametized;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.conditions.IConditionValidator;
import su.nexmedia.engine.utils.actions.params.IParamValue;
import su.nexmedia.engine.utils.actions.targets.ITargetSelector;

public class IEditorActionsMain<P extends NexPlugin<P>> extends NGUI<P> {

    private IActionEditable actionObject;
    private Map<String, ActionBuilder> actionBuilders;
    private Map<String, IEditorActionsSection<P>> actionEditors;

    private static int[] objSlots;
    private static String objName;
    private static List<String> objLore;

    public IEditorActionsMain(@NotNull P plugin, @NotNull IActionEditable actionObject) {
        super(plugin, EditorManager.EDITOR_ACTIONS_MAIN, "");
        this.actionObject = actionObject;
        this.actionBuilders = new HashMap<>();
        this.actionEditors = new HashMap<>();

        JYML cfg = EditorManager.EDITOR_ACTIONS_MAIN;
        objSlots = cfg.getIntArray("object-slots");
        objName = StringUT.color(cfg.getString("object-name", "%section%"));
        objLore = StringUT.color(cfg.getStringList("object-lore"));

        // Load and transfer object actions into editable format.
        ActionManipulator actionManipulator = actionObject.getActions();
        actionManipulator.getActions().forEach((id, section) -> {
            ActionBuilder builder = new ActionBuilder(section, id);
            this.actionBuilders.put(id, builder);
        });

        GuiClick click = new GuiClick() {
            @Override
            public void click(Player p, @Nullable Enum<?> type, InventoryClickEvent e) {
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
                            actionObject.getEditor().open(p, 1);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
                else if (clazz.equals(EditorType.class)) {
                    EditorType type2 = (EditorType) type;

                    if (type2 == EditorType.OBJECT_ACTIONS_SECTION_ADD) {
                        EditorManager.tipCustom(p, plugin.lang().Core_Editor_Actions_Section_Add.getMsg());
                        EditorManager.startEdit(p, IEditorActionsMain.this, type2);
                        p.closeInventory();
                    }
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

    @NotNull
    public IActionEditable getActionObject() {
        return actionObject;
    }

    @NotNull
    public Map<String, ActionBuilder> getActionBuilders() {
        return actionBuilders;
    }

    @Nullable
    public ActionBuilder getActionBuilder(@NotNull String sectionId) {
        return this.actionBuilders.get(sectionId.toLowerCase());
    }

    public void save() {
        if (!(this.actionObject instanceof LoadableItem))
            return;

        String path = this.actionObject.getActionsPath();
        if (!path.endsWith("."))
            path += ".";

        String path2 = path;
        LoadableItem load = (LoadableItem) this.actionObject;
        JYML cfg = load.getConfig();

        this.actionBuilders.forEach((actId, builder) -> {
            for (ActionCategory category : ActionCategory.values()) {
                List<String> lines = new ArrayList<>();
                String sub;
                if (category == ActionCategory.TARGETS)
                    sub = "target-selectors";
                else if (category == ActionCategory.CONDITIONS)
                    sub = "conditions.list";
                else if (category == ActionCategory.ACTIONS)
                    sub = "action-executors";
                else
                    continue;

                builder.getParametized(category).values().forEach(mapPz -> {
                    mapPz.forEach((pz, mapParam) -> {
                        String prefix = "[" + pz.getKey() + "] ";
                        StringBuilder paramBuilder = new StringBuilder();

                        mapParam.forEach((param, value) -> {
                            if (paramBuilder.length() > 0)
                                paramBuilder.append(" ");
                            paramBuilder.append("~" + param + ": " + value + ";");
                        });

                        String line = prefix + paramBuilder.toString();
                        lines.add(line);
                    });
                });

                cfg.set(path2 + actId + "." + sub, lines);
            }
        });

        cfg.saveChanges();
    }

    @Override
    public void shutdown() {
        if (this.actionEditors != null) {
            this.actionEditors.values().forEach(editor -> {
                editor.shutdown();
            });
            this.actionEditors.clear();
            this.actionEditors = null;
        }
        super.shutdown();
    }

    @Override
    protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {
        int count = 0;

        for (Map.Entry<String, ActionBuilder> en : this.actionBuilders.entrySet()) {
            String sId = en.getKey();
            ActionBuilder builder = en.getValue();

            ItemStack item = new ItemStack(Material.OBSERVER);
            ItemMeta meta = item.getItemMeta();
            if (meta == null)
                continue;

            List<String> lore = new ArrayList<>(objLore);
            lore.replaceAll(line -> line
                    .replace("%targets-amount%", String.valueOf(builder.getParametized(ActionCategory.TARGETS).size()))
                    .replace("%conditions-amount%",
                            String.valueOf(builder.getParametized(ActionCategory.CONDITIONS).size()))
                    .replace("%actions-amount%",
                            String.valueOf(builder.getParametized(ActionCategory.ACTIONS).size())));

            meta.setDisplayName(objName.replace("%section%", sId));
            meta.setLore(lore);
            item.setItemMeta(meta);

            JIcon icon = new JIcon(item);
            icon.setClick((p2, type, e) -> {
                if (e.isRightClick()) {
                    if (e.isShiftClick()) {
                        this.getActionBuilders().remove(sId);
                        this.open(p, 1);
                        this.save(); // Save config
                    }
                    return;
                }

                // Create and open Action Section Editor.
                this.actionEditors.computeIfAbsent(sId, editor -> new IEditorActionsSection<P>(plugin, this, sId))
                        .open(p2, 1);
            });

            this.addButton(p, icon, objSlots[count++]);
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

    public static class ActionBuilder {

        private String id;
        private Map<ActionCategory, Map<Integer, Map<Parametized, Map<String, String>>>> items;

        public ActionBuilder(@NotNull ActionSection section, @NotNull String id) {
            this.items = new HashMap<>();
            this.id = id;

            this.inherit(ActionCategory.TARGETS, section.getTargetSelectors());
            this.inherit(ActionCategory.ACTIONS, section.getActionExecutors());
            this.inherit(ActionCategory.CONDITIONS, section.getConditions());
        }

        @NotNull
        public String getId() {
            return id;
        }

        public void inject(@NotNull ActionSection section) {
            // Clear old first.
            section.getTargetSelectors().clear();
            section.getConditions().clear();
            section.getActionExecutors().clear();

            // Update real object actions.
            this.inject(section.getTargetSelectors(), ActionCategory.TARGETS);
            this.inject(section.getConditions(), ActionCategory.CONDITIONS);
            this.inject(section.getActionExecutors(), ActionCategory.ACTIONS);
        }

        // Update the real Actions with the custom editor from Builder.
        private void inject(@NotNull List<String> to, @NotNull ActionCategory category) {
            List<String> lines = new ArrayList<>();
            this.getParametized(category).forEach((pId, mapPz) -> {
                mapPz.forEach((selector, mapParam) -> {
                    String prefix = "[" + selector.getKey() + "] "; // Brackets are not required tho
                    StringBuilder paramBuilder = new StringBuilder();

                    mapParam.forEach((param, value) -> {
                        if (paramBuilder.length() > 0)
                            paramBuilder.append(" ");
                        paramBuilder.append("~" + param + ": " + value + ";");
                    });

                    String line = prefix + mapParam.toString();
                    lines.add(line);
                });
            });
            to.addAll(lines);
        }

        private void inherit(@NotNull ActionCategory type, @NotNull List<String> exes) {
            exes.forEach(line -> {
                String selectorKey = line.split(" ")[0].replace("[", "").replace("]", "");

                Parametized pz = NexEngine.get().getActionsManager().getParametized(type, selectorKey);
                if (pz == null)
                    return;

                Map<Integer, Map<Parametized, Map<String, String>>> global = this.getParametized(type);
                Map<Parametized, Map<String, String>> mapSelector = global.computeIfAbsent(global.size(),
                        map -> new HashMap<>());
                Map<String, String> mapParams = mapSelector.computeIfAbsent(pz, map -> new HashMap<>());

                // String paramsRaw = line.replace(selectorKey, "").trim();
                pz.getParams().forEach(param -> {
                    String flag = param.getFlag(); // Raw flag, without '~' prefix
                    if (!line.contains(flag))
                        return;

                    // Search for flag of this parameter
                    Matcher m = param.getPattern().matcher(line); // TODO add Fixed ICharSeq

                    // Get the flag value
                    if (m.find()) {
                        String ext = m.group(4).trim(); // Extract only value from all flag string
                        IParamValue v = param.getParser().parseValue(ext); // Parse value from a string
                        mapParams.put(param.getKey().toLowerCase(), v.getRaw()); // Put in result map
                    }
                });
            });
        }

        public int addTargetSelector(@NotNull ITargetSelector targetSelector) {
            return this.addParametized(targetSelector, ActionCategory.TARGETS);
        }

        public void addTargetParam(int key, @NotNull ITargetSelector ts, @NotNull String param, @NotNull String value) {
            this.addParametizedParam(key, ActionCategory.TARGETS, ts, param, value);
        }

        public int addActionExecutor(@NotNull IActionExecutor executor) {
            return this.addParametized(executor, ActionCategory.ACTIONS);
        }

        public void addActionParam(int key, @NotNull IActionExecutor exec, @NotNull String param,
                @NotNull String value) {
            this.addParametizedParam(key, ActionCategory.ACTIONS, exec, param, value);
        }

        public int addConditionValidator(@NotNull IConditionValidator validator) {
            return this.addParametized(validator, ActionCategory.CONDITIONS);
        }

        public void addConditionParam(int key, @NotNull IConditionValidator cd, @NotNull String param,
                @NotNull String value) {
            this.addParametizedParam(key, ActionCategory.CONDITIONS, cd, param, value);
        }

        public Map<Integer, Map<Parametized, Map<String, String>>> getTargets() {
            return this.getParametized(ActionCategory.TARGETS);
        }

        public int addParametized(@NotNull Parametized parametized, @NotNull ActionCategory type) {
            int key = this.items.getOrDefault(type, Collections.emptyMap()).size();

            Map<Parametized, Map<String, String>> mapG = this.items.computeIfAbsent(type, map0 -> new HashMap<>())
                    .computeIfAbsent(key, map -> new HashMap<>());

            Map<String, String> params = mapG.computeIfAbsent(parametized, map -> new HashMap<>());
            mapG.put(parametized, params);

            return key;
        }

        public void removeParametized(@NotNull Parametized parametized, @NotNull ActionCategory type, int pId) {
            Map<Parametized, Map<String, String>> mapG = this.items.computeIfAbsent(type, map0 -> new HashMap<>())
                    .computeIfAbsent(pId, map -> new HashMap<>());

            mapG.remove(parametized);
        }

        public void addParametizedParam(int key, @NotNull ActionCategory type, @NotNull Parametized parametized,
                @NotNull String param, @NotNull String value) {

            Map<String, String> params = this.items.computeIfAbsent(type, map -> new HashMap<>())
                    .computeIfAbsent(key, map -> new HashMap<>()).computeIfAbsent(parametized, map -> new HashMap<>());

            params.put(param.toLowerCase(), value);
        }

        @NotNull
        public Map<Integer, Map<Parametized, Map<String, String>>> getParametized(@NotNull ActionCategory type) {
            return this.items.computeIfAbsent(type, map -> new HashMap<>());
        }

        public void removeParametizedParam(int key, @NotNull ActionCategory type, @NotNull Parametized parametized,
                @NotNull String param) {

            Map<String, String> params = this.items.computeIfAbsent(type, map -> new HashMap<>())
                    .computeIfAbsent(key, map -> new HashMap<>()).computeIfAbsent(parametized, map -> new HashMap<>());

            params.remove(param.toLowerCase());
        }
    }
}
