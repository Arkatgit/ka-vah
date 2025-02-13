package ca.brock.cs.lambda;

public class BooleanLiteral extends Term {
    private boolean value;

    public BooleanLiteral(boolean v)
    {
        value = v;
    }

    @Override
    public String toStringPrec(int prec)
    {
       return value? "True" : "False";
    }

}
