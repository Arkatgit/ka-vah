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
    protected Type computeType(Map<String, Type> env) {
        condition.type(env);
        trueBranch.type(env);
        falseBranch.type(env);

        Type condType = condition.getType();
        Type thenType = trueBranch.getType();
        Type elseType = falseBranch.getType();

        if (!condType.equals(new ca.brock.ca.interpreter.Constant("Bool"))) {
            throw new RuntimeException("Condition must be boolean");
        }

        if (!thenType.equals(elseType)) {
            throw new RuntimeException("Branches must have same type");
        }

        return thenType;
    }

}
