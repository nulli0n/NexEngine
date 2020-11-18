package su.nexmedia.engine.manager.editor;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ClickText;
import su.nexmedia.engine.utils.ClickText.ClickWord;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.constants.JStrings;

public class EditorManager {

	private static final NexEngine ENGINE;
	
	private static final Map<Player, Map.Entry<Enum<?>, Object>> EDITOR_CACHE = new WeakHashMap<>();
	public static JYML EDITOR_ACTIONS_MAIN;
	public static JYML EDITOR_ACTIONS_SECTION;
	public static JYML EDITOR_ACTIONS_PARAMETIZED;
	public static JYML EDITOR_ACTIONS_PARAMS;
	
	public static EditorActionsHandler actionsHandler;
	
	static {
		ENGINE = NexEngine.get();
	}
	
	public static void setup() {
		ENGINE.getConfigManager().extract("editor");
		
		if (EDITOR_ACTIONS_MAIN == null || !EDITOR_ACTIONS_MAIN.reload()) {
			EDITOR_ACTIONS_MAIN = JYML.loadOrExtract(ENGINE, "/editor/actions_main.yml");
		}
		if (EDITOR_ACTIONS_SECTION == null || !EDITOR_ACTIONS_SECTION.reload()) {
			EDITOR_ACTIONS_SECTION = JYML.loadOrExtract(ENGINE, "/editor/actions_section.yml");
		}
		if (EDITOR_ACTIONS_PARAMETIZED == null || !EDITOR_ACTIONS_PARAMETIZED.reload()) {
			EDITOR_ACTIONS_PARAMETIZED = JYML.loadOrExtract(ENGINE, "/editor/actions_parametized.yml");
		}
		if (EDITOR_ACTIONS_PARAMS == null || !EDITOR_ACTIONS_PARAMS.reload()) {
			EDITOR_ACTIONS_PARAMS = JYML.loadOrExtract(ENGINE, "/editor/actions_params.yml");
		}
		
		actionsHandler = new EditorActionsHandler(ENGINE);
	}
	
	public static void shutdown() {
		if (actionsHandler != null) {
			actionsHandler.shutdown();
			actionsHandler = null;
		}
		EDITOR_ACTIONS_MAIN = null;
		EDITOR_ACTIONS_SECTION = null;
		EDITOR_ACTIONS_PARAMETIZED = null;
		EDITOR_ACTIONS_PARAMS = null;
	}
	
	public static void startEdit(@NotNull Player player, @Nullable Object o, Enum<?> type) {
		EDITOR_CACHE.put(player, new AbstractMap.SimpleEntry<>(type, o));
		ClickText text = new ClickText(ENGINE.lang().Core_Editor_Tips_Exit_Name.getMsg());
		text.createFullPlaceholder().execCmd(JStrings.EXIT).hint(ENGINE.lang().Core_Editor_Tips_Exit_Hint.getMsg());
		text.send(player);
	}

	public static void sendClickableTips(@NotNull Player player, @NotNull List<String> items) {
		StringBuilder builder = new StringBuilder();
		items.forEach(pz -> {
			if (builder.length() > 0) builder.append(" &7| ");
			builder.append("%" + pz + "%");
		});
		
		ClickText text = new ClickText(builder.toString());
		items.forEach(pz -> {
			ClickWord word = text.createPlaceholder("%" + pz + "%", "&a" + pz);
			word.hint(ENGINE.lang().Core_Editor_Tips_Hint.getMsg());
			word.execCmd(pz);
		});
		
		ENGINE.lang().Core_Editor_Tips_Header.send(player, false);
		text.send(player);
	}

	public static boolean isEdit(@NotNull Player player) {
		return getEditor(player) != null;
	}

	public static void endEdit(@NotNull Player player) {
		EditorManager.tip(player, ENGINE.lang().Core_Editor_Display_Done_Title.getMsg(), "", 40);
		EDITOR_CACHE.remove(player);
	}

	@Nullable
	public static Map.Entry<Enum<?>, Object> getEditor(@NotNull Player player) {
		return EDITOR_CACHE.getOrDefault(player, null);
	}

	public static void tip(@NotNull Player player, @NotNull String title, @NotNull String sub, int stay) {
		if (stay == 999) stay = 100000;
		
		ENGINE.lang().Core_Editor_Display_Edit_Format
			.replace("%title%", title)
			.replace("%message%", sub)
			.title(player, 10, stay, 20);
	}

	public static void tipCustom(@NotNull Player player, @NotNull String sub) {
		EditorManager.tip(player, ENGINE.lang().Core_Editor_Display_Edit_Title.getMsg(), sub, 999);
	}

	public static void errorNumber(@NotNull Player player, boolean mustDecimal) {
		String title = ENGINE.lang().Core_Editor_Display_Error_Number_Invalid.getMsg();
		String sub = ENGINE.lang().Core_Editor_Display_Error_Number_MustInteger.getMsg();
		if (mustDecimal) sub = ENGINE.lang().Core_Editor_Display_Error_Number_MustDecimal.getMsg();
		
		EditorManager.tip(player, title, sub, 999);
	}

	public static void errorCustom(@NotNull Player player, @NotNull String sub) {
		EditorManager.tip(player, ENGINE.lang().Core_Editor_Display_Error_Title.getMsg(), sub, 999);
	}

	public static void errorEnum(@NotNull Player player, @NotNull Class<?> clazz) {
		String title = ENGINE.lang().Core_Editor_Display_Error_Type_Title.getMsg();
		String sub = ENGINE.lang().Core_Editor_Display_Error_Type_Values.getMsg();
		EditorManager.tip(player, title, sub, 999);
		EditorManager.sendClickableTips(player, CollectionsUT.getEnumsList(clazz));
	}
}
