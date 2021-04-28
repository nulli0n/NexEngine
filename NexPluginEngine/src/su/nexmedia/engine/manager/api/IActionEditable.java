package su.nexmedia.engine.manager.api;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.manager.editor.object.IEditorActionsMain;
import su.nexmedia.engine.utils.actions.api.IActioned;

public interface IActionEditable extends IActioned, Editable {

    @NotNull
    public IEditorActionsMain<? extends NexPlugin<?>> getEditorActions();

    @NotNull
    public String getActionsPath();
}
