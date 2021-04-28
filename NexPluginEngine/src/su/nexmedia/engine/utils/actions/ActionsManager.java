package su.nexmedia.engine.utils.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.manager.IManager;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.list.Action_ActionBar;
import su.nexmedia.engine.utils.actions.actions.list.Action_Broadcast;
import su.nexmedia.engine.utils.actions.actions.list.Action_Burn;
import su.nexmedia.engine.utils.actions.actions.list.Action_CommandConsole;
import su.nexmedia.engine.utils.actions.actions.list.Action_CommandOp;
import su.nexmedia.engine.utils.actions.actions.list.Action_CommandPlayer;
import su.nexmedia.engine.utils.actions.actions.list.Action_Damage;
import su.nexmedia.engine.utils.actions.actions.list.Action_Firework;
import su.nexmedia.engine.utils.actions.actions.list.Action_Goto;
import su.nexmedia.engine.utils.actions.actions.list.Action_Health;
import su.nexmedia.engine.utils.actions.actions.list.Action_Hook;
import su.nexmedia.engine.utils.actions.actions.list.Action_Hunger;
import su.nexmedia.engine.utils.actions.actions.list.Action_Lightning;
import su.nexmedia.engine.utils.actions.actions.list.Action_Message;
import su.nexmedia.engine.utils.actions.actions.list.Action_ParticleSimple;
import su.nexmedia.engine.utils.actions.actions.list.Action_Potion;
import su.nexmedia.engine.utils.actions.actions.list.Action_ProgressBar;
import su.nexmedia.engine.utils.actions.actions.list.Action_Projectile;
import su.nexmedia.engine.utils.actions.actions.list.Action_Saturation;
import su.nexmedia.engine.utils.actions.actions.list.Action_Sound;
import su.nexmedia.engine.utils.actions.actions.list.Action_Teleport;
import su.nexmedia.engine.utils.actions.actions.list.Action_Throw;
import su.nexmedia.engine.utils.actions.actions.list.Action_Titles;
import su.nexmedia.engine.utils.actions.conditions.IConditionValidator;
import su.nexmedia.engine.utils.actions.conditions.list.Condition_EntityHealth;
import su.nexmedia.engine.utils.actions.conditions.list.Condition_EntityType;
import su.nexmedia.engine.utils.actions.conditions.list.Condition_Permission;
import su.nexmedia.engine.utils.actions.conditions.list.Condition_VaultBalance;
import su.nexmedia.engine.utils.actions.conditions.list.Condition_WorldTime;
import su.nexmedia.engine.utils.actions.params.IParam;
import su.nexmedia.engine.utils.actions.params.IParamType;
import su.nexmedia.engine.utils.actions.params.defaults.IParamBoolean;
import su.nexmedia.engine.utils.actions.params.defaults.IParamNumber;
import su.nexmedia.engine.utils.actions.params.defaults.IParamString;
import su.nexmedia.engine.utils.actions.params.list.AllowSelfParam;
import su.nexmedia.engine.utils.actions.params.list.AttackableParam;
import su.nexmedia.engine.utils.actions.params.list.LocationParam;
import su.nexmedia.engine.utils.actions.params.list.OffsetParam;
import su.nexmedia.engine.utils.actions.targets.ITargetSelector;
import su.nexmedia.engine.utils.actions.targets.list.Target_FromSight;
import su.nexmedia.engine.utils.actions.targets.list.Target_Radius;
import su.nexmedia.engine.utils.actions.targets.list.Target_Self;

public class ActionsManager extends IManager<NexEngine> {

    private Map<String, IActionExecutor> actionExecutors;
    private Map<String, IConditionValidator> conditionValidators;
    private Map<String, ITargetSelector> targetSelectors;
    private Map<String, IParam> params;
    private Map<String, ActionManipulator> manipulators;

    public ActionsManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    public void setup() {
        this.actionExecutors = new HashMap<>();
        this.conditionValidators = new HashMap<>();
        this.params = new HashMap<>();
        this.targetSelectors = new HashMap<>();
        this.manipulators = new HashMap<>();

        this.setupDefaults();
    }

