package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Type;

import java.util.Map;

public abstract class Term {
    protected Type type; // Stores the computed type

    public abstract String toStringPrec(int prec);

    public String toString() {
        return toStringPrec(0);
    }

    // Phase 1: Calculate and store the type
    public void type(Map<String, Type> env) {
        this.type = computeType(env);
    }

    // Phase 2: Retrieve the stored type
    public Type getType() {
        if (type == null) {
            throw new IllegalStateException("Type not computed yet. Call type() first.");
        }
        return type;
    }

    // Internal type computation (implemented by subclasses)
    protected abstract Type computeType(Map<String, Type> env);
}