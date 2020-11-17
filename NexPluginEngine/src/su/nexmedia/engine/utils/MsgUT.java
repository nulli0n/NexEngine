package su.nexmedia.engine.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import su.nexmedia.engine.utils.ClickText.ClickWord;

public class MsgUT {

    private static final Pattern PATTERN_JSON_FULL = Pattern.compile("(\\{json:)+(.)+?(\\})+(.*?)(\\{end-json\\})");
    private static final Pattern PATTERN_JSON_ARGUMENTS = Pattern.compile("(\\{json:)+(.)+?(\\})+(.*?)(\\})?");
    private static final String[] JSON_ARGUMENTS = new String[] {"hint", "chat-type", "chat-suggest", "url"};
	
    public static void sendActionBar(@NotNull Player player, @NotNull String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
    
    public static void sendTitles(@NotNull Player player, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    public static void sound(@NotNull Player player, @NotNull String sound) {
    	if (sound.isEmpty()) return;
		float pitch = 0.9f;
		if (sound.contains(":")) {
			pitch = (float) StringUT.getDouble(sound.split(":")[1], 0.9D);
			sound = sound.split(":")[0];
		}
		
		try {
			Sound s = Sound.valueOf(sound.toUpperCase());
			player.playSound(player.getLocation(), s, pitch, pitch);
		}
		catch (IllegalArgumentException ex) {}
    }
    
    public static void sound(@NotNull Player player, @NotNull Sound sound) {
		player.playSound(player.getLocation(), sound, 0.9F, 0.9F);
    }
	
    public static void sound(@NotNull Location loc, @NotNull String sound) {
    	if (sound.isEmpty()) return;
		World w = loc.getWorld();
		if (w == null) return;
		
		float pitch = 0.9f;
		if (sound.contains(":")) {
			pitch = (float) StringUT.getDouble(sound.split(":")[1], 0.9D);
			sound = sound.split(":")[0];
		}
		
		try {
			Sound s = Sound.valueOf(sound.toUpperCase());
			w.playSound(loc, s, pitch, pitch);
		}
		catch (IllegalArgumentException ex) {}
    }
    
    public static boolean isJSON(@NotNull String str) {
    	return str.contains("json:");
    }

	public static void sendWithJSON(@NotNull CommandSender p, @NotNull String orig) {
		orig = StringUT.color(orig);
		if (!isJSON(orig)) {
			p.sendMessage(orig);
			return;
		}
		
		Matcher mFull = PATTERN_JSON_FULL.matcher(orig);
		
		// Fix output for Console. Remove JSON arguments and send default text.
		if (!(p instanceof Player)) {
			while (mFull.find()) {
				String textToJS = mFull.group(4); // Only text to JSON
				String full = mFull.group(0); // Full string with {json start and end}
				orig = orig.replace(full, textToJS); // Replace full with only text
			}
			p.sendMessage(orig);
			return;
		}
		
		Map<String[], ClickWord> jsonMap = new HashMap<>(); // Map for placeholders
		// Loop until JSON messages ends
		while (mFull.find()) {
			String textToJS = mFull.group(4); // Only text to JSON
			String full = mFull.group(0); // Full string with {json start and end}
			String textPlaceholder = "%" + textToJS.replace(" ", "__space__") + "%";
			
			orig = orig.replace(full, textPlaceholder); // Replace full with only text
			//System.out.println("orig: " + orig);
			//System.out.println("textToJS: " + textToJS);
			//System.out.println("textPlaceholder: " + textPlaceholder);
			
			// Now search for only arguments of JSON
			Matcher mArgs = PATTERN_JSON_ARGUMENTS.matcher(full);
			
			// Probably always find
			if (mArgs.find()) {
				// String with only args
    			String arguments = mArgs.group(0).replace("{json:", "").replace("}", "").trim();
    			
    			// Create json data cache
    			ClickWord cw = new ClickText(textToJS).createPlaceholder(textPlaceholder, textToJS);
    			
    			for (String argType : JSON_ARGUMENTS) {
	    			// Search for flag of this parameter
	    			Pattern pArgVal = Pattern.compile("(~)+(" + argType + ")+?(:)+(.*?)(;)");
	    			Matcher mArgVal = pArgVal.matcher(arguments); // TODO add Fixed ICharSeq
	    			
	    			// Get the flag value
	    			if (mArgVal.find()) {
	    				// Extract only value from all flag string
	    				String argValue = mArgVal.group(4).trim();
	    				
	    				switch (argType) {
		    				case "hint": {
		    					cw.hint(argValue.split("\\|"));
		    					break;
		    				}
		    				case "chat-type": {
		    					cw.execCmd(argValue);
		    					break;
		    				}
		    				case "chat-suggest": {
		    					cw.suggCmd(argValue);
		    					break;
		    				}
		    				case "url": {
		    					cw.url(argValue);
		    					break;
		    				}
	    				}
	    			}
    			}
    			jsonMap.put(new String[] {textPlaceholder, textToJS}, cw);
			}
		}
		
		ClickText clickText = new ClickText(orig);
		for (Entry<String[], ClickWord> e : jsonMap.entrySet()) {
			String textPlaceholder = e.getKey()[0];
			String textOriginal = e.getKey()[1];
			ClickWord jsonData = e.getValue();
			
			//System.out.println("JS Place: " + textPlaceholder);
			//System.out.println("JS Orig: " + textOriginal);
			
			ClickWord clickWord = clickText.createPlaceholder(textPlaceholder, textOriginal);
			clickWord.click = jsonData.click;
			clickWord.hover = jsonData.hover;
		}
    	clickText.send(p);
	}
}
