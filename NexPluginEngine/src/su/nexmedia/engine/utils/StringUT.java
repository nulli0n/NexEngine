package su.nexmedia.engine.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.ChatColor;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.core.Version;

public class StringUT {

	public static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
	
	@NotNull
	public static String oneSpace(@NotNull String str) {
		return str.trim().replaceAll("\\s+", " ");
	}
	
	@NotNull
	public static String noSpace(@NotNull String str) {
		return str.trim().replaceAll("\\s+", "");
	}
	
	@NotNull
	public static String color(@NotNull String str) {
		if (Version.CURRENT.isHigher(Version.V1_15_R1)) str = colorHex(str);
		return ChatColor.translateAlternateColorCodes('&', colorFix(str));
	}
	
	/**
	 * Removes multiple color codes that are 'color of color'. Example: '&a&b&cText' -> '&cText'.
	 * @param str String to fix.
	 * @return A string with a proper color codes formatting.
	 */
	@NotNull
	public static String colorFix(@NotNull String str) {
		return NexEngine.get().getNMS().fixColors(str);
	}
	
	@NotNull
    public static String colorHex(@NotNull String str) {
        Matcher matcher = HEX_PATTERN.matcher(str);
        StringBuffer buffer = new StringBuffer(str.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }
	
	@NotNull
    public static String colorHexRaw(@NotNull String str) {
		if (Version.CURRENT.isLower(Version.V1_16_R2)) return str;
		
		StringBuffer buffer = new StringBuffer(str);
		
		int index;
		while ((index = buffer.toString().indexOf(ChatColor.COLOR_CHAR + "x")) >= 0) {
			int count = 0;
			buffer = buffer.replace(index, index + 2, "#");
			
			for (int point = index + 1; count < 6; point += 1) {
				buffer = buffer.deleteCharAt(point);
				count++;
			}
		}
		
        return buffer.toString();
    }
	
	@NotNull
	public static String colorRaw(@NotNull String str) {
		return str.replace(ChatColor.COLOR_CHAR, '&');
	}
	
	@NotNull
	public static String colorOff(@NotNull String str) {
		String off = ChatColor.stripColor(str);
		return off == null ? "" : off;
	}
	
	@NotNull
	public static List<String> color(@NotNull List<String> list) {
		list.replaceAll(line -> color(line));
		return list;
	}
	
	@NotNull
	public static Set<String> color(@NotNull Set<String> list) {
		return new HashSet<>(StringUT.color(new ArrayList<>(list)));
	}
	
    public static double getDouble(@NotNull String input, double def) {
        return getDouble(input, def, false);
    }
    
    public static double getDouble(@NotNull String input, double def, boolean allowNega) {
        try {
            double amount = Double.parseDouble(input);
            if (amount < 0.0 && !allowNega) {
                throw new NumberFormatException();
            }
            return amount;
        }
        catch (NumberFormatException ex) {
            return def;
        }
    }
    
    public static int getInteger(@NotNull String input, int def) {
        return getInteger(input, def, false);
    }
    
    public static int getInteger(@NotNull String input, int def, boolean nega) {
        return (int) getDouble(input, def, nega);
    }
    
	public static int[] getIntArray(@NotNull String str) {
		int[] slots = new int[1];
		String[] raw = str.replaceAll("\\s", "").split(",");
		slots = new int[raw.length];
		for (int i = 0; i < raw.length; i++) {
			try {
				slots[i] = Integer.parseInt(raw[i].trim());
			}
			catch (NumberFormatException ex) {}
		}
		return slots;
	}
	
	@NotNull
	public static String capitalizeFully(@NotNull String str) {
		return WordUtils.capitalizeFully(str);
	}
	
	@NotNull
    public static String capitalizeFirstLetter(@NotNull String original) {
        if (original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
	
	@NotNull
    public static List<String> getByFirstLetters(@NotNull String arg, @NotNull List<String> source) {
    	List<String> ret = new ArrayList<>();
    	List<String> sugg = new ArrayList<>(source);
    	StringUtil.copyPartialMatches(arg, sugg, ret);
        Collections.sort(ret);
    	return ret;
    }
	
	@NotNull
	public static String extractCommandName(@NotNull String cmd) {
		String cmdFull = colorOff(cmd).split(" ")[0];
		String cmdName = cmdFull.replace("/", "").replace("\\/", "");
		String[] pluginPrefix = cmdName.split(":");
		if (pluginPrefix.length == 2) {
			cmdName = pluginPrefix[1];
		}
		
		return cmdName;
	}
	
	public static boolean isCustomBoolean(@NotNull String str) {
		if (str.equalsIgnoreCase("0") || str.equalsIgnoreCase("off")) {
			return true;
		}
		if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("on")) {
			return true;
		}
		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
			return true;
		}
		if (str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("no")) {
			return true;
		}
		
		return false;
	}
	
	public static boolean parseCustomBoolean(@NotNull String str) {
		if (str.equalsIgnoreCase("0") || str.equalsIgnoreCase("off")
				|| str.equals("no")) {
			return false;
		}
		if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("on")
				|| str.equals("yes")) {
			return true;
		}
		return Boolean.parseBoolean(str);
	}
	
	@NotNull
    public static String c(@NotNull String s) {
    	char[] ch = s.toCharArray();
    	char[] out = new char[ch.length * 2];
    	int i = 0;
    	for (char c : ch) {
    		int orig = Character.getNumericValue(c);
    		int min;
    		int max;
    		
    		char cas;
    		if (Character.isUpperCase(c)) {
    			min = Character.getNumericValue('A');
    			max = Character.getNumericValue('Z');
    			cas = 'q';
    		}
    		else {
    			min = Character.getNumericValue('a');
    			max = Character.getNumericValue('z');
    			cas = 'p';
    		}
    		
    		int pick = min + (max - orig);
    		char get = Character.forDigit(pick, Character.MAX_RADIX);
    		out[i] = get;
    		out[++i] = cas;
    		i++;
    	}
    	return String.valueOf(out);
    }
    
	@NotNull
    public static String d(@NotNull String s) {
    	char[] ch = s.toCharArray();
    	char[] dec = new char[ch.length / 2];
    	for (int i = 0; i < ch.length; i = i + 2) {
    		int j = i;
    		char letter = ch[j];
    		char cas = ch[++j];
    		boolean upper = cas == 'q';
    		
    		int max;
    		int min;
    		if (upper) {
    			min = Character.getNumericValue('A');
    			max = Character.getNumericValue('Z');
    		}
    		else {
    			min = Character.getNumericValue('a');
    			max = Character.getNumericValue('z');
    		}
    		
    		int orig = max - Character.getNumericValue(letter) + min;
    		char get = Character.forDigit(orig, Character.MAX_RADIX);
    		if (upper) get = Character.toUpperCase(get);
    		
    		dec[i/2] = get;
    	}
    	return String.valueOf(dec);
    }
}
