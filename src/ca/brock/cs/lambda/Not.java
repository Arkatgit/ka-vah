package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

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

        // Operand must be boolean
        if (!operandType.equals(new ca.brock.ca.interpreter.Constant("Bool"))) {
            throw new RuntimeException("NOT operand must be boolean (found " + operandType + ")");
        }

        return new ca.brock.ca.interpreter.Constant("Bool");
    }

}
