package su.nexmedia.engine.data;

import org.jetbrains.annotations.NotNull;

public enum StorageType {

    MYSQL("MySQL"), SQLITE("SQLite"),;

    private String name;

    private StorageType(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return this.name;
    }
}
