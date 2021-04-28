package su.nexmedia.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.actions.actions.IActionExecutor;
import su.nexmedia.engine.utils.actions.actions.IActionType;
import su.nexmedia.engine.utils.actions.params.IParamResult;
import su.nexmedia.engine.utils.actions.params.IParamType;

public class Action_Titles extends IActionExecutor {

    public Action_Titles(@NotNull NexPlugin<?> plugin) {
        super(plugin, IActionType.TITLES);
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return plugin.lang().Core_Editor_Actions_Action_Titles_Desc.asList();
    }

    @Override
    public void registerParams() {
        this.registerParam(IParamType.TARGET);
        this.registerParam(IParamType.TITLES_TITLE);
        this.registerParam(IParamType.TITLES_SUBTITLE);
        this.registerParam(IParamType.TITLES_FADE_IN);
        this.registerParam(IParamType.TITLES_FADE_OUT);
        this.registerParam(IParamType.TITLES_STAY);
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
        String title = result.getParamValue(IParamType.TITLES_TITLE).getString("");
        String subtitle = result.getParamValue(IParamType.TITLES_SUBTITLE).getString("");
        int fadeIn = result.getParamValue(IParamType.TITLES_FADE_IN).getInt(0);
        int stay = result.getParamValue(IParamType.TITLES_STAY).getInt(20);
        int fadeOut = result.getParamValue(IParamType.TITLES_FADE_OUT).getInt(0);

        for (Entity e : targets) {
            if (e.getType() != EntityType.PLAYER)
                continue;
            Player player = (Player) e;
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

}
