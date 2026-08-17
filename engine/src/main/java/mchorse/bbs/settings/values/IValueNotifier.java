package mchorse.bbs.settings.values;

import mchorse.bbs.settings.values.base.BaseValue;

public interface IValueNotifier {
    default void preNotify() {
        this.preNotify(IValueListener.FLAG_DEFAULT);
    }

    void preNotify(int flag);

    default void preNotify(BaseValue value, int flag) {
        if (this.getParent() != null) {
            this.getParent().preNotify(value, flag);
        }
    }

    default void postNotify() {
        this.postNotify(IValueListener.FLAG_DEFAULT);
    }

    void postNotify(int flag);

    default void postNotify(BaseValue value, int flag) {
        if (this.getParent() != null) {
            this.getParent().postNotify(value, flag);
        }
    }

    IValueNotifier getParent();
}