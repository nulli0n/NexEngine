package su.nexmedia.engine.core;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.CollectionsUT;

public enum Version {

    // !!! KEEP THE VERSIONS LIST IN A ORDER FROM LOWER TO HIGHER !!!
    V1_15_R1, V1_16_R3,;

    public static final Version CURRENT;

    static {
        String[] split = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String versionRaw = split[split.length - 1];

        CURRENT = CollectionsUT.getEnum(versionRaw, Version.class);
    }

    public boolean isLower(@NotNull Version version) {
        return this.ordinal() < version.ordinal();
    }

    public boolean isHigher(@NotNull Version version) {
        return this.ordinal() > version.ordinal();
    }

    public boolean isCurrent() {
        return this == Version.CURRENT;
    }
}
