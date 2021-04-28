package su.nexmedia.engine.hooks;

import org.jetbrains.annotations.NotNull;

public enum HookState {

    SUCCESS("Success!"), ERROR("Error!"),;

    private String state;

    private HookState(@NotNull String state) {
        this.state = state;
    }

    @NotNull
    public String getName() {
        return this.state;
    }
}
