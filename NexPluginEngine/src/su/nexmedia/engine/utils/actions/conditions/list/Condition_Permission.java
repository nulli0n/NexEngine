package su.nexmedia.engine.utils.actions.conditions.list;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.conditions.IConditionType;
import su.nexmedia.engine.utils.actions.conditions.IConditionValidator;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public class Condition_Permission extends IConditionValidator {

    public Condition_Permission(@NotNull NexPlugin<?> plugin) {
        super(plugin, IConditionType.PERMISSION);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Condition_Permission_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.NAME);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    @Nullable
    protected Predicate<Entity> validate(@NotNull Entity exe, @NotNull Set<Entity> targets,
            @NotNull IParamResult result) {

        String node = result.getParamValue(IParamType.NAME).getString(null);
        if (node == null)
            return null;

        boolean negative = node.startsWith("-");
        return target -> target.hasPermission(node) == !negative;
    }
}