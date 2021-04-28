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

public class Action_Hunger extends IActionExecutor {

    public Action_Hunger(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.HUNGER);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Hunger_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.AMOUNT);
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
            double max = 20;
            if (percent) {
                amount2 = max * (amount / 100D);
            }

            livingEntity.setFoodLevel((int) Math.min(livingEntity.getFoodLevel() + amount2, max));
        });
    }
}
