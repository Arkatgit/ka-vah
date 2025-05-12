package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.parser.Term;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

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
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        return new Constant("Int");
    }
}
