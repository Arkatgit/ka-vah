package ca.brock.cs.lambda;

public class Recursion extends Term {

    private String name;
    private Term body;

    public Recursion(String n, Term b)
    {
        name = n;
        body = b;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return name + ". " + body.toStringPrec(prec);
    }

}
