package mchorse.bbs.camera.values;

import mchorse.bbs.camera.data.Point;
import mchorse.bbs.data.types.BaseType;
import mchorse.bbs.settings.values.base.BaseValueBasic;

public class ValuePoint extends BaseValueBasic<Point> {
    public ValuePoint(String id, Point point) {
        super(id, point);
    }

    @Override
    public void set(Point value) {
        this.preNotify();
        this.value.set(value);
        this.postNotify();
    }

    @Override
    public BaseType toData() {
        return this.value.toData();
    }

    @Override
    public void fromData(BaseType data) {
        this.value.fromData(data.asMap());
    }
}