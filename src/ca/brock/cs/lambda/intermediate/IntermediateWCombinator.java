package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.WCombinator;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents the W combinator: W f x = f x x
 */
public class IntermediateWCombinator extends IntermediateCombinator {

    public IntermediateWCombinator() {
        // No specific fields, just represents the W combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "W";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }

    @Override
    public Combinator toCombinatorTerm() {
        return new WCombinator();
    }
}