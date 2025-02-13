package ca.brock.cs.lambda;

public class Constant extends Term {
    private String value;
    private final InfixOperator operator;

    // Constructor for normal constants
    public Constant(String value) {
        this.value = value;
        this.operator = InfixOperator.fromSymbol(value);
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
}