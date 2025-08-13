package ca.brock.cs.lambda.combinators;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class YCombinator extends Combinator {
    private static final int precedence = 20;

    public YCombinator() {}

    @Override
    public String toStringPrec(int prec) {
        String result = "Y";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Combinator eval(Map<String, Combinator> env) {
        return this; // Y combinator itself is in normal form until applied
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Y is a constant combinator, no free variables
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        return this; // Y is a constant, substitution doesn't apply
    }
}