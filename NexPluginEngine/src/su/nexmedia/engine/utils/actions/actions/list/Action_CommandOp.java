package su.nexmedia.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.IActionType;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public class Action_CommandOp extends IActionExecutor {

    public Action_CommandOp(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.COMMAND_OP);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_CommandOp_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.MESSAGE);
        this.registerParam(IParamType.TARGET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!result.hasParam(IParamType.MESSAGE))
            return;
        if (exe.getType() != EntityType.PLAYER)
            return;

        String text = result.getParamValue(IParamType.MESSAGE).getString(null);
        if (text == null)
            return;

        text = text.replace("%executor%", exe.getName());

        Player executor = (Player) exe;
        boolean isOp = executor.isOp();
        if (!isOp) {
            executor.setOp(true);
        }

        if (!targets.isEmpty()) {
            for (Entity e : targets) {
                String text2 = text.replace("%target%", e.getName());
                executor.performCommand(text2);
            }
        }
        else {
            executor.performCommand(text);
        }

        if (!isOp) {
            executor.setOp(false);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }
}
