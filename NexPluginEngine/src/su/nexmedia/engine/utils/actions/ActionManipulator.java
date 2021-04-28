package su.nexmedia.engine.utils.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Sets;

import me.clip.placeholderapi.PlaceholderAPI;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.conditions.IConditionValidator;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;
import su.nexmedia.engine.utils.actions.targets.ITargetSelector;
import su.nexmedia.engine.utils.constants.JStrings;

public class ActionManipulator {

    private NexPlugin<?> plugin;
    private Map<String, ActionSection> actions;

    public ActionManipulator(@NotNull NexPlugin<?> plugin, @NotNull ActionManipulator copy) {
        this.plugin = plugin;
        this.actions = new LinkedHashMap<>();
        for (Map.Entry<String, ActionSection> en : copy.getActions().entrySet()) {
            this.actions.put(en.getKey(), new ActionSection(en.getValue()));
        }
    }

    public ActionManipulator(@NotNull NexPlugin<?> plugin, @NotNull JYML cfg, @NotNull String path) {
        this.plugin = plugin;
        this.actions = new LinkedHashMap<>();

        for (String id : cfg.getSection(path)) {
            String path2 = path + "." + id + ".";

            // Update Target Selectors format to the new one.
            if (!cfg.getSection(path2 + "target-selectors").isEmpty()) {
                List<String> selectorsUpdated = new ArrayList<>();
                for (String selectorId : cfg.getSection(path2 + "target-selectors")) {
                    List<String> selectors = cfg.getStringList(path2 + "target-selectors." + selectorId);
                    selectors.replaceAll(str -> str + " ~name: " + selectorId + ";");
                    selectorsUpdated.addAll(selectors);
                }
                cfg.set(path2 + "target-selectors", null);
                cfg.set(path2 + "target-selectors", selectorsUpdated);
                cfg.saveChanges();
            }

            List<String> targetSelectors = cfg.getStringList(path2 + "target-selectors");
            List<String> conditionList = cfg.getStringList(path2 + "conditions.list");
            String conditionActionOnFail = cfg.getString(path2 + "conditions.actions-on-fail", "");
            List<String> actionExecutors = cfg.getStringList(path2 + "action-executors");

            ActionSection engine = new ActionSection(targetSelectors, conditionList, conditionActionOnFail,
                    actionExecutors);
            this.actions.put(id.toLowerCase(), engine);
        }
    }

    @NotNull
    public ActionManipulator replace(@NotNull UnaryOperator<String> func) {
        ActionManipulator manipulatorCopy = new ActionManipulator(plugin, this);

        for (ActionSection copyEngine : manipulatorCopy.getActions().values()) {
            copyEngine.getActionExecutors().replaceAll(func);
            copyEngine.getConditions().replaceAll(func);
            copyEngine.getTargetSelectors().replaceAll(func);
        }

        return manipulatorCopy;
    }

    @NotNull
    public Map<String, ActionSection> getActions() {
        return this.actions;
    }

    public void process(@NotNull Entity exec) {
        this.process(exec, Collections.emptyMap());
    }

    public void process(@NotNull Entity exec, @NotNull Map<String, Set<Entity>> targetMap2) {
        if (this.actions.isEmpty())
            return;
        String id = new ArrayList<>(this.actions.keySet()).get(0);
        this.process(exec, id, targetMap2);
    }

    public void process(@NotNull Entity exec, @NotNull String id) {
        this.process(exec, id, Collections.emptyMap());
    }

