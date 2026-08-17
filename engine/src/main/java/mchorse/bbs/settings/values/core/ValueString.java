package mchorse.bbs.settings.values.core;

import mchorse.bbs.settings.values.base.BaseKeyframeFactoryValue;
import mchorse.bbs.utils.keyframes.factories.KeyframeFactories;

public class ValueString extends BaseKeyframeFactoryValue<String> {
    public ValueString(String id, String defaultValue) {
        super(id, KeyframeFactories.STRING, defaultValue);
    }

    @Override
    public String toString() {
        return this.value;
    }
}