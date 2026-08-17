package mchorse.bbs.math.functions.utility;

import mchorse.bbs.math.IExpression;
import mchorse.bbs.math.functions.NNFunction;
import mchorse.bbs.utils.interps.Lerps;

public class LerpRotate extends NNFunction {
    public LerpRotate(IExpression[] expressions, String name) throws Exception {
        super(expressions, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double doubleValue() {
        return Lerps.lerpYaw(this.getArg(0).doubleValue(), this.getArg(1).doubleValue(), this.getArg(2).doubleValue());
    }
}