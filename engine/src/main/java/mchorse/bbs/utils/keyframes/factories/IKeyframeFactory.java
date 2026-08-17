package mchorse.bbs.utils.keyframes.factories;

import mchorse.bbs.data.types.BaseType;
import mchorse.bbs.utils.interps.IInterp;
import mchorse.bbs.utils.keyframes.Keyframe;

import java.util.Objects;

public interface IKeyframeFactory<T> {
    T fromData(BaseType data);

    BaseType toData(T value);

    T createEmpty();

    default boolean compare(Object a, Object b) {
        return Objects.equals(a, b);
    }

    T copy(T value);

    default T interpolate(Keyframe<T> preA, Keyframe<T> a, Keyframe<T> b, Keyframe<T> postB, IInterp interpolation, float x) {
        return this.interpolate(preA.getValue(), a.getValue(), b.getValue(), postB.getValue(), interpolation, x);
    }

    T interpolate(T preA, T a, T b, T postB, IInterp interpolation, float x);

    default double getY(T value) {
        return 0D;
    }

    default Object yToValue(double y) {
        return y;
    }
}