    @Override
    public void shutdown() {
        if (this.manipulators != null) {
            this.manipulators.clear();
            this.manipulators = null;
        }
        if (this.actionExecutors != null) {
            this.actionExecutors.clear();
            this.actionExecutors = null;
        }
        if (this.conditionValidators != null) {
            this.conditionValidators.clear();
            this.conditionValidators = null;
        }
        if (this.targetSelectors != null) {
            this.targetSelectors.clear();
            this.targetSelectors = null;
        }
        if (this.params != null) {
            this.params.clear();
            this.params = null;
        }
    }

    @NotNull
    public Collection<IActionExecutor> getExecutors() {
        return this.actionExecutors.values();
    }

    @NotNull
    public Collection<IConditionValidator> getConditionValidators() {
        return this.conditionValidators.values();
    }

    @NotNull
    public Collection<IParam> getParams() {
        return this.params.values();
    }

    @NotNull
    public Collection<ITargetSelector> getTargetSelectors() {
        return this.targetSelectors.values();
    }

    @NotNull
    public Collection<IActionExecutor> getActionExecutors() {
        return this.actionExecutors.values();
    }

    public void registerExecutor(@NotNull IActionExecutor executor) {
        if (this.actionExecutors.put(executor.getKey(), executor) != null) {
            plugin.info(
                    "[Actions Engine] Replaced registered action executor '" + executor.getKey() + "' with a new one.");
        }
    }

    public void registerCondition(@NotNull IConditionValidator conditionValidator) {
        if (this.conditionValidators.put(conditionValidator.getKey(), conditionValidator) != null) {
            plugin.info("[Actions Engine] Replaced registered condition validator '" + conditionValidator.getKey()
                    + "' with a new one.");
        }
    }

    public void registerParam(@NotNull IParam param) {
        if (this.params.put(param.getKey(), param) != null) {
            plugin.info("[Actions Engine] Replaced registered param '" + param.getKey() + "' with a new one.");
        }
    }

    public void registerTargetSelector(@NotNull ITargetSelector selector) {
        if (this.targetSelectors.put(selector.getKey(), selector) != null) {
            plugin.info(
                    "[Actions Engine] Replaced registered target selector '" + selector.getKey() + "' with a new one.");
        }
    }

    public void registerManipulator(@NotNull String id, @NotNull ActionManipulator manipulator) {
        if (this.manipulators.put(id.toLowerCase(), manipulator) != null) {
            plugin.info("[Actions Engine] Replaced registered Action Manipulator '" + id + "' with a new one.");
        }
    }

    @Nullable
    public ActionManipulator getManipulator(@NotNull String id) {
        return this.manipulators.getOrDefault(id.toLowerCase(), null);
    }

    public void unregisterManipulator(@NotNull String id) {
        this.manipulators.remove(id.toLowerCase());
    }

    @Nullable
    public Parametized getParametized(@NotNull ActionCategory category, @NotNull String key) {
        if (category == ActionCategory.TARGETS)
            return this.getTargetSelector(key);
        if (category == ActionCategory.CONDITIONS)
            return this.getConditionValidator(key);
        if (category == ActionCategory.ACTIONS)
            return this.getActionExecutor(key);
        return null;
    }

    @Nullable
    public Collection<? extends Parametized> getParametized(@NotNull ActionCategory category) {
        if (category == ActionCategory.TARGETS)
            return this.getTargetSelectors();
        if (category == ActionCategory.CONDITIONS)
            return this.getConditionValidators();
        if (category == ActionCategory.ACTIONS)
            return this.getActionExecutors();
        return Collections.emptySet();
    }

    @Nullable
    public IActionExecutor getActionExecutor(@NotNull String key) {
        return this.actionExecutors.getOrDefault(key.toUpperCase(), null);
    }

    @Nullable
    public IConditionValidator getConditionValidator(@NotNull String key) {
        return this.conditionValidators.getOrDefault(key.toUpperCase(), null);
    }

