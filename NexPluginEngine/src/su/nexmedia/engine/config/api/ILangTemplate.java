package su.nexmedia.engine.config.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.StringUT;

public abstract class ILangTemplate {

	protected NexPlugin<?> plugin;
	protected JYML config;
	
	public ILangTemplate(@NotNull NexPlugin<?> plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfigManager().configLang;
	}

	public void setup() {
	    this.load();
	    this.setupEnums();
	    this.config.saveChanges();
	}

	protected void setupEnums() {
		
	}
	
	protected void setupEnum(@NotNull Class<? extends Enum<?>> clazz) {
		if (!clazz.isEnum()) return;
		for (Object o : clazz.getEnumConstants()) {
			if (o == null) continue;
			
			String name = o.toString();
			String path = clazz.getSimpleName() + "." + name;
			String val = StringUT.capitalizeFully(name.replace("_", " "));
			this.config.addMissing(path, val);
		}
	}

	public JLangMsg Prefix = new JLangMsg("&e%plugin% &8» &7");

	public JLangMsg Core_Command_Usage = new JLangMsg("&c* Usage: &f/%label% %cmd% %usage%");
	public JLangMsg Core_Command_Help_Format = new JLangMsg("&6» &e/%label% %cmd% %usage% &7- %description%");
	public JLangMsg Core_Command_Help_List = new JLangMsg(
			"&8&m━━━━━━━━━━━━&8&l[ &e&l%plugin% &7- &6&lHelp &8&l]&8&m━━━━━━━━━━━━"
			+ "\n"
			+ "%cmds%");
	public JLangMsg Core_Command_Help_Desc = new JLangMsg("Show help page.");
	public JLangMsg Core_Command_Editor_Desc = new JLangMsg("Opens GUI Editor.");
	public JLangMsg Core_Command_About_Desc = new JLangMsg("Some info about the plugin.");
	public JLangMsg Core_Command_Reload_Desc = new JLangMsg("Reload the plugin.");
	public JLangMsg Core_Command_Reload_Done = new JLangMsg("Reloaded!");

	public JLangMsg Core_Editor_Tips_Commands = new JLangMsg(
			"&7"
			+ "\n"
			+ "&b&lCommand Tips:"
			+ "\n"
			+ "&7"
			+ "\n"
			+ "&2• &a[CONSOLE] <command> &2- Execute from Console."
			+ "\n"
			+ "&2• &a[OP] <command> &2- Execute as an Operator."
			+ "\n"
			+ "&2• (no prefix) &a<command> &2- Execute from a Player."
			+ "\n"
			+ "&2• &a%player% &2- Player name placeholder."
			+ "\n"
			+ "&7");
	
	public JLangMsg Core_Editor_Tips_Header = new JLangMsg(
			"&7"
			+ "\n"
			+ "&e&lSUGGESTED (ALLOWED) VALUES:"
			+ "\n");
	public JLangMsg Core_Editor_Tips_Hint = new JLangMsg("&b&nClick to select!");
	
	public JLangMsg Core_Editor_Tips_Exit_Name = new JLangMsg("&b<Click this message to &dExit &bthe &dEdit Mode&b>");
	public JLangMsg Core_Editor_Tips_Exit_Hint = new JLangMsg("&7Click to exit edit mode.");
	public JLangMsg Core_Editor_Display_Edit_Format = new JLangMsg(
			"%title%"
			+ "\n"
			+ "&7%message%");
	public JLangMsg Core_Editor_Display_Done_Title = new JLangMsg("&a&lDone!");
	public JLangMsg Core_Editor_Display_Edit_Title = new JLangMsg("&a&lEditing...");
	public JLangMsg Core_Editor_Display_Error_Title = new JLangMsg("&c&lError!");
	public JLangMsg Core_Editor_Display_Error_Number_Invalid = new JLangMsg("&c&lInvalid number!");
	public JLangMsg Core_Editor_Display_Error_Number_MustDecimal = new JLangMsg("&7Must be &cInteger &7or &cDecimal");
	public JLangMsg Core_Editor_Display_Error_Number_MustInteger = new JLangMsg("&7Must be &cInteger");
	public JLangMsg Core_Editor_Display_Error_Type_Title = new JLangMsg("&c&lInvalid Type!");
	public JLangMsg Core_Editor_Display_Error_Type_Values = new JLangMsg("&7See allowed values in chat.");
	
