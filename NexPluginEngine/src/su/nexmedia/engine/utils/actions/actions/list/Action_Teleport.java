package su.nexmedia.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.IActionType;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public class Action_Teleport extends IActionExecutor {

    public Action_Teleport(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.TELEPORT);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Teleport_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.LOCATION);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        String locRaw = result.getParamValue(IParamType.LOCATION).getString(null);
        if (locRaw == null)
            return;

        Location locExe = exe.getLocation();

        locRaw = locRaw.replace("%executor.world%", exe.getWorld().getName())
                .replace("%executor.x%", String.valueOf(locExe.getX()))
                .replace("%executor.y%", String.valueOf(locExe.getY()))
                .replace("%executor.z%", String.valueOf(locExe.getZ()));

        for (Entity e : targets) {
            Location locE = e.getLocation();
            locRaw = locRaw.replace("%target.world%", exe.getWorld().getName())
                    .replace("%target.x%", String.valueOf(locE.getX()))
                    .replace("%target.y%", String.valueOf(locE.getY()))
                    .replace("%target.z%", String.valueOf(locE.getZ()));

            String[] split = locRaw.replace(" ", "").split(",");
            World world = plugin.getServer().getWorld(split[0]);
            if (world == null)
                continue;

            double x = split.length >= 2 ? StringUT.getDouble(split[1], 0, true) : 0;
            double y = split.length >= 3 ? StringUT.getDouble(split[2], 0, true) : 0;
            double z = split.length >= 4 ? StringUT.getDouble(split[3], 0, true) : 0;
            Location loc = new Location(world, x, y, z);

            e.teleport(loc);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
