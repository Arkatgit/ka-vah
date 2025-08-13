package ca.brock.cs.lambda.combinators;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents the B combinator: B = λf.λg.λx. f (g x)
 * B is a "composition" combinator.
 */
public class BCombinator extends Combinator {

    // Define precedence for this specific combinator for toStringPrec formatting
    private static final int precedence = 0; // Individual combinators typically have the lowest precedence

    public BCombinator() {
        // No specific fields, it's a fundamental combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "B";
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
