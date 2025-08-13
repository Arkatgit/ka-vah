package ca.brock.cs.lambda.combinators;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents the C combinator: C = λf.λx.λy. f y x
 * C is a "flip" or "exchange" combinator.
 */
public class CCombinator extends Combinator {

    // Define precedence for this specific combinator for toStringPrec formatting
    private static final int precedence = 0; // Individual combinators typically have the lowest precedence

    public CCombinator() {
        // No specific fields, just represents a fundamental combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "C";
        // Parenthesize if the current context's precedence is higher than this combinator's
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Combinator eval(Map<String, Combinator> env) {
        return this; // Combinators themselves are values, they reduce only when applied
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Combinators are closed terms
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        return this; // Combinators don't contain variables to substitute
    }
}
