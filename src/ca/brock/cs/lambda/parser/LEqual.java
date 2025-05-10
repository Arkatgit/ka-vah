package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Map;

public class LEqual extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 10;

    public LEqual(Term l, Term r)
    {
        left = l;
        right = r;
    }

    public Term getLeft() {
        return left;
    }

    public Term getRight() {
        return right;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return left.toStringPrec(prec) + " <= " + right.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        left.type(env, unifier);
        right.type(env, unifier);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<TVar, Type> sub = unifier.unify(leftType, rightType);
        if (sub == null) {
            throw new RuntimeException("Comparison operands must have same type");
        }

        return new Constant("Bool");
    }
}
