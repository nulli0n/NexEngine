package su.nexmedia.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.IActionType;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public class Action_Burn extends IActionExecutor {

    public Action_Burn(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.BURN);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Burn_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.DURATION);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        int dura = 0;
        if (result.hasParam(IParamType.DURATION)) {
            dura = result.getParamValue(IParamType.DURATION).getInt(0);
        }
        if (dura <= 0)
            return;

        for (Entity e : targets) {
            e.setFireTicks(e.getFireTicks() + dura);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
