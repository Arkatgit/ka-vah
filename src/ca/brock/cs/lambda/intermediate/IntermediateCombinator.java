// ca.brock.cs.lambda.intermediate.IntermediateCombinator.java
package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for combinators (S, K, I, Y, B, C) in the intermediate representation.
 */
public abstract class IntermediateCombinator extends IntermediateTerm {

    protected static final int precedence = 20; // Default precedence for combinators

    /**
     * Combinators themselves do not change during the T[] transformation,
     * as they are the target of the transformation.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateCombinator.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        // Combinators are base cases for methodT; they don't contain lambdas to eliminate.
        // The 'optimize' flag is not directly used here but is part of the signature
        // to maintain consistency across all IntermediateTerm.methodT implementations.
        return this;
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Pure combinators have no free variables
    }

    // toCombinatorTerm() will be implemented by concrete subclasses (e.g., IntermediateICombinator)
}