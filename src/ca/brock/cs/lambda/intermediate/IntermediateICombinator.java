package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.ICombinator;

/**
 * Represents the I combinator in the intermediate representation.
 */
public class IntermediateICombinator extends IntermediateCombinator {

    public IntermediateICombinator() {
        // No specific fields, just represents the I combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "I";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }
    /**
     * For combinator constants, methodT() simply returns the combinator itself, as it's a base case.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateICombinator.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }

    /**
     * Converts this IntermediateICombinator to an ICombinator.
     * @return The equivalent ICombinator.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new ICombinator();
    }
}