	public JLangMsg Core_Editor_Actions_Section_Add = new JLangMsg("&7Enter unique section id...");
	public JLangMsg Core_Editor_Actions_Subject_Add = new JLangMsg("&7Select a subject...");
	public JLangMsg Core_Editor_Actions_Subject_Invalid = new JLangMsg("&cInvalid provided!");
	public JLangMsg Core_Editor_Actions_Subject_Hint = new JLangMsg(
			"%description%"
			+ "\n"
			+ "&7"
			+ "\n"
			+ "&d&nClick to select!");
	public JLangMsg Core_Editor_Actions_Subject_NoParams = new JLangMsg("&c* No Params Available *");
	
	public JLangMsg Core_Editor_Actions_Param_Add = new JLangMsg("&7Select a param and type it's value...");
	public JLangMsg Core_Editor_Actions_Param_Edit = new JLangMsg("&7Enter a new value...");
	public JLangMsg Core_Editor_Actions_Param_Hint = new JLangMsg("&d&nClick to select!");
	public JLangMsg Core_Editor_Actions_Param_Invalid = new JLangMsg("&cNo such param!");
	
	public JLangMsg Core_Editor_Actions_Action_ActionBar_Desc = new JLangMsg("&7Sends a message to action bar.");
	public JLangMsg Core_Editor_Actions_Action_Broadcast_Desc = new JLangMsg("&7Broadcasts a message to whole server.");
	public JLangMsg Core_Editor_Actions_Action_Burn_Desc = new JLangMsg("&7Burns an entity.");
	public JLangMsg Core_Editor_Actions_Action_CommandConsole_Desc = new JLangMsg("&7Executes a command from console.");
	public JLangMsg Core_Editor_Actions_Action_CommandOp_Desc = new JLangMsg("&7Executes a command with OP permissions.");
	public JLangMsg Core_Editor_Actions_Action_CommandPlayer_Desc = new JLangMsg("&7Executes a command by a player.");
	public JLangMsg Core_Editor_Actions_Action_Hunger_Desc = new JLangMsg("&7Changes player's hunger level.");
	public JLangMsg Core_Editor_Actions_Action_Saturation_Desc = new JLangMsg("&7Changes player's saturation level.");
	public JLangMsg Core_Editor_Actions_Action_Damage_Desc = new JLangMsg("&7Damages an entity.");
	public JLangMsg Core_Editor_Actions_Action_Firework_Desc = new JLangMsg("&7Launches a random firework.");
	public JLangMsg Core_Editor_Actions_Action_Goto_Desc = new JLangMsg("&7Executes actions of certain actions section.");
	public JLangMsg Core_Editor_Actions_Action_Health_Desc = new JLangMsg("&7Changes entity's health level.");
	public JLangMsg Core_Editor_Actions_Action_Hook_Desc = new JLangMsg("&7Pulls towards the target.");
	public JLangMsg Core_Editor_Actions_Action_Lightning_Desc = new JLangMsg("&7Summons a lightning strike.");
	public JLangMsg Core_Editor_Actions_Action_Message_Desc = new JLangMsg("&7Send a message to a target.");
	public JLangMsg Core_Editor_Actions_Action_ParticleSimple_Desc = new JLangMsg("&7Plays certain particle.");
	public JLangMsg Core_Editor_Actions_Action_Potion_Desc = new JLangMsg("&7Adds certain potion effect.");
	public JLangMsg Core_Editor_Actions_Action_ProgressBar_Desc = new JLangMsg("&7Displays progress bar.");
	public JLangMsg Core_Editor_Actions_Action_Projectile_Desc = new JLangMsg("&7Launches certain projectile.");
	public JLangMsg Core_Editor_Actions_Action_Sound_Desc = new JLangMsg("&7Plays certain sound.");
	public JLangMsg Core_Editor_Actions_Action_Teleport_Desc = new JLangMsg("&7Teleport to the target.");
	public JLangMsg Core_Editor_Actions_Action_Throw_Desc = new JLangMsg("&7Pulls away targets.");
	public JLangMsg Core_Editor_Actions_Action_Titles_Desc = new JLangMsg("&7Shows custom titles.");
	
