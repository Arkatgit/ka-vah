package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.FType;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class Constant extends Term {
    private String value;
    private final InfixOperator operator;

    // Constructor for normal constants
    public Constant(String value) {
        this.value = value;
        this.operator = InfixOperator.fromSymbol(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toStringPrec(int prec) {
        if (operator != null) {
            return operator.getSymbol(); // Use operator symbol if it's an infix operator
        }
        return value;
    }

    public boolean isInfixOperator() {
        return operator != null;
    }

    public boolean canBeUsedAsSection() {
        return isInfixOperator() && operator.canBeUsedAsSection();
    }

    public int getArity() {
        return isInfixOperator() ? operator.getArity() : 0;
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        if (value.equals("True") || value.equals("False")) {
            return new ca.brock.ca.interpreter.Constant("Bool");
        }

        switch (value) {
            case "+":
            case "-":
            case "*":
                return new FType(new ca.brock.ca.interpreter.Constant("Int"), new FType(new ca.brock.ca.interpreter.Constant("Int"), new ca.brock.ca.interpreter.Constant("Int")));
            case "and":
            case "or":
                return new FType(new ca.brock.ca.interpreter.Constant("Bool"), new FType(new ca.brock.ca.interpreter.Constant("Bool"), new ca.brock.ca.interpreter.Constant("Bool")));
            case "=":
            case "<=":
                TVar a = TVar.fresh();
                return new FType(a, new FType(a, new ca.brock.ca.interpreter.Constant("Bool")));
            case "not":
                return new FType(new ca.brock.ca.interpreter.Constant("Bool"), new ca.brock.ca.interpreter.Constant("Bool"));
            default:
                return TVar.fresh();
        }
    }

}