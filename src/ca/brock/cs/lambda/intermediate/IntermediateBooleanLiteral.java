package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a boolean literal in the intermediate representation.
 */
public class IntermediateBooleanLiteral extends IntermediateTerm {
    private boolean value;

    public IntermediateBooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toStringPrec(int prec) {
        return value ? "True" : "False";
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Boolean literals have no free variables
    }

    /**
     * For boolean literals, methodT() simply returns the literal itself, as it's a base case
     * and doesn't involve lambda abstraction elimination.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateBooleanLiteral.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        // Boolean literals are base cases for methodT; they don't contain lambdas to eliminate.
        // The 'optimize' flag is not directly used here but is part of the signature
        // to maintain consistency across all IntermediateTerm.methodT implementations.
        return this;
    }

    /**
     * Converts this IntermediateBooleanLiteral to a CombinatorConstant holding the boolean value.
     * @return The equivalent CombinatorConstant.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new CombinatorConstant(value);
    }
}