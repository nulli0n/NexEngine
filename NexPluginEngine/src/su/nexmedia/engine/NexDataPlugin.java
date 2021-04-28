package su.nexmedia.engine;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.data.IDataHandler;
import su.nexmedia.engine.data.users.IAbstractUser;
import su.nexmedia.engine.data.users.IUserManager;

public abstract class NexDataPlugin<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends NexPlugin<P> {

    protected IUserManager<P, U> userManager;

    protected abstract boolean setupDataHandlers();

    protected void shutdownDataHandlers() {
        if (this.userManager != null)
            this.userManager.shutdown();

        IDataHandler<P, U> dataHandler = this.getData();
        if (dataHandler != null)
            dataHandler.shutdown();
    }

    public abstract IDataHandler<P, U> getData();

    @NotNull
    public IUserManager<P, U> getUserManager() {
        return this.userManager;
    }
}
