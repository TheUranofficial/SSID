package mchorse.bbs.settings.values;

import mchorse.bbs.settings.values.base.BaseValue;

public interface IValueListener {
    int FLAG_DEFAULT = 0b0;
    int FLAG_UNMERGEABLE = 0b1;

    void accept(BaseValue value, int flag);
}