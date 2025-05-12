package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.parser.Term;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Map;

public class Not extends Term {
    private Term operand;
    public static final int precedence = 4;
    public Not(Term op){
        operand = op;
    }

    public Term getOperand() {
        return operand;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return  " not " + operand.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        // Type check the operand, passing down the Unifier
        operand.type(env, unifier);
        Type operandType = operand.getType();

        Map<TVar, Type> substitution = unifier.unify(operandType , new Constant("Bool"));
        if (substitution == null)
        {
            throw new TypeError("NOT operand must be boolean (found " + operandType + ")");
        }

        return new Constant("Bool");
    }

}
