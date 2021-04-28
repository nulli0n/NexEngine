package su.nexmedia.engine.utils.actions.actions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.ActionManipulator;
import su.nexmedia.engine.utils.actions.Parametized;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public abstract class IActionExecutor extends Parametized {

    public IActionExecutor(@NotNull NexPlugin<?> plugin, @NotNull String key) {
        super(plugin, key);
        this.registerParam(IParamType.DELAY);
    }

    public abstract boolean mustHaveTarget();

    protected abstract void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result);

    public final void process(@NotNull Entity exe, @NotNull Map<String, Set<Entity>> targetMap, @NotNull String fullStr,
            @NotNull ActionManipulator manipulator) {

        IParamResult result = this.getParamResult(fullStr);

        if (fullStr.contains(FLAG_NO_DELAY)) {
            fullStr = fullStr.replace(FLAG_NO_DELAY, "");
        }
        else if (result.hasParam(IParamType.DELAY)) {
            int delay = result.getParamValue(IParamType.DELAY).getInt(0);
            final String fullStr2 = fullStr;

            if (delay > 0) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    this.process(exe, targetMap, fullStr2 + FLAG_NO_DELAY, manipulator);
                }, delay);
                return;
            }
        }

        if (this.mustHaveTarget() && !result.hasParam(IParamType.TARGET)) {
            plugin.warn("No Target specified for action: " + fullStr);
            return;
        }

        Set<Entity> targets = new HashSet<>();
        String[] tsSplit = result.getParamValue(IParamType.TARGET).getString("").split(",");
        for (String ts : tsSplit) {
            if (ts.isEmpty())
                continue;
            ts = ts.toLowerCase();
            if (targetMap.containsKey(ts)) {
                targets.addAll(targetMap.get(ts));
            }
            else {
                plugin.warn("Invalid Target specified for action: " + fullStr);
            }
        }

        if (this.mustHaveTarget() && targets.isEmpty()) {
            return;
        }

        if (this.getKey().equalsIgnoreCase(IActionType.GOTO)) {
            String id = result.getParamValue(IParamType.NAME).getString(null);
            if (id == null)
                return;

            manipulator.process(exe, id);
        }
        else {
            this.execute(exe, targets, result);
        }
    }
}
