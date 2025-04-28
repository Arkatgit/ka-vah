package ca.brock.cs.lambda;

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
    public void type(Map<String, Type> env) {
        left.type(env);
        right.type(env);

        Unifier unifier = new Unifier();
        Map<String, Type> sub1 = unifier.unify(left.getType(), new TVar("Int"));
        if (sub1 == null) throw new TypeError("Left operand must be Int");

        Type rightType = Unifier.applySubstitution(right.getType(), sub1);
        Map<String, Type> sub2 = unifier.unify(rightType, new TVar("Int"));
        if (sub2 == null) throw new TypeError("Right operand must be Int");

        type = new TVar("Bool");
    }
}
