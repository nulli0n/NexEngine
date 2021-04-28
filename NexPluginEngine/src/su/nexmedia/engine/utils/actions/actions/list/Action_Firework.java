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
import su.nexmedia.engine.utils.random.Rnd;

public class Action_Firework extends IActionExecutor {

    public Action_Firework(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.FIREWORK);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Firework_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!targets.isEmpty()) {
            for (Entity target : targets) {
                Rnd.spawnRandomFirework(target.getLocation());
            }
        }
        else {
            Rnd.spawnRandomFirework(exe.getLocation());
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

}
