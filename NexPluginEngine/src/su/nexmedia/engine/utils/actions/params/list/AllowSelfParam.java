package su.nexmedia.engine.utils.actions.params.list;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.actions.params.IAutoValidated;
import su.nexmedia.engine.utils.actions.params.IParamType;
import su.nexmedia.engine.utils.actions.params.IParamValue;
import su.nexmedia.engine.utils.actions.params.defaults.IParamBoolean;

public class AllowSelfParam extends IParamBoolean implements IAutoValidated {

    public AllowSelfParam() {
        super(IParamType.ALLOW_SELF, "allow-self");
    }

    @Override
    public void autoValidate(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamValue val) {
        boolean b = val.getBoolean();
        if (!b) {
            targets.remove(exe);
        }
        else {
            targets.add(exe);
        }
    }
}
