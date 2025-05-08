package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Constant;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class Equal extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 10;

    public Equal(Term l, Term r)
    {
        left = l;
        right = r;
    }

    public Term getLeft(){
        return left;
    }
    public Term getRight(){
        return right;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return left.toStringPrec(prec) + " = " + right.toStringPrec(prec);
    }
    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        left.type(env, unifier);
        right.type(env, unifier);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<TVar, Type> sub = unifier.unify(leftType, rightType); // Use the provided unifier
        if (sub == null) {
            throw new RuntimeException("Comparison operands must have same type");
        }

        return new ca.brock.ca.interpreter.Constant("Bool");
    }
}
