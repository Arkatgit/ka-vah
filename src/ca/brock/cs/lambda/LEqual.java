package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Constant;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class LEqual extends Term{
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
    protected Type computeType(Map<String, Type> env) {
        left.type(env);
        right.type(env);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<String, Type> sub = new Unifier().unify(leftType, rightType);
        if (sub == null) {
            throw new RuntimeException("Comparison operands must have same type");
        }

        return new Constant("Bool");
    }
}
