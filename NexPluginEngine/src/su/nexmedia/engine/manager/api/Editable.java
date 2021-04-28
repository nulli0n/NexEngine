package su.nexmedia.engine.manager.api;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.api.gui.NGUI;

public interface Editable {

    @NotNull
    public NGUI<?> getEditor();
}
