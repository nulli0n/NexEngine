package su.nexmedia.engine.utils.actions.targets.list;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.targets.ITargetSelector;
import su.nexmedia.engine.utils.actions.targets.ITargetType;

public class Target_Self extends ITargetSelector {

    public Target_Self(@NotNull NexPlugin<?> plugin) {
        super(plugin, ITargetType.SELF);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_TargetSelector_Self_Desc.asList();
    }

    @Override
    public void registerParams() {

    }

    @Override
    protected void validateTarget(Entity exe, Set<Entity> targets, IParamResult result) {
        Set<Entity> disTargets = new HashSet<>();
        disTargets.add(exe);

        targets.addAll(disTargets); // Add all targets from this selector
    }
}
