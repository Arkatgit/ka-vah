package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.SCombinator;

/**
 * Represents the S combinator in the intermediate representation.
 */
public class IntermediateSCombinator extends IntermediateCombinator {

    public IntermediateSCombinator() {
        // No specific fields, just represents the S combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "S";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    /**
     * For combinator constants, methodT() simply returns the combinator itself, as it's a base case.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateSCombinator.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }

    /**
     * Converts this IntermediateSCombinator to an SCombinator.
     * @return The equivalent SCombinator.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new SCombinator();
    }
}
