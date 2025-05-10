package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

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
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        // Type check both operands
        left.type(env, unifier);
        right.type(env, unifier);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<TVar, Type> substitution = unifier.unify(leftType , new Constant("Bool"));
        if (substitution == null)
        {
            throw new TypeError("Left operand of OR must be an boolean, but got: " + leftType);
        }

        substitution = unifier.unify( rightType , new Constant("Bool"));

        if (substitution == null)
        {
            throw new TypeError("Right operand of OR must be an boolean, but got: " + rightType);
        }

        return new Constant("Bool");
    }

}