    @Nullable
    public ITargetSelector getTargetSelector(@NotNull String key) {
        return this.targetSelectors.getOrDefault(key.toUpperCase(), null);
    }

    @Nullable
    public IParam getParam(@NotNull String key) {
        return this.params.getOrDefault(key.toUpperCase(), null);
    }

    private final void setupDefaults() {
        this.registerParam(new AllowSelfParam());
        this.registerParam(new AttackableParam());
        this.registerParam(new IParamNumber(IParamType.AMOUNT, "amount"));
        this.registerParam(new IParamNumber(IParamType.DELAY, "delay"));
        this.registerParam(new IParamNumber(IParamType.DISTANCE, "distance"));
        this.registerParam(new IParamNumber(IParamType.DURATION, "duration"));
        this.registerParam(new IParamBoolean(IParamType.FILTER, "filter"));
        this.registerParam(new IParamString(IParamType.MESSAGE, "message"));
        this.registerParam(new IParamString(IParamType.NAME, "name"));
        this.registerParam(new IParamNumber(IParamType.SPEED, "speed"));
        this.registerParam(new IParamString(IParamType.TARGET, "target"));
        this.registerParam(new IParamString(IParamType.TITLES_TITLE, "title"));
        this.registerParam(new IParamString(IParamType.TITLES_SUBTITLE, "subtitle"));
        this.registerParam(new IParamNumber(IParamType.TITLES_FADE_IN, "fadeIn"));
        this.registerParam(new IParamNumber(IParamType.TITLES_STAY, "stay"));
        this.registerParam(new IParamNumber(IParamType.TITLES_FADE_OUT, "fadeOut"));
        this.registerParam(new IParamString(IParamType.BAR_COLOR_EMPTY, "color-empty"));
        this.registerParam(new IParamString(IParamType.BAR_COLOR_FILL, "color-fill"));
        this.registerParam(new LocationParam());
        this.registerParam(new OffsetParam());

        // CONDITIONS //
        this.registerCondition(new Condition_WorldTime(this.plugin));
        this.registerCondition(new Condition_Permission(this.plugin));
        this.registerCondition(new Condition_VaultBalance(this.plugin));
        this.registerCondition(new Condition_EntityHealth(this.plugin));
        this.registerCondition(new Condition_EntityType(this.plugin));

        // EXECUTORS //
        this.registerExecutor(new Action_ActionBar(this.plugin));
        this.registerExecutor(new Action_Broadcast(this.plugin));
        this.registerExecutor(new Action_Burn(this.plugin));
        this.registerExecutor(new Action_Damage(this.plugin));
        this.registerExecutor(new Action_Health(this.plugin));
        this.registerExecutor(new Action_Message(this.plugin));
        this.registerExecutor(new Action_CommandPlayer(this.plugin));
        this.registerExecutor(new Action_CommandConsole(this.plugin));
        this.registerExecutor(new Action_CommandOp(this.plugin));
        this.registerExecutor(new Action_Firework(this.plugin));
        this.registerExecutor(new Action_Hook(this.plugin));
        this.registerExecutor(new Action_Lightning(this.plugin));
        this.registerExecutor(new Action_ParticleSimple(this.plugin));
        this.registerExecutor(new Action_Potion(this.plugin));
        this.registerExecutor(new Action_ProgressBar(this.plugin));
        this.registerExecutor(new Action_Projectile(this.plugin));
        this.registerExecutor(new Action_Throw(this.plugin));
        this.registerExecutor(new Action_Saturation(this.plugin));
        this.registerExecutor(new Action_Hunger(this.plugin));
        this.registerExecutor(new Action_Sound(this.plugin));
        this.registerExecutor(new Action_Teleport(this.plugin));
        this.registerExecutor(new Action_Titles(this.plugin));
        this.registerExecutor(new Action_Goto(this.plugin));

        // TARGETS //
        this.registerTargetSelector(new Target_FromSight(this.plugin));
        this.registerTargetSelector(new Target_Self(this.plugin));
        this.registerTargetSelector(new Target_Radius(this.plugin));
    }
}
