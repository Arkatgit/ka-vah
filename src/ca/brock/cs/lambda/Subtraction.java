package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class Subtraction extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 20;

    public Subtraction(Term l, Term r)
    {
        left = l;
        right  = r;
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
        return left.toStringPrec(prec) + " - " + right.toStringPrec(prec);
    }


    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        left.type(env, unifier);
        right.type(env, unifier);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<TVar, Type> substitution = unifier.unify(leftType , new ca.brock.ca.interpreter.Constant("Int"));
        if (substitution == null)
        {
            throw new TypeError("Left operand of subtraction must be an integer, but got: " + leftType);
        }

        substitution = unifier.unify( rightType , new ca.brock.ca.interpreter.Constant("Int"));

        if (substitution == null)
        {
            throw new TypeError("Right operand of subtraction must be an integer, but got: " + leftType);
        }

        return new ca.brock.ca.interpreter.Constant("Int");
    }

}
