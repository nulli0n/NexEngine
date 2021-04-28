package su.nexmedia.engine.utils.actions.params;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface IAutoValidated {

    public void autoValidate(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamValue val);
}
