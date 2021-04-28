package su.nexmedia.engine.manager.editor;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.manager.editor.object.IEditorActionsMain;
import su.nexmedia.engine.manager.editor.object.IEditorActionsParametized;
import su.nexmedia.engine.manager.editor.object.IEditorActionsParams;
import su.nexmedia.engine.manager.editor.object.IEditorActionsSection;
import su.nexmedia.engine.manager.editor.object.IEditorActionsMain.ActionBuilder;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.ActionCategory;
import su.nexmedia.engine.utils.actions.ActionSection;
import su.nexmedia.engine.utils.actions.Parametized;
import su.nexmedia.engine.utils.actions.params.IParam;

public class EditorActionsHandler extends EditorHandler<NexEngine> {

    public EditorActionsHandler(@NotNull NexEngine plugin) {
        super(plugin, EditorType.class, null);
    }

    @Override
    protected boolean onType(@NotNull Player p, @Nullable Object editObject, @NotNull Enum<?> type,
            @NotNull String msg) {

        if (type == EditorType.OBJECT_ACTIONS_PARAM_VALUE || type == EditorType.OBJECT_ACTIONS_PARAM_ADD) {
            if (editObject == null)
                return false;
            return this.onTypeParam(p, editObject, (EditorType) type, msg);
        }
        if (type == EditorType.OBJECT_ACTIONS_SECTION_ADD) {
            if (editObject == null)
                return false;

            String sectionId = StringUT.colorOff(msg.toLowerCase());
            IEditorActionsMain<?> editor = (IEditorActionsMain<?>) editObject;
            if (editor.getActionBuilders().containsKey(sectionId))
                return false;

            ActionSection section = new ActionSection(new ArrayList<>(), new ArrayList<>(), "null", new ArrayList<>());
            ActionBuilder builder = new ActionBuilder(section, sectionId);

            editor.getActionBuilders().put(sectionId, builder);
            editor.open(p, 1);
            editor.save();
            return true;
        }
        if (type == EditorType.OBJECT_ACTIONS_PARAMETIZED_ADD) {
            if (editObject == null)
                return false;

            IEditorActionsParametized<?> editor = (IEditorActionsParametized<?>) editObject;
            String pzId = msg;
            ActionCategory category = editor.getSectionType();
            Parametized pz = plugin.getActionsManager().getParametized(category, pzId);
            if (pz == null) {
                EditorManager.errorCustom(p, plugin.lang().Core_Editor_Actions_Subject_Invalid.getMsg());
                return false;
            }

            // Check for valid Builder.
            ActionBuilder builder = editor.getSectionEditor().getEditorMain()
                    .getActionBuilder(editor.getSectionEditor().getSectionId());
            if (builder == null) {
                EditorManager.errorCustom(p, plugin.lang().Error_Internal.getMsg());
                return false;
            }

            builder.addParametized(pz, category);
            editor.open(p, 1);
            editor.getSectionEditor().getEditorMain().save();
            return true;
        }

        return true;
    }

    private boolean onTypeParam(@NotNull Player p, @NotNull Object editObject, @NotNull EditorType type,
            @NotNull String msg) {

        IEditorActionsParams<?> paramEditor = (IEditorActionsParams<?>) editObject;
        IEditorActionsSection<?> sectionEditor = paramEditor.getSctionEditor();
        ActionBuilder builder = sectionEditor.getEditorMain().getActionBuilder(sectionEditor.getSectionId());
        if (builder == null) {
            EditorManager.errorCustom(p, plugin.lang().Error_Internal.getMsg());
            return false;
        }

        int pId = paramEditor.getpId();

        if (type == EditorType.OBJECT_ACTIONS_PARAM_VALUE) {
            builder.addParametizedParam(pId, paramEditor.getCategory(), paramEditor.getParametized(),
                    paramEditor.getClickedParam(), msg);
        }
        else if (type == EditorType.OBJECT_ACTIONS_PARAM_ADD) {
            String[] split = msg.split(" ");
            String param = split[0];
            String value = split.length >= 2 ? split[1] : "null";

            IParam param2 = plugin.getActionsManager().getParam(param);
            if (param2 == null) {
                EditorManager.errorCustom(p, plugin.lang().Core_Editor_Actions_Param_Invalid.getMsg());
                return false;
            }

            builder.addParametizedParam(pId, paramEditor.getCategory(), paramEditor.getParametized(), param2.getFlag(),
                    value);
        }

        paramEditor.open(p, 1);
        paramEditor.getSctionEditor().getEditorMain().save();
        return true;
    }
}
