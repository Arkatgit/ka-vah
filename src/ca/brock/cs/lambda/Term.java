package ca.brock.cs.lambda;

public abstract class Term {
    public abstract String toStringPrec(int prec);

    public String toString() {
        return toStringPrec(0);
    }
}
