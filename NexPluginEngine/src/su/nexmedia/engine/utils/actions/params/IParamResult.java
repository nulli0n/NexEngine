package su.nexmedia.engine.utils.actions.params;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class IParamResult {

    private Map<String, IParamValue> values;
    private static final IParamValue EMPTY_PARAM = new IParamValue();

    public IParamResult(@NotNull Map<String, IParamValue> values) {
        this.values = values;
    }

    @NotNull
    public IParamValue getParamValue(@NotNull String key) {
        return this.values.getOrDefault(key.toUpperCase(), EMPTY_PARAM);
    }

    public boolean hasParam(@NotNull String key) {
        return this.values.containsKey(key.toUpperCase());
    }
}
