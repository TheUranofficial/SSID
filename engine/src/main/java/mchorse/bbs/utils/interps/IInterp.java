package mchorse.bbs.utils.interps;

public interface IInterp {
    InterpContext context = new InterpContext();

    default boolean has(IInterp interp) {
        return this == interp;
    }

    default float interpolate(float a, float b, float x) {
        return (float) this.interpolate(context.set(a, b, x));
    }

    default double interpolate(double a, double b, double x) {
        return this.interpolate(context.set(a, b, x));
    }

    double interpolate(InterpContext context);

    String getKey();

    int getKeyCode();
}