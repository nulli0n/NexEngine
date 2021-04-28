package su.nexmedia.engine.utils.actions.params.parser;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.params.IParamValue;

public class ParamStringParser implements IParamParser {

    @Override
    @NotNull
    public IParamValue parseValue(@NotNull String str) {
        return new IParamValue(StringUT.color(str));
    }
}
