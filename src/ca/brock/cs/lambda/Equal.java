package ca.brock.cs.lambda;

public class Equal extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 10;

    public Equal(Term l, Term r)
    {
        left = l;
        right = r;
    }

    public Term getLeft(){
        return left;
    }
    public Term getRight(){
        return right;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return left.toStringPrec(prec) + " = " + right.toStringPrec(prec);
    }
}
