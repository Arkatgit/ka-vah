package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class Addition extends Term {
    private Term left;
    private Term right;
    public static final int precedence = 10;

    public Term getLeft() {
        return left;
    }

    public Term getRight() {
        return right;
    }

    public Addition(Term l, Term r){
        left = l ;
        right = r;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return left.toStringPrec(prec) + " + " + right.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env) {
        left.type(env);
        right.type(env);

        Type leftType = left.getType();
        Type rightType = right.getType();

        if (!leftType.equals(new ca.brock.ca.interpreter.Constant("Int")) || !rightType.equals( new ca.brock.ca.interpreter.Constant("Int"))) {
            throw new RuntimeException("Operands must be integers");
        }

        return new ca.brock.ca.interpreter.Constant("Int");
    }

}
