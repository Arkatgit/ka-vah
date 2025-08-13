// ca.brock.cs.lambda.intermediate.IntermediateTerm.java
package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import java.util.Set;

/**
 * Abstract base class for terms in the intermediate representation.
 * These terms are produced after the first phase of translation from lambda calculus terms.
 * The second phase (lambda elimination) operates on these terms.
 */
public abstract class IntermediateTerm {

    protected static final int precedence = 0; // Default precedence for intermediate terms

    /**
     * Returns a string representation of the term with respect to precedence.
     * @param prec The current precedence level.
     * @return The string representation.
     */
    public abstract String toStringPrec(int prec);

    @Override
    public String toString() {
        return toStringPrec(0);
    }

    /**
     * Computes the set of free variables in this intermediate term.
     * @return A set of strings, where each string is the name of a free variable.
     */
    public abstract Set<String> getFreeVariables();

    /**
     * Applies the T[] transformation (lambda elimination) to this intermediate term.
     * This method recursively processes the term to remove lambda abstractions.
     *
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return A new IntermediateTerm with this layer of abstraction eliminated,
     * or the term itself if it's a base case (variable, constant, combinator).
     */
    public abstract IntermediateTerm methodT(boolean optimize);

    /**
     * Converts this (fully transformed, i.e., lambda-free) IntermediateTerm
     * into a Combinator calculus term. This is the final step in the translation.
     * @return The equivalent Combinator term.
     * @throws IllegalStateException if called on an IntermediateAbstraction (which should have been eliminated).
     */
    public abstract Combinator toCombinatorTerm();
}