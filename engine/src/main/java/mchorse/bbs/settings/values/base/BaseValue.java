package mchorse.bbs.settings.values.base;

import mchorse.bbs.data.IDataSerializable;
import mchorse.bbs.data.types.BaseType;
import mchorse.bbs.settings.values.IValueListener;
import mchorse.bbs.settings.values.IValueNotifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class BaseValue implements IDataSerializable<BaseType>, IValueNotifier {
    protected String id;
    protected IValueNotifier parent;

    private boolean visible = true;
    private List<IValueListener> preCallbacks;
    private List<IValueListener> postCallbacks;

    public static <T extends BaseValue> void edit(T value, Consumer<T> callback) {
        if (callback == null) {
            return;
        }

        value.preNotify();
        callback.accept(value);
        value.postNotify();
    }

    public BaseValue(String id) {
        this.setId(id);
    }

    /**
     * Don't use it without a reason!
     */
    public void setId(String id) {
        this.id = id;
    }

    public BaseValue invisible() {
        this.visible = false;

        return this;
    }

    public BaseValue preCallback(IValueListener callback) {
        if (this.preCallbacks == null) {
            this.preCallbacks = new ArrayList<>();
        }

        this.preCallbacks.add(callback);

        return this;
    }

    public BaseValue postCallback(IValueListener callback) {
        if (this.postCallbacks == null) {
            this.postCallbacks = new ArrayList<>();
        }

        this.postCallbacks.add(callback);

        return this;
    }

    public boolean isVisible() {
        boolean visible = true;
        BaseValue value = this;

        while (value != null) {
            visible = visible && value.visible;
            value = value.getParentValue();
        }

        return visible;
    }

    public BaseValue getRoot() {
        BaseValue value = this;

        while (true) {
            if (value.getParent() == null) {
                return value;
            }

            value = value.getParentValue();
        }
    }

    public void setParent(BaseValue parent) {
        this.parent = parent;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public void preNotify(int flag) {
        this.preNotify(this, flag);
    }

    @Override
    public void preNotify(BaseValue value, int flag) {
        IValueNotifier.super.preNotify(value, flag);

        if (this.preCallbacks != null) {
            for (IValueListener callback : this.preCallbacks) {
                callback.accept(value, flag);
            }
        }
    }

    @Override
    public void postNotify(int flag) {
        this.postNotify(this, flag);
    }

    @Override
    public void postNotify(BaseValue value, int flag) {
        IValueNotifier.super.postNotify(value, flag);

        if (this.postCallbacks != null) {
            for (IValueListener callback : this.postCallbacks) {
                callback.accept(value, flag);
            }
        }
    }

    public IValueNotifier getParent() {
        return this.parent;
    }

    public BaseValue getParentValue() {
        return this.parent instanceof BaseValue ? (BaseValue) this.parent : null;
    }

    public List<String> getPathSegments() {
        List<String> strings = new ArrayList<>();
        BaseValue value = this;

        while (value != null) {
            String id = value.getId();

            if (!id.isEmpty()) {
                strings.add(id);
            }

            value = value.getParentValue();
        }

        Collections.reverse(strings);

        return strings;
    }

    public String getPath() {
        return String.join(".", this.getPathSegments());
    }

    public String getRelativePath(BaseValue ancestor) {
        List<String> strings = new ArrayList<>();
        BaseValue value = this;

        while (value != null) {
            String id = value.getId();

            if (!id.isEmpty()) {
                strings.add(id);
            }

            value = value.getParentValue();

            if (value == ancestor) {
                strings.add(value.getId());

                Collections.reverse(strings);

                return String.join(".", strings);
            }
        }

        return null;
    }

    public void copy(BaseValue value) {
        this.fromData(value.toData());
    }
}