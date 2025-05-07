package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class And extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 5;
    public And(Term l, Term r)
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
        return left.toStringPrec(prec) + " and " + right.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env) {
        left.type(env);
        right.type(env);

        Type leftType = left.getType();
        Type rightType = right.getType();

        if (!leftType.equals(new ca.brock.ca.interpreter.Constant("Bool")) || !rightType.equals(new ca.brock.ca.interpreter.Constant("Bool"))) {
            throw new RuntimeException("AND operands must be boolean");
        }

        return new ca.brock.ca.interpreter.Constant("Bool");
    }

}
