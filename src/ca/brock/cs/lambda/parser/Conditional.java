package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

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
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        condition.type(env, unifier);
        trueBranch.type(env, unifier);
        falseBranch.type(env, unifier);

        Type condType = condition.getType();
        Type thenType = trueBranch.getType();
        Type elseType = falseBranch.getType();

        if (!condType.equals(new Constant("Bool"))) {
            throw new RuntimeException("Condition must be boolean");
        }

        if (!thenType.equals(elseType)) {
            throw new RuntimeException("Branches must have same type");
        }

        return thenType;
    }

}
