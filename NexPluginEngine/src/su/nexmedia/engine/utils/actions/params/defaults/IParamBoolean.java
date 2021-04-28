package su.nexmedia.engine.utils.actions.params.defaults;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.actions.params.IParam;
import su.nexmedia.engine.utils.actions.params.parser.IParamParser;

public class IParamBoolean extends IParam {

    public IParamBoolean(@NotNull String key, @NotNull String flag) {
        super(key, flag);
    }

    @Override
    @NotNull
    public final IParamParser getParser() {
        return IParamParser.BOOLEAN;
    }
}
