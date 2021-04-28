package su.nexmedia.engine;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.commands.CommandManager;
import su.nexmedia.engine.commands.api.IGeneralCommand;
import su.nexmedia.engine.commands.list.MainCommand;
import su.nexmedia.engine.config.ConfigManager;
import su.nexmedia.engine.config.api.IConfigTemplate;
import su.nexmedia.engine.core.config.CoreLang;
import su.nexmedia.engine.hooks.HookManager;
import su.nexmedia.engine.hooks.NHook;
import su.nexmedia.engine.hooks.external.MythicMobsHK;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.hooks.external.WorldGuardHK;
import su.nexmedia.engine.hooks.external.citizens.CitizensHK;
import su.nexmedia.engine.manager.api.Loggable;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorHandler;
import su.nexmedia.engine.modules.ModuleManager;
import su.nexmedia.engine.nms.NMS;
import su.nexmedia.engine.nms.packets.PacketManager;
import su.nexmedia.engine.utils.actions.ActionsManager;
import su.nexmedia.engine.utils.actions.Parametized;
import su.nexmedia.engine.utils.craft.CraftManager;

public abstract class NexPlugin<P extends NexPlugin<P>> extends JavaPlugin implements Loggable {

    public static final String TM = "NEX-Media";

    private Logger logger;
    private boolean isEngine;
    private boolean isSpigot = true;

    protected ConfigManager<P> configManager;
    protected CommandManager<P> cmdManager;
    protected ModuleManager<P> moduleManager;
    protected EditorHandler<P> editorHandler;

    public final boolean isEngine() {
        return this.isEngine;
    }

    public final boolean isSpigot() {
        return isSpigot;
    }

    @NotNull
    public static final NexEngine getEngine() {
        return NexEngine.get();
    }

    @Override
    public final void onEnable() {
        long loadTook = System.currentTimeMillis();
        this.logger = this.getLogger();
        this.isEngine = this instanceof NexEngine;

        if (!(this.isSpigot = this.getServer().getVersion().toLowerCase().contains("spigot"))) {
            this.warn("============== DISCLAIMER ==============");
            this.warn("> You're running an unknown Spigot fork.");
            this.warn("> This plugin is designed to work on Spigot (spigotmc.org) only!");
            this.warn("> You won't get any support for any issues unless they persist on regular Spigot.");
            this.warn("========================================");
        }

        NexEngine engine = getEngine();
        if (this.isEngine()) {
            if (!engine.loadCore()) {
                this.getPluginManager().disablePlugin(this);
                return;
            }
        }
        else {
            engine.hookChild(this);
            this.info("Powered by: " + engine.getName());
        }
        this.loadManagers();
        this.info("Plugin loaded in " + (System.currentTimeMillis() - loadTook) + " ms!");
    }

    @Override
    public final void onDisable() {
        this.unloadManagers();
    }

    public abstract void enable();

    public abstract void disable();

    public final void reload() {
        if (this.isEngine()) {
            this.setConfig();
            return;
        }
        this.unloadManagers();
        this.loadManagers();
    }

    public abstract void setConfig();

    public abstract void registerHooks();

    public abstract void registerCmds(@NotNull IGeneralCommand<P> mainCommand);

    public abstract void registerEditor();

    @NotNull
    public abstract IConfigTemplate cfg();

    @NotNull
    public abstract CoreLang lang();

    @Override
    public final void info(@NotNull String msg) {
        this.logger.info(msg);
    }

    @Override
    public final void warn(@NotNull String msg) {
        this.logger.warning(msg);
    }

    @Override
    public final void error(@NotNull String msg) {
        this.logger.severe(msg);
    }

    @Nullable
    public final <T extends NHook<P>> T registerHook(@NotNull String pluginName, @NotNull Class<T> clazz) {
        return this.getHooks().register(this, pluginName, clazz);
    }

    private final void unregisterListeners() {
        // Force close custom GUIs
        // To prevent take items after unregister listeners
        for (Player p : this.getServer().getOnlinePlayers()) {
            if (p != null) {
                InventoryHolder ih = p.getOpenInventory().getTopInventory().getHolder();
                if (ih instanceof NGUI) {
                    p.closeInventory();
                }
            }
        }
        HandlerList.unregisterAll(this);
    }

