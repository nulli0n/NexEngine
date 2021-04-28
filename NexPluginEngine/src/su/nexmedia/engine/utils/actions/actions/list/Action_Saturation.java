package su.nexmedia.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.IActionType;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;
import su.nexmedia.engine.utils.actions.params.IParamValue;

public class Action_Saturation extends IActionExecutor {

    public Action_Saturation(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.SATURATION);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Saturation_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.AMOUNT);
        this.registerParam(IParamType.TARGET);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        IParamValue value = result.getParamValue(IParamType.AMOUNT);

        double amount = value.getDouble(0);
        if (amount == 0)
            return;

        boolean percent = value.getBoolean();

        targets.forEach(target -> {
            if (!(target instanceof Player))
                return;

            Player livingEntity = (Player) target;
            double amount2 = amount;
            double has = livingEntity.getSaturation();
            if (percent) {
                amount2 = has * (amount / 100D);
            }

            livingEntity.setSaturation((float) (has + amount2));
        });
    }
}
