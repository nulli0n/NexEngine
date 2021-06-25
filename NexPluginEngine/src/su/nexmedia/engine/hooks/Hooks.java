package su.nexmedia.engine.hooks;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.mcmonkey.sentinel.SentinelTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.hooks.external.WorldGuardHK;
import su.nexmedia.engine.utils.Constants;

public class Hooks {

    private static final NexEngine ENGINE = NexEngine.get();

    public static final String VAULT = "Vault";
    public static final String CITIZENS = "Citizens";
    public static final String PLACEHOLDER_API = "PlaceholderAPI";
    public static final String MYTHIC_MOBS = "MythicMobs";
    public static final String WORLD_GUARD = "WorldGuard";
    public static final String HOLOGRAPHIC_DISPLAYS = "HolographicDisplays";

    @NotNull
    public static String getPermGroup(@NotNull Player player) {
        VaultHK vault = ENGINE.getVault();
        return vault != null ? vault.getPlayerGroup(player).toLowerCase() : "";
    }

    @NotNull
    public static Set<String> getPermissionGroups(@NotNull Player player) {
        VaultHK vault = ENGINE.getVault();
        return vault != null ? vault.getPlayerGroups(player) : Collections.emptySet();
    }

    public static long getGroupValueLong(@NotNull Player player, @NotNull Map<String, Long> rankMap,
            boolean isNegaBetter) {
        Set<String> groups = Hooks.getPermissionGroups(player);
        // System.out.println("[0] groups of '" + player.getName() + "': " + groups);
        // System.out.println("[1] map to compare: " + map);

        Optional<Map.Entry<String, Long>> opt = rankMap.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(Constants.DEFAULT) || groups.contains(entry.getKey()))
                .sorted((entry1, entry2) -> {
                    long val1 = entry1.getValue();
                    long val2 = entry2.getValue();
                    if (isNegaBetter && val2 < 0)
                        return 1;
                    if (isNegaBetter && val1 < 0)
                        return -1;
                    return (int) (val2 - val1);
                }).findFirst();

        // System.out.println("[2] max value for '" + player.getName() + "': " +
        // (opt.isPresent() ? opt.get() : "-1x"));

        return opt.isPresent() ? opt.get().getValue() : -1L;
    }

    public static int getGroupValueInt(@NotNull Player player, @NotNull Map<String, Integer> map,
            boolean isNegaBetter) {
        Set<String> groups = Hooks.getPermissionGroups(player);
        // System.out.println("[0] groups of '" + player.getName() + "': " + groups);
        // System.out.println("[1] map to compare: " + map);

        Optional<Map.Entry<String, Integer>> opt = map.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(Constants.DEFAULT) || groups.contains(entry.getKey()))
                .sorted((entry1, entry2) -> {
                    int val1 = entry1.getValue();
                    int val2 = entry2.getValue();
                    if (isNegaBetter && val2 < 0)
                        return 1;
                    if (isNegaBetter && val1 < 0)
                        return -1;
                    return val2 - val1;
                }).findFirst();

        // System.out.println("[2] max value for '" + player.getName() + "': " +
        // (opt.isPresent() ? opt.get() : "-1x"));

        return opt.isPresent() ? opt.get().getValue() : -1;
    }

    public static double getGroupValueDouble(@NotNull Player player, @NotNull Map<String, Double> map,
            boolean isNegaBetter) {
        Set<String> groups = Hooks.getPermissionGroups(player);
        // System.out.println("[0] groups of '" + player.getName() + "': " + groups);
        // System.out.println("[1] map to compare: " + map);

        Optional<Map.Entry<String, Double>> opt = map.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(Constants.DEFAULT) || groups.contains(entry.getKey()))
                .sorted((entry1, entry2) -> {
                    double val1 = entry1.getValue();
                    double val2 = entry2.getValue();
                    if (isNegaBetter && val2 < 0)
                        return 1;
                    if (isNegaBetter && val1 < 0)
                        return -1;
                    return (int) (val2 - val1);
                }).findFirst();

        // System.out.println("[2] max value for '" + player.getName() + "': " +
        // (opt.isPresent() ? opt.get() : "-1x"));

        return opt.isPresent() ? opt.get().getValue() : -1D;
    }

    @NotNull
    public static String getPrefix(@NotNull Player player) {
        VaultHK vault = ENGINE.getVault();
        return vault != null ? vault.getPrefix(player) : "";
    }

    @NotNull
    public static String getSuffix(@NotNull Player player) {
        VaultHK vault = ENGINE.getVault();
        return vault != null ? vault.getSuffix(player) : "";
    }

    public static boolean isNPC(@NotNull Entity entity) {
        return hasPlugin(CITIZENS) ? CitizensAPI.getNPCRegistry().isNPC(entity) : false;
    }

    public static boolean isMythic(@NotNull Entity entity) {
        MythicMobsHK mobsHK = ENGINE.getMythicMobs();
        return mobsHK == null ? false : mobsHK.isMythicMob(entity);
    }

    public static boolean hasPlugin(@NotNull String plugin) {
        Plugin p = ENGINE.getPluginManager().getPlugin(plugin);
        return p != null;// && p.isEnabled();
    }

    public static boolean hasPlaceholderAPI() {
        return Hooks.hasPlugin(PLACEHOLDER_API);
    }

    public static boolean canFights(@NotNull Entity attacker, @NotNull Entity victim) {
        if (attacker.equals(victim))
            return false;
        if (victim.isInvulnerable() || !(victim instanceof LivingEntity))
            return false;

        if (isNPC(victim)) {
            if (!hasPlugin("Sentinel")) {
                return false;
            }

            NPC npc = CitizensAPI.getNPCRegistry().getNPC(victim);
            if (!npc.hasTrait(SentinelTrait.class)) {
                return false;
            }
        }

        WorldGuardHK wg = ENGINE.getWorldGuard();
        if (wg != null) {
            if (!wg.canFights(attacker, victim)) {
                return false;
            }
        }

        return true;
    }
}
