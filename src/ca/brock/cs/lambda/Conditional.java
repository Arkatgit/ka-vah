package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class Conditional extends Term {
    private Term condition;
    private Term trueBranch;
    private Term  falseBranch;

    public Conditional(Term c, Term t, Term f)
    {
        condition = c ;
        trueBranch = t;
        falseBranch = f;
    }

    public Term getCondition() {
        return condition;
    }
    public Term getTrueBranch(){
        return trueBranch;
    }
    public Term getFalseBranch()
    {
        return falseBranch;
    }
    @Override
    public String toStringPrec(int prec)
    {
        return  "if " + condition.toStringPrec(prec) + " then " + trueBranch.toStringPrec(prec) +
            "else " +  falseBranch.toStringPrec(prec);
    }
    @Override
    public void type(Map<String, Type> env) {
        condition.type(env);
        trueBranch.type(env);
        falseBranch.type(env);

        Unifier unifier = new Unifier();
        Map<String, Type> sub1 = unifier.unify(condition.getType(), new TVar("Bool"));
        if (sub1 == null) throw new TypeError("Condition must be Bool");

        Type trueType = Unifier.applySubstitution(trueBranch.getType(), sub1);
        Type falseType = Unifier.applySubstitution(falseBranch.getType(), sub1);

        Map<String, Type> sub2 = unifier.unify(trueType, falseType);
        if (sub2 == null) throw new TypeError("Branches must have same type");

        type = Unifier.applySubstitution(trueType, sub2);
    }

}
