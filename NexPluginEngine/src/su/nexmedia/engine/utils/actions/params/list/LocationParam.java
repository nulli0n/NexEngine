package su.nexmedia.engine.utils.actions.params.list;

import su.nexmedia.engine.utils.actions.params.IParamType;
import su.nexmedia.engine.utils.actions.params.defaults.IParamString;

public class LocationParam extends IParamString {

    public LocationParam() {
        super(IParamType.LOCATION, "location");
    }
}
