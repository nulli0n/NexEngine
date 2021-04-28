package su.nexmedia.engine.utils.actions.params.list;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.actions.params.IAutoValidated;
import su.nexmedia.engine.utils.actions.params.IParamType;
import su.nexmedia.engine.utils.actions.params.IParamValue;
import su.nexmedia.engine.utils.actions.params.defaults.IParamBoolean;

public class AttackableParam extends IParamBoolean implements IAutoValidated {

    public AttackableParam() {
        super(IParamType.ATTACKABLE, "attackable");
    }

    @Override
    public void autoValidate(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamValue val) {
        boolean b = val.getBoolean();
        targets.removeIf(target -> {
            boolean attackable = Hooks.canFights(exe, target);
            return attackable != b;
        });
    }
}
