package mchorse.bbs.settings.values.core;

import mchorse.bbs.resources.Link;
import mchorse.bbs.settings.values.base.BaseKeyframeFactoryValue;
import mchorse.bbs.utils.keyframes.factories.KeyframeFactories;

public class ValueLink extends BaseKeyframeFactoryValue<Link> {
    public ValueLink(String id, Link defaultValue) {
        super(id, KeyframeFactories.LINK, defaultValue);
    }

    @Override
    public String toString() {
        return this.value == null ? "" : this.value.toString();
    }
}