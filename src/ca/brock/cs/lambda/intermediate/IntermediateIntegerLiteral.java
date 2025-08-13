package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import java.util.Collections;
import java.util.Set;

/**
 * Represents an integer literal in the intermediate representation.
 */
public class IntermediateIntegerLiteral extends IntermediateTerm {
    private int value;

    public IntermediateIntegerLiteral(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toStringPrec(int prec) {
        return String.valueOf(value);
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Integer literals have no free variables
    }

    /**
     * For integer literals, methodT() simply returns the literal itself, as it's a base case
     * and doesn't involve lambda abstraction elimination.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateIntegerLiteral.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        // Integer literals are base cases for methodT; they don't contain lambdas to eliminate.
        // The 'optimize' flag is not directly used here but is part of the signature
        // to maintain consistency across all IntermediateTerm.methodT implementations.
        return this;
    }

    /**
     * Converts this IntermediateIntegerLiteral to a CombinatorConstant holding the integer value.
     * @return The equivalent CombinatorConstant.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new CombinatorConstant(value);
    }
}