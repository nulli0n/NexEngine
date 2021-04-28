package su.nexmedia.engine.utils.actions.targets.list;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;
import su.nexmedia.engine.utils.actions.targets.ITargetSelector;
import su.nexmedia.engine.utils.actions.targets.ITargetType;

public class Target_Radius extends ITargetSelector {

    public Target_Radius(@NotNull NexPlugin<?> plugin) {
        super(plugin, ITargetType.RADIUS);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_TargetSelector_Radius_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.ALLOW_SELF);
        this.registerParam(IParamType.ATTACKABLE);
        this.registerParam(IParamType.DISTANCE);
    }

    @Override
    protected void validateTarget(Entity exe, Set<Entity> targets, IParamResult result) {
        double dist = -1;
        if (result.hasParam(IParamType.DISTANCE)) {
            dist = result.getParamValue(IParamType.DISTANCE).getDouble(0);
        }
        else
            return;

        if (dist <= 0)
            return;

        Set<Entity> disTargets = new HashSet<>();
        disTargets.addAll(exe.getNearbyEntities(dist, dist, dist));

        targets.addAll(disTargets); // Add all targets from this selector
    }
}
