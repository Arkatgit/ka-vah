package ca.brock.cs.lambda.combinators;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class WCombinator extends Combinator {
    private static final int precedence = 0;

    public WCombinator() {}

    @Override
    public String toStringPrec(int prec) {
        String result = "W";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Combinator eval(Map<String, Combinator> env) {
        return this;
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet();
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        return this;
    }
}