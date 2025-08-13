package ca.brock.cs.lambda.combinators;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class KCombinator extends Combinator {
    private static final int precedence = 20;

    public KCombinator() {}

    @Override
    public String toStringPrec(int prec) {
        String result = "K";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Combinator eval(Map<String, Combinator> env) {
        return this; // K combinator is already in normal form
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // K is a constant combinator, no free variables
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        return this; // K is a constant, substitution doesn't apply
    }

}
