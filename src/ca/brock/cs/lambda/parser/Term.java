package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Map;
import java.util.Set;

public abstract class Term {
    protected Type type; // Stores the computed type

    public abstract String toStringPrec(int prec);

    public String toString() {
        return toStringPrec(0);
    }

    public void setType(Type type) {
        this.type = type;
    }

    // Phase 1: Calculate and store the type using a Unifier
    public void type(Map<String, Type> env, Unifier unifier) {
        this.type = computeType(env, unifier);
    }

    // Initial call to type without a Unifier (creates a fresh one)
    public void type(Map<String, Type> env) {
        this.type = computeType(env, new Unifier());
    }

    // Phase 2: Retrieve the stored type
    public Type getType() {
        if (type == null) {
            throw new IllegalStateException("Type not computed yet. Call type() first.");
        }
        return type;
    }

    // Internal type computation (implemented by subclasses)
    protected abstract Type computeType(Map<String, Type> env, Unifier unifier);

    public abstract Term eval(Map<String, Term> env);
    public abstract Term substitute(String varName, Term value);

    public abstract Set<String> getFreeVariables();

    /**
     * Translates this lambda calculus term into a combinator calculus term.
     * @return The equivalent Combinator term.
     */
    public abstract Combinator translate();

    /**
     * Converts this lambda calculus term into an IntermediateTerm.
     * This is the first step in the two-phase translation to combinators.
     * @return The equivalent IntermediateTerm.
     */
    public abstract IntermediateTerm toIntermediateTerm();

}