    @SuppressWarnings("unchecked")
    private final void loadManagers() {
        // Setup plugin Hooks.
        this.registerHooks();

        // Setup ConfigManager before any other managers.
        this.configManager = new ConfigManager<P>((P) this);
        this.configManager.setup();
        if (this.cfg().cmds == null || this.cfg().cmds.length == 0) {
            this.error("Could not register plugin commands!");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        // Connect to the database if present.
        NexDataPlugin<?, ?> dataPlugin = null;
        if (this instanceof NexDataPlugin) {
            dataPlugin = (NexDataPlugin<?, ?>) this;
            if (!dataPlugin.setupDataHandlers()) {
                this.error("Could not setup plugin Data Handler!");
                this.getPluginManager().disablePlugin(this);
                return;
            }
        }

        this.registerEditor();

        // Register plugin commands.
        this.cmdManager = new CommandManager<P>((P) this);
        this.cmdManager.setup();

        // Register plugin modules.
        this.moduleManager = new ModuleManager<P>((P) this);
        this.moduleManager.setup();

        // Custom plugin loaders.
        this.enable();

        // Load plugin users only when full plugin is loaded.
        if (dataPlugin != null) {
            dataPlugin.getUserManager().loadOnlineUsers();
        }

        Parametized.clearCache();
    }

    private final void unloadManagers() {
        this.getServer().getScheduler().cancelTasks(this); // First stop all plugin tasks

        if (this.moduleManager != null) {
            this.moduleManager.shutdown();
        }
        this.disable();
        if (this.cmdManager != null) {
            this.cmdManager.shutdown();
        }
        if (this.editorHandler != null) {
            this.editorHandler.shutdown();
        }

        // Unregister all plugin traits and NPC listeners.
        CitizensHK citizensHook = this.getCitizens();
        if (citizensHook != null) {
            citizensHook.unregisterTraits(this);
            citizensHook.unregisterListeners(this);
        }

        // Unregister all plugin hooks.
        if (!this.isEngine()) {
            this.getHooks().shutdown(this);
        }

        // Unregister ALL plugin listeners.
        this.unregisterListeners();

        // Save user data and disconnect from the database.
        if (this instanceof NexDataPlugin) {
            NexDataPlugin<?, ?> userData = (NexDataPlugin<?, ?>) this;
            userData.shutdownDataHandlers();
        }
    }

    @NotNull
    public final String getAuthor() {
        List<String> list = this.getDescription().getAuthors();
        return list.isEmpty() ? TM : list.get(0);
    }

    @NotNull
    public final String getNameRaw() {
        return this.getName().toLowerCase().replace(" ", "").replace("-", "");
    }

    @NotNull
    public final String getLabel() {
        return this.getLabels()[0];
    }

    @NotNull
    public final String[] getLabels() {
        return this.cfg().cmds;
    }

    @NotNull
    public final NMS getNMS() {
        return getEngine().nms;
    }

    @NotNull
    public final MainCommand<P> getMainCommand() {
        return this.getCommandManager().getMainCommand();
    }

    @NotNull
    public final ConfigManager<P> getConfigManager() {
        return this.configManager;
    }

    @NotNull
    public final CommandManager<P> getCommandManager() {
        return this.cmdManager;
    }

    @NotNull
    public final CraftManager getCraftManager() {
        return getEngine().craftManager;
    }

    @NotNull
    public final ModuleManager<P> getModuleManager() {
        return this.moduleManager;
    }

    @NotNull
    public final ActionsManager getActionsManager() {
        return getEngine().actionsManager;
    }

    @NotNull
    public final PacketManager getPacketManager() {
        return getEngine().packetManager;
    }

    @NotNull
    public final PluginManager getPluginManager() {
        return getEngine().pluginManager;
    }

    @NotNull
    public final HookManager getHooks() {
        return getEngine().getHookManager();
    }

    public final boolean isHooked(@NotNull Class<? extends NHook<?>> clazz) {
        return this.getHooks().isHooked(this, clazz);
    }

    public final boolean isHooked(@NotNull String plugin) {
        return this.getHooks().isHooked(this, plugin);
    }

    @Nullable
    public final <T extends NHook<?>> T getHook(@NotNull Class<T> clazz) {
        return this.getHooks().getHook(this, clazz);
    }

    @Nullable
    public final NHook<? extends NexPlugin<?>> getHook(@NotNull String name) {
        return this.getHooks().getHook(this, name);
    }

    @Nullable
    public final VaultHK getVault() {
        return getEngine().hookVault;
    }

    @Nullable
    public final CitizensHK getCitizens() {
        return getEngine().hookCitizens;
    }

    @Nullable
    public final WorldGuardHK getWorldGuard() {
        return getEngine().hookWorldGuard;
    }

    @Nullable
    public final MythicMobsHK getMythicMobs() {
        return getEngine().hookMythicMobs;
    }

    public boolean hasEditor() {
        return this.editorHandler != null;
    }

    public void openEditor(@NotNull Player p) {
        if (!this.hasEditor()) {
            throw new IllegalStateException("This plugin does not provides GUI Editor!");
        }
        this.editorHandler.open(p, 1);
    }

    @Nullable
    public EditorHandler<P> getEditorHandler() {
        return this.editorHandler;
    }

    public ClassLoader getClazzLoader() {
        return this.getClassLoader();
    }

    public final void runTask(@NotNull Consumer<BukkitTask> consume, boolean async) {
        if (async) {
            this.getServer().getScheduler().runTaskAsynchronously(this, consume);
        }
        else {
            this.getServer().getScheduler().runTask(this, consume);
        }
    }
}
