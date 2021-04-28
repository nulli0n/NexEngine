package su.nexmedia.engine.manager.api;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;

public interface Saveable {

    public void save(@NotNull JYML cfg);
}
