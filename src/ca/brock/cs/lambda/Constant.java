package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.FType;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;

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
    public void type(Map<String, Type> env) {
        if (operator != null) {
            switch (operator.getSymbol()) {
                case "+": case "-": case "*":
                    type = new FType(new TVar("Int"),
                        new FType(new TVar("Int"), new TVar("Int")));
                    break;
                case "and": case "or":
                    type = new FType(new TVar("Bool"),
                        new FType(new TVar("Bool"), new TVar("Bool")));
                    break;
                case "not":
                    type = new FType(new TVar("Bool"), new TVar("Bool"));
                    break;
                case "=": case "<=":
                    TVar a = TVar.fresh();  // Changed from new TVar()
                    type = new FType(a, new FType(a, new TVar("Bool")));
                    break;
                case "flip":
                    TVar b = TVar.fresh();  // Changed from new TVar()
                    TVar c = TVar.fresh();  // Changed from new TVar()
                    TVar d = TVar.fresh();  // Changed from new TVar()
                    type = new FType(b, new FType(c, new FType(d,
                        new FType(new FType(c, new FType(b, d)), d))));
                    break;
                default:
                    throw new TypeError("Unknown operator: " + operator.getSymbol());
            }
        } else if (value.equals("True") || value.equals("False")) {
            type = new TVar("Bool");
        } else {
            type = TVar.fresh();  // Changed from new TVar()
        }
    }
}