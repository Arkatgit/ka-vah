package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Map;
import java.util.Set;

/**
 * An abstract class representing a pattern in a match expression.
 * Subclasses will define specific types of patterns like variables or constructors.
 */
public abstract class Pattern {
    /**
     * Attempts to match this pattern against a given term.
     *
     * @param term The term to match against.
     * @return A map of variable bindings if the match succeeds, or null if it fails.
     */
    public abstract Map<String, Term> match(Term term);

    /**
     * Returns a set of variable names bound by this pattern.
     *
     * @return A set of bound variable names.
     */
    public abstract Set<String> getBoundVariables();

    /**
     * Computes the type of the pattern.
     * This method adds new variable bindings to the environment provided by the Unifier.
     *
     * @param env The environment to add new type bindings to.
     * @param unifier The unifier for type operations.
     * @return The inferred type of the pattern.
     */
    public abstract Type computeType(Map<String, Type> env, Unifier unifier);

}
