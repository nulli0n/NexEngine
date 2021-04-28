package su.nexmedia.engine.manager.api;

import org.jetbrains.annotations.NotNull;

public interface Loggable {

    public void info(@NotNull String msg);

    public void warn(@NotNull String msg);

    public void error(@NotNull String msg);
}
