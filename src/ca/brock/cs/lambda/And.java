package ca.brock.cs.lambda;

public class And extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 5;
    public And(Term l, Term r)
    {
        left = l;
        right = r;
    }
    public Term getLeft() {
        return left;
    }

    public Term getRight() {
        return right;
    }
    @Override
    public String toStringPrec(int prec)
    {
        return left.toStringPrec(prec) + " and " + right.toStringPrec(prec);
    }
}