	public JLangMsg Core_Editor_Actions_Condition_EntityHealth_Desc = new JLangMsg("&7Checks the target's health.");
	public JLangMsg Core_Editor_Actions_Condition_EntityType_Desc = new JLangMsg("&7Checks the target's type.");
	public JLangMsg Core_Editor_Actions_Condition_Permission_Desc = new JLangMsg("&7Checks the target's permission.");
	public JLangMsg Core_Editor_Actions_Condition_VaultBalance_Desc = new JLangMsg("&7Checks the player's balance.");
	public JLangMsg Core_Editor_Actions_Condition_WorldTime_Desc = new JLangMsg("&7Checks the world's time.");
	
	public JLangMsg Core_Editor_Actions_TargetSelector_FromSight_Desc = new JLangMsg("&7Selects a target from executor's sight.");
	public JLangMsg Core_Editor_Actions_TargetSelector_Radius_Desc = new JLangMsg("&7Selects targets in a radius.");
	public JLangMsg Core_Editor_Actions_TargetSelector_Self_Desc = new JLangMsg("&7Selects executor as a target.");
	
	
	public JLangMsg Time_Day = new JLangMsg("%s%d.");
	public JLangMsg Time_Hour = new JLangMsg("%s%h.");
	public JLangMsg Time_Min = new JLangMsg("%s%min.");
	public JLangMsg Time_Sec = new JLangMsg("%s%sec.");
	
	public JLangMsg Other_Yes = new JLangMsg("&aYes");
	public JLangMsg Other_No = new JLangMsg("&cNo");
	
	public JLangMsg Error_NoPlayer = new JLangMsg("&cPlayer not found.");
	public JLangMsg Error_NoWorld = new JLangMsg("&cWorld not found.");
	public JLangMsg Error_Number = new JLangMsg("&7%num% &cis not a valid number.");
	public JLangMsg Error_NoPerm = new JLangMsg("&cYou don't have permissions to do that!");
	public JLangMsg Error_NoData = new JLangMsg("&4Error while get data for &c%player%&4.");
	public JLangMsg Error_NoItem = new JLangMsg("&cYou must hold an item!");
	public JLangMsg Error_Type = new JLangMsg("Invalid type. Available: %types%");
	public JLangMsg Error_Self = new JLangMsg("Can not be used on yourself.");
	public JLangMsg Error_Sender = new JLangMsg("This command is for players only.");
    public JLangMsg Error_Internal = new JLangMsg("&cInternal error!");
	
	@NotNull
    public String getEnum(@NotNull Enum<?> e) {
    	String path = e.getClass().getSimpleName() + "." + e.name();
    	String locEnum = this.getCustom(path);
    	if (locEnum == null && !this.plugin.isEngine()) {
    		return NexPlugin.getEngine().lang().getEnum(e);
    	}
    	return locEnum == null ? "null" : locEnum;
    }
	
	@NotNull
	public String getPotionType(@NotNull PotionEffectType type) {
		if (!this.plugin.isEngine()) return NexEngine.get().lang().getPotionType(type);
		return this.config.getString("PotionEffectType." + type.getName(), type.getName());
	}
    
	@NotNull
	public String getEnchantment(@NotNull Enchantment e) {
		if (!this.plugin.isEngine()) return NexEngine.get().lang().getEnchantment(e);
		this.config.addMissing("Enchantment." + e.getKey().getKey(), StringUT.capitalizeFully(e.getKey().getKey()));
		this.config.saveChanges();
		
		return this.config.getString("Enchantment." + e.getKey().getKey(), e.getKey().getKey().replace("_", " "));
	}
	
	@NotNull
    public String getBool(boolean b) {
    	if (b) return this.Other_Yes.getMsg();
    	else return this.Other_No.getMsg();
    }
    
    @Nullable
    public String getCustom(@NotNull String path) {
    	String str = this.config.getString(path);
    	return str == null ? str : StringUT.color(str);
    }
    
    @NotNull
    protected List<Field> getFields(@NotNull Class<?> type) {
        List<Field> result = new ArrayList<>();

        Class<?> clazz = type;
        while (clazz != null && clazz != Object.class) {
        	if (!result.isEmpty()) {
        		result.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
        	}
        	else {
        		Collections.addAll(result, clazz.getDeclaredFields());
        	}
        	clazz = clazz.getSuperclass();
        }
        
        return result;
    }
    
