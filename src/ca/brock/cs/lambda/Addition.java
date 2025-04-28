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
    public void type(Map<String, Type> env) {
        // First type check both operands
        left.type(env);
        right.type(env);

        // Create a Unifier instance
        Unifier unifier = new Unifier();

        // Unify left operand with Int
        Map<String, Type> substitution = unifier.unify(left.getType(), new TVar("Int"));

        if (substitution == null) {
            throw new TypeError("Left operand of addition must be Int");
        }

        // Apply the substitution to the right operand's type
        Type rightType = Unifier.applySubstitution(right.getType(), substitution);

        // Unify right operand with Int
        Map<String, Type> sub2 = unifier.unify(rightType, new TVar("Int"));

        if (sub2 == null) {
            throw new TypeError("Right operand of addition must be Int");
        }

        // Combine substitutions
        substitution.putAll(sub2);

        // Set the result type to Int
        type = new TVar("Int");
    }

}
