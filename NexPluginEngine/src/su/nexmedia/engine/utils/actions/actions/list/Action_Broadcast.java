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

public class Action_Broadcast extends IActionExecutor {

    public Action_Broadcast(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.BROADCAST);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Broadcast_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.MESSAGE);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        if (!result.hasParam(IParamType.MESSAGE))
            return;

        String text = result.getParamValue(IParamType.MESSAGE).getString(null);
        if (text == null)
            return;

        text = text.replace("%executor%", exe.getName());

        plugin.getServer().broadcastMessage(text);
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

}