    private void load() {
    	ILangTemplate superLang = NexEngine.get().lang();

    	for (Field field : getFields(this.getClass())) {
    		if (!JLangMsg.class.isAssignableFrom(field.getType())) {
    			continue;
    		}
    		
	        JLangMsg jmsg;
			try {
				jmsg = (JLangMsg) field.get(this);
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				continue;
			}
			
			jmsg.setPath(field.getName()); // Set the path to String in config
			
			// Fill super field values with strings from Engine
			if (!field.getDeclaringClass().equals(this.getClass())) {
				Object superField = Reflex.getFieldValue(superLang, field.getName());
				if (superField != null) {
					JLangMsg superMsg = (JLangMsg) superField;
					jmsg.setMsg(superMsg.getMsg().replace("%plugin%", plugin.cfg().pluginName));
					if (!this.plugin.isEngine()) {
						continue;
					}
				}
			}
			
			String path = jmsg.getPath();
			JYML cfg = this.config;
			
			// Add missing lang node in config.
	        if (cfg.getString(path) == null && !cfg.contains(path)) {
	           	String msg = jmsg.getDefaultMsg();
	           	String[] ss = msg.split("\n");
	           	if (ss.length > 1) {
	           		cfg.set(path, Arrays.asList(ss));
	           	}
	           	else {
	           		cfg.set(path, msg);
	           	}
	        }
	        
	        // Fill values from lang config
	        String msgLoad = null;
	        List<String> cList = cfg.getStringList(path);
	        if (!cList.isEmpty()) {
	           	String msg = "";
	          	for (String s2 : cList) {
	           		if (msg.isEmpty()) msg = msg + s2;
	           		else msg = msg + "\\n" + s2;
	           	}
	          	msgLoad = msg;
	        }
	        else {
	        	msgLoad = cfg.getString(path, "");
	        }
        	jmsg.setMsg(msgLoad);
    	}
    	this.config.saveChanges();
    }
    
    public class JLangMsg {

    	private String msgDefault;
    	private String msgColor;
    	private String path;
    	private OutputType out = OutputType.CHAT;
    	private boolean isJson;
    	
    	public JLangMsg(@NotNull String msg) {
    		this.msgDefault = msg;
    		this.setMsg(msg);
    	}
    	
    	JLangMsg(@NotNull JLangMsg from) {
    		this.msgDefault = from.getDefaultMsg();
    		this.msgColor = from.getMsg();
    		this.path = from.getPath();
    		this.out = from.out;
    		this.isJson = from.isJson;
    	}
    	
    	private void setPath(@NotNull String path) {
    		this.path = path.replace("_", ".");
    	}
    	
    	@NotNull
    	public String getPath() {
    		return this.path;
    	}
    	
    	/**
    	 * Replaces the colored non-default message value as well as output type.
    	 * @param msg New message
    	 */
    	private void setMsg(@NotNull String msg) {
    		this.out = OutputType.getType(msg);
    		this.isJson = MsgUT.isJSON(msg) && this.out == OutputType.CHAT;
    		this.msgColor = OutputType.clearPrefix(msg);
    		if (!this.isJson()) {
    			this.msgColor = StringUT.color(this.msgColor);
    		}
    	}
    	
    	@NotNull
    	public String getDefaultMsg() {
    		return this.msgDefault;
    	}
    	
    	@NotNull
    	public String getMsg() {
    		return this.msgColor;
    	}
    	
    	public final boolean isJson() {
			return this.isJson;
		}
    	
    	@SuppressWarnings("unchecked")
		@NotNull
    	public JLangMsg replace(@NotNull String var, @NotNull Object replacer) {
    		if (this.isEmpty()) return this;
    		if (replacer instanceof List) return this.replace(var, (List<Object>) replacer);
    		
    		return this.replace(str -> str.replace(var, String.valueOf(replacer)));
    	}
    	
		@NotNull
		public JLangMsg replace(@NotNull String var, @NotNull List<Object> replacer) {
			if (this.isEmpty()) return this;
			
			StringBuilder builder = new StringBuilder();
			replacer.forEach(rep -> {
				if (builder.length() > 0) builder.append("\\n");
				builder.append(rep.toString());
			});
			
			return this.replace(str -> str.replace(var, builder.toString()));
		}

