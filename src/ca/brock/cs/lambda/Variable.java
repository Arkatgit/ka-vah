package ca.brock.cs.lambda;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    @Override
    public String toStringPrec(int prec) {
        return name;
    }
}