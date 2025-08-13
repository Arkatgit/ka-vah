package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.KCombinator;

/**
 * Represents the K combinator in the intermediate representation.
 */
public class IntermediateKCombinator extends IntermediateCombinator {

    public IntermediateKCombinator() {
        // No specific fields, just represents the K combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "K";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    /**
     * For combinator constants, methodT() simply returns the combinator itself, as it's a base case.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateKCombinator.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }

    /**
     * Converts this IntermediateKCombinator to a KCombinator.
     * @return The equivalent KCombinator.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new KCombinator();
    }
}
