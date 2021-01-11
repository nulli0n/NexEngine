package su.nexmedia.engine.utils.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Rnd {
	
    public static final MTRandom rnd;
    
    private static final List<String> MATERIAL_COLORS;
    
    static {
	    rnd = new MTRandom();
	    
	    MATERIAL_COLORS = Arrays.asList(
				"WHITE",
				"BLACK",
				"ORANGE",
				"YELLOW",
				"RED",
				"GREEN",
				"LIME",
				"BLUE",
				"CYAN",
				"BROWN",
				"GRAY",
				"PURPLE",
				"PINK",
				"MAGENTA",
				"LIGHT_GRAY",
				"LIGHT_BLUE"
			);
	}

	public static float get() {
        return Rnd.rnd.nextFloat();
    }
    
    public static float get(boolean normalize) {
        float f = Rnd.get();
        if (normalize) f *= 100f;
        return f;
    }
    
    public static int get(int n) {
        return Rnd.nextInt(n);
    }
    
    public static int get(int min, int max) {
        return min + (int) Math.floor(Rnd.rnd.nextDouble() * (max - min + 1));
    }
    
	public static double getDouble(double max) {
		return getDouble(0, max);
	}
    
	public static double getDouble(double min, double max) {
		return min + (max - min) * rnd.nextDouble();
	}
	
	public static double getDoubleNega(double min, double max) {
		double range = max - min;
		double scaled = rnd.nextDouble() * range;
		double shifted = scaled + min;
		return shifted;
	}
    
    @NotNull
    public static <E> E get(@NotNull E[] list) {
        return list[get(list.length)];
    }
    
    public static int get(int[] list) {
        return list[get(list.length)];
    }
    
    @Nullable
    public static <E> E get(@NotNull List<E> list) {
        return list.isEmpty() ? null : list.get(get(list.size()));
    }
    
	@Nullable
	public static <T> T get(@NotNull Map<@NotNull T, Double> map) {
		List<T> list = get(map, 1);
		return list.isEmpty() ? null : list.get(0);
	}
	
	@NotNull
	public static <T> List<T> get(@NotNull Map<@NotNull T, Double> map, int amount) {
		map.values().removeIf(chance -> chance <= 0D);
		if (map.isEmpty()) return Collections.emptyList();
		
		List<T> list = new ArrayList<>();
		double total = map.values().stream().mapToDouble(d -> d).sum();
		
	    for (int count = 0; count < amount; count++) {
	    	double index = Rnd.getDouble(0D, total);//Math.random() * total;
	        double countWeight = 0D;
	        
	        for (Map.Entry<T, Double> en : map.entrySet()) {
	            countWeight += en.getValue();
	            if (countWeight >= index) {
	    	        list.add(en.getKey());
	    	        break;
	            }
	        }
	    }
	    return list;
	}
	
	@Nullable
	@Deprecated
	public static <T> T getRandomItem(@NotNull Map<T, Double> map) {
		return getRandomItem(map, true);
	}

	@Deprecated
	@Nullable
    public static <T> T getRandomItem(@NotNull Map<T, Double> map, boolean alwaysHundred) {
	    return get(map);
	}
    
    public static boolean chance(int chance) {
	    return chance >= 1 && (chance > 99 || nextInt(99) + 1 <= chance);
	}

	public static boolean chance(double chance) {
	    return nextDouble() <= chance / 100.0;
	}

	public static int nextInt(int n) {
        return (int)Math.floor(Rnd.rnd.nextDouble() * n);
    }
    
    public static int nextInt() {
        return Rnd.rnd.nextInt();
    }
    
    public static double nextDouble() {
        return Rnd.rnd.nextDouble();
    }
    
    public static double nextGaussian() {
        return Rnd.rnd.nextGaussian();
    }
    
    public static boolean nextBoolean() {
        return Rnd.rnd.nextBoolean();
    }
    
    
    
    
    @NotNull
    public static Firework spawnRandomFirework(@NotNull Location loc) {
    	World w = loc.getWorld();
    	if (w == null) w = Bukkit.getWorlds().get(0);
    	
        Firework fw = (Firework) w.spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta meta = fw.getFireworkMeta();
        FireworkEffect.Type type = Rnd.get(FireworkEffect.Type.values());
        Color c1 = Color.fromBGR(Rnd.nextInt(254), Rnd.nextInt(254), Rnd.nextInt(254));
        Color c2 = Color.fromBGR(Rnd.nextInt(254), Rnd.nextInt(254), Rnd.nextInt(254));
        FireworkEffect effect = FireworkEffect.builder().flicker(Rnd.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(Rnd.nextBoolean()).build();
        meta.addEffect(effect);
        
        int power = Rnd.get(5);
        meta.setPower(power);
        fw.setFireworkMeta(meta);
        
        return fw;
    }
    
    @NotNull
    public static Material getColoredMaterial(@NotNull Material m) {
    	String name = m.name();
    	for (String c : Rnd.MATERIAL_COLORS) {
    		if (name.startsWith(c)) {
    			String color = Rnd.get(Rnd.MATERIAL_COLORS);
    			name = name.replace(c, color);
    			break;
    		}
    	}
    	
    	Material get = Material.getMaterial(name);
    	if (get == null) {
    		return Material.BARRIER;
    	}
    	return get;
    }
}
