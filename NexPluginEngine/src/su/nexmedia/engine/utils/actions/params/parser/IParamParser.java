package su.nexmedia.engine.utils.actions.params.parser;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.actions.params.IParamValue;

public interface IParamParser {

    public static final IParamParser BOOLEAN = new ParamBooleanParser();
    public static final IParamParser NUMBER = new ParamNumberParser();
    public static final IParamParser STRING = new ParamStringParser();

    @NotNull
    public IParamValue parseValue(@NotNull String str);
}
