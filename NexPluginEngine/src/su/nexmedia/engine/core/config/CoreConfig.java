package su.nexmedia.engine.core.config;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.config.api.IConfigTemplate;
import su.nexmedia.engine.utils.StringUT;

public class CoreConfig extends IConfigTemplate {

    public static String MODULES_PATH_INTERNAL = "/modules/";
    public static String MODULES_PATH_EXTERNAL = "/modules/_external/";

    private static Map<String, String> LOCALE_WORLD_NAMES;

    public CoreConfig(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        this.cfg.addMissing("locale.world-names.world", "World");
        this.cfg.addMissing("locale.world-names.world_nether", "Nether");
        this.cfg.addMissing("locale.world-names.world_the_end", "The End");

        LOCALE_WORLD_NAMES = new HashMap<>();
        this.cfg.getSection("locale.world-names").forEach(world -> {
            String name = StringUT.color(this.cfg.getString("locale.world-names." + world, world));
            LOCALE_WORLD_NAMES.put(world, name);
        });
    }

    @NotNull
    public static String getWorldName(@NotNull String world) {
        return LOCALE_WORLD_NAMES.getOrDefault(world, world);
    }
}
