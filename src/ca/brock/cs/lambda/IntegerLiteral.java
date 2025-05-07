package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Constant;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;

import java.util.Map;

public class IntegerLiteral extends Term {
    private int value;

    public IntegerLiteral(int v)
    {
        value = v;
    }

    @Override
    public String toStringPrec(int prec) {
        return Integer.toString(value);
    }
    @Override
    protected Type computeType(Map<String, Type> env) {
        return new Constant("Int");
    }
}
