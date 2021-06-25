package su.nexmedia.engine.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.random.Rnd;

public class EffectUT {

    public static void playEffect(@NotNull Location loc, @NotNull String eff, double x, double y, double z, double speed,
        int amount) {

        World world = loc.getWorld();
        if (world == null) return;

        String[] nameSplit = eff.split(":");
        String particleName = nameSplit[0];
        String particleData = nameSplit.length >= 2 ? nameSplit[1].toUpperCase() : null;

        Particle particle = CollectionsUT.getEnum(particleName, Particle.class);
        if (particle == null) return;

        if (particle == Particle.REDSTONE || particle == Particle.FALLING_DUST) {
            Color color = Color.WHITE;
            if (particleData != null) {
                String[] pColor = particleData.split(",");
                int r = StringUT.getInteger(pColor[0], Rnd.get(255));
                int g = pColor.length >= 2 ? StringUT.getInteger(pColor[1], Rnd.get(255)) : 0;
                int b = pColor.length >= 3 ? StringUT.getInteger(pColor[2], Rnd.get(255)) : 0;
                color = Color.fromRGB(r, g, b);
            }

            Object data = new Particle.DustOptions(color, 1.5f);
            world.spawnParticle(particle, loc, amount, x, y, z, speed, data);
            return;
        }

        if (particle == Particle.BLOCK_CRACK || particle == Particle.LEGACY_BLOCK_CRACK) {
            Material m = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            BlockData blockData = m != null ? m.createBlockData() : Material.STONE.createBlockData();
            world.spawnParticle(particle, loc, amount, x, y, z, speed, blockData);
            return;
        }

        if (particle == Particle.ITEM_CRACK) {
            Material m = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            ItemStack item = m != null ? new ItemStack(m) : new ItemStack(Material.STONE);
            world.spawnParticle(particle, loc, amount, x, y, z, speed, item);
            return;
        }

        world.spawnParticle(particle, loc, amount, x, y, z, speed);
    }

    public static void playEffect(@NotNull Player player, @NotNull Location loc, @NotNull String eff, double x, double y, double z, double speed,
        int amount) {

        String[] nameSplit = eff.split(":");
        String particleName = nameSplit[0];
        String particleData = nameSplit.length >= 2 ? nameSplit[1].toUpperCase() : null;

        Particle particle = CollectionsUT.getEnum(particleName, Particle.class);
        if (particle == null) return;

        if (particle == Particle.REDSTONE || particle == Particle.FALLING_DUST) {
            Color color = Color.WHITE;
            if (particleData != null) {
                String[] pColor = particleData.split(",");
                int r = StringUT.getInteger(pColor[0], Rnd.get(255));
                int g = pColor.length >= 2 ? StringUT.getInteger(pColor[1], Rnd.get(255)) : 0;
                int b = pColor.length >= 3 ? StringUT.getInteger(pColor[2], Rnd.get(255)) : 0;
                color = Color.fromRGB(r, g, b);
            }

            Object data = new Particle.DustOptions(color, 1.5f);
            player.spawnParticle(particle, loc, amount, x, y, z, speed, data);
            return;
        }

        if (particle == Particle.BLOCK_CRACK || particle == Particle.LEGACY_BLOCK_CRACK) {
            Material m = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            BlockData blockData = m != null ? m.createBlockData() : Material.STONE.createBlockData();
            player.spawnParticle(particle, loc, amount, x, y, z, speed, blockData);
            return;
        }

        if (particle == Particle.ITEM_CRACK) {
            Material m = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            ItemStack item = m != null ? new ItemStack(m) : new ItemStack(Material.STONE);
            player.spawnParticle(particle, loc, amount, x, y, z, speed, item);
            return;
        }

        player.spawnParticle(particle, loc, amount, x, y, z, speed);
    }
    
    public static void drawLine(Location from, Location to, String pe, float offX, float offY, float offZ, float speed,
        int amount) {
        Location origin = from.clone();
        Vector target = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ()).toVector();
        origin.setDirection(target.subtract(origin.toVector()));
        Vector increase = origin.getDirection();

        for (int counter = 0; counter < from.distance(to); counter++) {
            Location loc = origin.add(increase);
            EffectUT.playEffect(loc, pe, offX, offY, offZ, speed, 5);
        }
    }

}
