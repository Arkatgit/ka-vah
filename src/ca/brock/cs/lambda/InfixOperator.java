package ca.brock.cs.lambda;

public enum InfixOperator {

    ADD("+", 2, true),
    SUBTRACT("-", 2, true),
    MULTIPLY("*", 2, true),
    DIVIDE("/", 2, true),
    AND("and", 2, false),
    OR("or", 2, false),
    EQUALS("=", 2, false),
    LESS_THAN("<=", 2, false),

    FLIP("flip", 2, true);

    private final String symbol;
    private final int arity; // Number of arguments
    private final boolean canBeUsedAsSection;

    InfixOperator(String symbol, int arity, boolean canBeUsedAsSection) {
        this.symbol = symbol;
        this.arity = arity;
        this.canBeUsedAsSection = canBeUsedAsSection;
    }

    // Method to find an operator by symbol
    public static InfixOperator fromSymbol(String symbol) {
        for (InfixOperator op : values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        return null; // Not found
    }

    public String getSymbol() {
        return symbol;
    }

    public int getArity() {
        return arity;
    }

    public boolean canBeUsedAsSection() {
        return canBeUsedAsSection;
    }



}
