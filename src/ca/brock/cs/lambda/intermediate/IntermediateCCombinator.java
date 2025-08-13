package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.CCombinator;
import ca.brock.cs.lambda.combinators.Combinator;

/**
 * Represents the C combinator in the intermediate representation.
 * C = λf.λx.λy. f y x
 */
public class IntermediateCCombinator extends IntermediateCombinator {

    public IntermediateCCombinator() {
        // C combinator has no specific fields, just represents itself
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "C";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    /**
     * For combinator constants, methodT() simply returns the combinator itself, as it's a base case.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateCCombinator.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }


    /**
     * Converts this IntermediateCCombinator to a CCombinator.
     * @return The equivalent CCombinator.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new CCombinator();
    }
}
