package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.BCombinator;
import ca.brock.cs.lambda.combinators.Combinator;

/**
 * Represents the B combinator in the intermediate representation.
 * B = λf.λg.λx. f (g x)
 */
public class IntermediateBCombinator extends IntermediateCombinator {

    public IntermediateBCombinator() {
        // B combinator has no specific fields, just represents itself
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "B";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }
    /**
     * For combinator constants, methodT() simply returns the combinator itself, as it's a base case.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateBCombinator.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }


    /**
     * Converts this IntermediateBCombinator to a BCombinator.
     * @return The equivalent BCombinator.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new BCombinator();
    }
}
