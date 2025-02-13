package ca.brock.cs.lambda;

public class IntegerLiteral extends Term {
    private int value;

    public IntegerLiteral(int v)
    {
        value = v;
    }

    @Override
    public String toStringPrec(int prec) {
        return Integer.toString(value);
    }
}