		@NotNull
    	public JLangMsg replace(@NotNull UnaryOperator<String> replacer) {
			if (this.isEmpty()) return this;
			
    		JLangMsg msgCopy = new JLangMsg(this);
    		msgCopy.msgColor = StringUT.color(replacer.apply(msgCopy.getMsg()));
    		return msgCopy;
    	}
    	
    	public boolean isEmpty() {
			return (this.out == OutputType.NONE || this.getMsg().isEmpty());
		}

		public void broadcast(boolean prefix) {
    		if (this.isEmpty()) return;
    		for (Player p : plugin.getServer().getOnlinePlayers()) {
    			if (p == null) continue;
				this.send(p, prefix);
			}
    		this.send(plugin.getServer().getConsoleSender(), prefix);
    	}
    	
    	public void send(@NotNull CommandSender sender) {
			this.send(sender, false);
		}

		public void send(@NotNull CommandSender sender, boolean prefix) {
    		if (this.isEmpty()) return;
    		
    		if (this.out == OutputType.CHAT) {
	    		for (String line : this.asList()) {
	    			if (prefix) line = Prefix.getMsg() + line;
	    			
	    			MsgUT.sendWithJSON(sender, line);
	    		}
    		}
    		else if (sender instanceof Player) {
    			if (this.out == OutputType.ACTION_BAR) {
	    			MsgUT.sendActionBar((Player) sender, this.getMsg());
	    		}
    			else if (this.out == OutputType.TITLES) {
	    			this.title((Player) sender, 10, 50, 10);
	    		}
    		}
    	}
    	
    	public void title(@NotNull Player player, int in, int stay, int out) {
    		List<String> list = this.asList();
    		String up = list.get(0);
    		String down = list.size() > 1 ? list.get(1) : "";
    		MsgUT.sendTitles(player, up, down, in, stay, out);
    	}
    	
    	@NotNull
        public List<String> asList() {
        	String msg = this.getMsg();
    		if (msg.isEmpty()) return Collections.emptyList();
    		
        	List<String> list = new ArrayList<>();
    		for (String line : msg.split("\\\\n")) {
    			list.add(line.trim());
    		}
        	return list;
        }
    	
    	/**
    	 * Replaces a raw '\n' new line splitter with a system one.
    	 * @return A string with a system new line splitters.
    	 */
    	@NotNull
        public String normalizeLines() {
    		StringBuilder text = new StringBuilder("");
    		for (String line : this.asList()) {
    			if (text.length() > 0) {
    				text.append("\n");
    			}
    			text.append(line);
    		}
    		return text.toString();
        }
    }
    
    public static enum OutputType {

    	CHAT,
    	ACTION_BAR,
    	TITLES,
    	NONE,
    	;
    	
    	public static void msg(@NotNull Player p, @NotNull String msg) {
    		OutputType type = getType(msg);
    		msg = clearPrefix(msg);
    		if (msg.isEmpty()) return;
    		
    		switch (type) {
    			case CHAT: {
    				String[] spl = msg.split("/n");
    				for (String msg2 : spl) {
    					p.sendMessage(msg2.trim());
    				}
    				break;
    			}
    			case ACTION_BAR: {
    				msg = msg.replace("/n", "");
    				MsgUT.sendActionBar(p, msg);
    				break;
    			}
    			case TITLES: {
    				String[] spl = msg.split("/n");
    				String up = spl[0].trim();
    				String down = "";
    				if (spl.length >= 2) {
    					down = spl[1].trim();
    				}
    				MsgUT.sendTitles(p, up, down, 10, 40, 10);
    				break;
    			}
    			case NONE: {
    				break;
    			}
    		}
    	}
    	
    	@NotNull
    	public static String clearPrefix(@NotNull String msg) {
    		for (OutputType type : values()) {
    			String name = type.name();
    			msg = msg.replace("[" + name + "]", "").trim();
    		}
    		return msg;
    	}
    	
    	@NotNull
    	public static OutputType getType(@NotNull String msg) {
    		if (msg.isEmpty()) return OutputType.NONE;
    		
    		if (msg.startsWith("[")) {
    			for (OutputType type : values()) {
    				String name = type.name();
    				if (msg.startsWith("[" + name + "]")) {
    					return type;
    				}
    			}
    		}
    		return OutputType.CHAT;
    	}
    }
}
