package su.nexmedia.engine.utils.actions.params;

import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.actions.params.parser.IParamParser;

public abstract class IParam {

    protected final String key;
    protected final String flag;
    protected final Pattern pattern;

    public IParam(@NotNull String key, @NotNull String flag) {
        this.key = key.toUpperCase();
        this.flag = flag.toLowerCase();
        this.pattern = Pattern.compile("(~)+(" + this.getFlag() + ")+?(:)+(.*?)(;)");
    }

    @NotNull
    public final String getKey() {
        return this.key;
    }

    @NotNull
    public final Pattern getPattern() {
        return this.pattern;
    }

    @NotNull
    public final String getFlag() {
        return this.flag;
    }

    @NotNull
    public abstract IParamParser getParser();
}
