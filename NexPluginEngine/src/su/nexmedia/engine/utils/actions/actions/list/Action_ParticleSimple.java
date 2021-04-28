package su.nexmedia.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.IActionType;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public class Action_ParticleSimple extends IActionExecutor {

    public Action_ParticleSimple(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.PARTICLE_SIMPLE);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_ParticleSimple_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.NAME);
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.AMOUNT);
        this.registerParam(IParamType.SPEED);
        this.registerParam(IParamType.OFFSET);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        String name = result.getParamValue(IParamType.NAME).getString(null);
        if (name == null)
            return;

        double[] offset = result.getParamValue(IParamType.OFFSET).getDoubleArray();

        int amount = result.getParamValue(IParamType.AMOUNT).getInt(30);

        float speed = (float) result.getParamValue(IParamType.SPEED).getDouble(0.1);

        for (Entity e : targets) {
            Location loc;
            if (e instanceof LivingEntity) {
                loc = ((LivingEntity) e).getEyeLocation();
            }
            else
                loc = e.getLocation();

            EffectUT.playEffect(loc, name, (float) offset[0], (float) offset[1], (float) offset[2], speed, amount);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
