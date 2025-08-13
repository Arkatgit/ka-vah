package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.YCombinator;

/**
 * Represents the Y combinator in the intermediate representation.
 */
public class IntermediateYCombinator extends IntermediateCombinator {

    public IntermediateYCombinator() {
        // No specific fields, just represents the Y combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "Y";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    /**
     * For combinator constants, methodT() simply returns the combinator itself, as it's a base case.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateYCombinator.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }


    /**
     * Converts this IntermediateYCombinator to a YCombinator.
     * @return The equivalent YCombinator.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new YCombinator();
    }
}
