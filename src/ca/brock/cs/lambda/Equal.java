package ca.brock.cs.lambda;

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
    public void type(Map<String, Type> env) {
        left.type(env);
        right.type(env);

        Unifier unifier = new Unifier();
        Map<String, Type> sub = unifier.unify(left.getType(), right.getType());
        if (sub == null) throw new TypeError("Operands must have same type");

        type = new TVar("Bool");
    }

}
