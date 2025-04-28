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
    public void type(Map<String, Type> env) {
        operand.type(env);

        Unifier unifier = new Unifier();
        Map<String, Type> sub = unifier.unify(operand.getType(), new TVar("Bool"));
        if (sub == null) throw new TypeError("Operand must be Bool");

        type = new TVar("Bool");
    }

}
