package su.nexmedia.engine.utils.actions.api;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.actions.ActionManipulator;

public interface IActioned {

    @NotNull
    public ActionManipulator getActions();
}
