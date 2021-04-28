package su.nexmedia.engine.utils.actions.params.parser;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.actions.params.IParamValue;
import su.nexmedia.engine.utils.actions.params.IParamValue.IOperator;

public class ParamNumberParser implements IParamParser {

    @Override
    @NotNull
    public IParamValue parseValue(@NotNull String str) {
        boolean perc = str.contains("%");
        IOperator oper = IOperator.parse(str);

        str = IOperator.clean(str);
        double amount = StringUT.getDouble(str.replace("%", ""), 0D, true);

        IParamValue val = new IParamValue((int) amount);
        val.setBoolean(perc);
        val.setDouble(amount);
        val.setOperator(oper);
        if (perc)
            val.setRaw(val.getRaw() + "%");

        return val;
    }
}
