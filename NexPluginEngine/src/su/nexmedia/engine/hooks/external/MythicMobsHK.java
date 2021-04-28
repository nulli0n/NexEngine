package su.nexmedia.engine.hooks.external;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;

public class MythicMobsHK extends NHook<NexEngine> {

    private MythicMobs mm;

    public MythicMobsHK(NexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.mm = MythicMobs.inst();
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {

    }

    public boolean isMythicMob(@NotNull Entity e) {
        return mm.getAPIHelper().isMythicMob(e);
    }

    public String getMythicNameByEntity(@NotNull Entity e) {
        return mm.getAPIHelper().getMythicMobInstance(e).getType().getInternalName();
    }

    public MythicMob getMythicInstance(@NotNull Entity e) {
        return mm.getAPIHelper().getMythicMobInstance(e).getType();
    }

    public boolean isDropTable(@NotNull String table) {
        return mm.getDropManager().getDropTable(table) != null
                && MythicMobs.inst().getDropManager().getDropTable(table).isPresent();
    }

    public double getLevel(@NotNull Entity e) {
        return mm.getAPIHelper().getMythicMobInstance(e).getLevel();
    }

    @NotNull
    public List<String> getMythicIds() {
        return new ArrayList<>(mm.getMobManager().getMobNames());
    }

    public void setSkillDamage(@NotNull Entity e, double d) {
        if (!isMythicMob(e))
            return;
        ActiveMob am1 = mm.getMobManager().getMythicMobInstance(e);
        am1.setLastDamageSkillAmount(d);
    }

    public void castSkill(@NotNull Entity e, @NotNull String skill) {
        mm.getAPIHelper().castSkill(e, skill);
    }

    public void killMythic(@NotNull Entity e) {
        if (!this.mm.getAPIHelper().getMythicMobInstance(e).isDead()) {
            this.mm.getAPIHelper().getMythicMobInstance(e).setDead();
            e.remove();
        }
    }

    public boolean isValid(@NotNull String name) {
        MythicMob koke = this.mm.getAPIHelper().getMythicMob(name);
        return koke != null;
    }

    @NotNull
    public String getName(@NotNull String mobId) {
        MythicMob koke = mm.getAPIHelper().getMythicMob(mobId);
        return koke != null ? koke.getDisplayName().get() : mobId;
    }

    @Nullable
    public Entity spawnMythicMob(@NotNull String name, @NotNull Location loc, int level) {
        try {
            MythicMob koke = mm.getAPIHelper().getMythicMob(name);
            Entity e = mm.getAPIHelper().spawnMythicMob(koke, loc, level);
            // mm.getAPIHelper().getMythicMobInstance(e).setLevel(level);
            return e;
        }
        catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
