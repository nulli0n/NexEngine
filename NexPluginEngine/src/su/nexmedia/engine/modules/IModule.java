package su.nexmedia.engine.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.core.config.CoreConfig;
import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.manager.api.Loggable;

public abstract class IModule<P extends NexPlugin<P>> extends IListener<P> implements Loggable {

    private String name;
    protected JYML cfg;
    private boolean isFailed;
    private boolean isLoaded;
    protected IModuleExecutor<P> moduleCommand;

    public IModule(@NotNull P plugin) {
        super(plugin);
        this.isLoaded = false;
        this.isFailed = false;
    }

    @Nullable
    public IModuleExecutor<P> getExecutor() {
        return this.moduleCommand;
    }

    public void load() {
        if (this.isLoaded())
            return;
        this.cfg = JYML.loadOrExtract(this.plugin, this.getPath() + "settings.yml");

        this.registerCommands();
        this.onPreSetup();
        this.setup();
        this.onPostSetup();
        this.registerListeners();

        if (this.isFailed) {
            this.unload();
            return;
        }
        this.isLoaded = true;
    }

    protected void onPostSetup() {

    }

    protected void onPreSetup() {

    }

    public void unload() {
        this.shutdown();
        this.unregisterCommands();
        this.unregisterListeners();
        this.onUnload();
        this.isLoaded = false;
        this.isFailed = false;
        this.cfg = null;
    }

    protected void onUnload() {

    }

    public void reload() {
        this.unload();
        this.load();
    }

    protected void onReload() {

    }

    protected final void interruptLoad() {
        if (this.isLoaded())
            return;
        this.isFailed = true;
    }

    public final boolean isLoaded() {
        return this.isLoaded;
    }

    @NotNull
    public abstract String getId();

    @NotNull
    public final String name() {
        if (this.name == null) {
            this.name = plugin.cfg().getModuleName(this);
        }
        return this.name;
    }

    @NotNull
    public abstract String version();

    public abstract void setup();

    public abstract void shutdown();

    public final boolean isEnabled() {
        return this.plugin.cfg().isModuleEnabled(this);
    }

    @NotNull
    public String getPath() {
        return CoreConfig.MODULES_PATH_INTERNAL + this.getId() + "/";
    }

    @NotNull
    public final String getFullPath() {
        return plugin.getDataFolder() + this.getPath();
    }

    @NotNull
    public final JYML getJYML() {
        return this.cfg;
    }

    private void registerCommands() {
        String alias = cfg.getString("command-aliases");
        if (alias == null)
            return;

        String[] aliases = alias.split(",");
        if (aliases.length == 0 || aliases[0].isEmpty())
            return;

        this.moduleCommand = new IModuleExecutor<P>(this, aliases);
        this.plugin.getCommandManager().registerCommand(this.moduleCommand);
    }

    private void unregisterCommands() {
        if (this.moduleCommand == null)
            return;

        this.plugin.getCommandManager().unregisterCommand(this.moduleCommand);
        this.moduleCommand = null;
    }

    @NotNull
    private final String buildLog(@NotNull String msg) {
        StringBuilder build = new StringBuilder();
        build.append("[").append(this.name()).append("] ").append(msg);

        return build.toString();
    }

    @Override
    public final void info(@NotNull String msg) {
        this.plugin.info(this.buildLog(msg));
    }

    @Override
    public final void warn(@NotNull String msg) {
        this.plugin.warn(this.buildLog(msg));
    }

    @Override
    public final void error(@NotNull String msg) {
        this.plugin.error(this.buildLog(msg));
    }
}
