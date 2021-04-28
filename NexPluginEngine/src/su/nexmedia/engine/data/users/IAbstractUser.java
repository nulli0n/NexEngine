package su.nexmedia.engine.data.users;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;

public abstract class IAbstractUser<P extends NexPlugin<P>> {

    @NotNull
    protected transient final P plugin;
    protected final UUID uuid;
    protected String name;
    protected long lastOnline;

    // Create new user data
    public IAbstractUser(@NotNull P plugin, @NotNull Player player) {
        this(plugin, player.getUniqueId(), player.getName(), System.currentTimeMillis());
    }

    // Load existent user data
    public IAbstractUser(@NotNull P plugin, @NotNull UUID uuid, @NotNull String name, long lastOnline) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.setName(name);
        this.setLastOnline(lastOnline);
    }

    @NotNull
    public final UUID getUUID() {
        return this.uuid;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    /**
     * Update stored user names to their mojang names.
     * 
     * @param name stored user name.
     */
    public void setName(@NotNull String name) {
        OfflinePlayer offlinePlayer = this.getOfflinePlayer();
        if (offlinePlayer != null) {
            String nameHas = offlinePlayer.getName();
            if (nameHas != null)
                name = nameHas;
        }
        this.name = name;
    }

    public final long getLastOnline() {
        return this.lastOnline;
    }

    public final void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public final boolean isOnline() {
        return this.getPlayer() != null;
    }

    @Nullable
    public final OfflinePlayer getOfflinePlayer() {
        return this.plugin.getServer().getOfflinePlayer(this.getUUID());
    }

    @Nullable
    public final Player getPlayer() {
        return this.plugin.getServer().getPlayer(this.getUUID());
    }

    @Override
    public String toString() {
        return "IAbstractUser [uuid=" + this.uuid + ", name=" + this.name + ", lastOnline=" + this.lastOnline + "]";
    }
}
