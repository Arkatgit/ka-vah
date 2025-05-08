package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

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

    // Existing computeType method - remove this
    // protected abstract Type computeType(Map<String, Type> env);
}