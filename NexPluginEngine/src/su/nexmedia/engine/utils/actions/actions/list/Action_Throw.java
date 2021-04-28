package su.nexmedia.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.IActionType;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public class Action_Throw extends IActionExecutor {

    public Action_Throw(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.THROW);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Throw_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        for (Entity target : targets) {
            Location localLocation1 = target.getLocation();
            Location localLocation2 = localLocation1.subtract(exe.getLocation());

            Vector localVector = localLocation2.getDirection().normalize().multiply(-1.4D);

            if (localVector.getY() >= 1.15D) {
                localVector.setY(localVector.getY() * 0.45D);
            }
            else if (localVector.getY() >= 1.0D) {
                localVector.setY(localVector.getY() * 0.6D);
            }
            else if (localVector.getY() >= 0.8D) {
                localVector.setY(localVector.getY() * 0.85D);
            }

            if (localVector.getY() <= 0.0D) {
                localVector.setY(-localVector.getY() + 0.3D);
            }
            if (Math.abs(localLocation2.getX()) <= 1.0D) {
                localVector.setX(localVector.getX() * 1.2D);
            }
            if (Math.abs(localLocation2.getZ()) <= 1.0D) {
                localVector.setZ(localVector.getZ() * 1.2D);
            }

            double d1 = localVector.getX() * 2;
            double d2 = localVector.getY() * 2;
            double d3 = localVector.getZ() * 2;

            if (d1 >= 3.0D)
                d1 *= 0.5D;
            if (d2 >= 3.0D)
                d2 *= 0.5D;
            if (d3 >= 3.0D)
                d3 *= 0.5D;

            localVector.setX(d1);
            localVector.setY(d2);
            localVector.setZ(d3);

            target.setVelocity(localVector);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
