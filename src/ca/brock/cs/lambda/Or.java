package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class Or extends Term {
    private Term left;
    private Term right;
    public static final int precedence = 5;
    public Or(Term l, Term r)
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
        return left.toStringPrec(prec) + " or " + right.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env) {
        // Type check both operands
        left.type(env);
        right.type(env);

        Type leftType = left.getType();
        Type rightType = right.getType();

        // Both operands must be boolean
        if (!leftType.equals(new Constant("Bool")) || !rightType.equals(new Constant("Bool"))) {
            throw new RuntimeException("OR operands must be boolean (found " + leftType + " and " + rightType + ")");
        }

        return new ca.brock.ca.interpreter.Constant("Bool");
    }

}