    public void process(@NotNull Entity exec, @NotNull String id, @NotNull Map<String, Set<Entity>> targetMap2) {
        ActionSection ae = this.actions.get(id.toLowerCase());
        if (ae == null)
            return;

        Map<String, Set<Entity>> targetMap = new HashMap<>();
        targetMap2.forEach((fromKey, fromVal) -> {
            targetMap.merge(fromKey, fromVal, (old, now) -> {
                Set<Entity> set = new HashSet<>(old);
                set.addAll(now);
                return set;
            });
        });

        Player p = null;
        if (Hooks.hasPlugin(Hooks.PLACEHOLDER_API) && exec instanceof Player) {
            p = (Player) exec;
        }

        // Precache target selectors for actions
        for (String selector : ae.getTargetSelectors()) {
            if (p != null)
                selector = PlaceholderAPI.setPlaceholders(p, selector);

            String selectorKey = selector.split(" ")[0].replace("[", "").replace("]", "");
            ITargetSelector targetSelector = plugin.getActionsManager().getTargetSelector(selectorKey);
            if (targetSelector == null) {
                plugin.error("Invalid target selector '" + selectorKey + "' in '" + selector + "' !");
                continue;
            }
            IParamResult result = targetSelector.getParamResult(selector);
            String targetId = result.getParamValue(IParamType.NAME).getString(JStrings.DEFAULT);
            Set<Entity> targets = new HashSet<>();

            targetSelector.select(exec, targets, selector);

            // We use merge instead of single set to prevent targets from
            // different selectors being removed due to different params.
            targetMap.merge(targetId, targets, (old, now) -> {
                Set<Entity> set = new HashSet<>(old);
                set.addAll(now);
                return set;
            });
        }

        // Check conditions
        for (String condition : ae.getConditions()) {
            if (p != null) {
                condition = PlaceholderAPI.setPlaceholders(p, condition);
            }

            String key = condition.split(" ")[0].replace("[", "").replace("]", "");
            IConditionValidator validator = plugin.getActionsManager().getConditionValidator(key);
            if (validator == null) {
                plugin.error("Invalid condition validator '" + key + "' in '" + condition + "' !");
                continue;
            }

            if (!validator.process(exec, targetMap, condition, this)) {
                this.process(exec, ae.getConditionFailActions());
                return;
            }
        }

        // Run actions
        for (String action : ae.getActionExecutors()) {
            if (p != null) {
                action = PlaceholderAPI.setPlaceholders(p, action);
            }

            String key = action.split(" ")[0].replace("[", "").replace("]", "");
            IActionExecutor executor = plugin.getActionsManager().getActionExecutor(key);
            if (executor == null) {
                plugin.error("Invalid action executor '" + key + "' in '" + action + "' !");
                continue;
            }

            executor.process(exec, targetMap, action, this);
        }
    }

    public static boolean processConditions(@NotNull NexPlugin<?> plugin, @NotNull Entity exec,
            @NotNull List<String> condis) {
        return processConditions(plugin, exec, condis, Collections.emptyMap());
    }

    public static boolean processConditions(@NotNull NexPlugin<?> plugin, @NotNull Entity exec,
            @NotNull List<String> condis, @NotNull Map<String, Set<Entity>> targetMap2) {

        Map<String, Set<Entity>> targetMap = new HashMap<>();
        targetMap.put(JStrings.DEFAULT, Sets.newHashSet(exec));
        targetMap2.forEach((fromKey, fromVal) -> {
            targetMap.merge(fromKey, fromVal, (old, now) -> {
                Set<Entity> set = new HashSet<>(old);
                set.addAll(now);
                return set;
            });
        });

        Player p = null;
        if (Hooks.hasPlugin(Hooks.PLACEHOLDER_API) && exec instanceof Player) {
            p = (Player) exec;
        }

        // Check conditions
        for (String condition : condis) {
            if (p != null) {
                condition = PlaceholderAPI.setPlaceholders(p, condition);
            }

            String key = condition.split(" ")[0].replace("[", "").replace("]", "");
            IConditionValidator validator = plugin.getActionsManager().getConditionValidator(key);
            if (validator == null) {
                plugin.error("Invalid condition validator '" + key + "' in '" + condition + "' !");
                continue;
            }

            if (!validator.process(exec, targetMap, condition)) {
                return false;
            }
        }
        return true;
    }
}
