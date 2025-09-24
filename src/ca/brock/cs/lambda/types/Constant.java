package ca.brock.cs.lambda.types;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

// Constant types (e.g., Int, Bool)
public class Constant extends Type {
    private String name;

    public Constant(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Constant) {
            return this.name.equals(((Constant) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public Type apply(Map<TVar, Type> s) {
        // A constant type has no type variables, so a substitution has no effect.
        return this;
    }

    @Override
    public Set<TVar> getFreeTypeVariables() {
        // A constant type has no free type variables.
        return Collections.emptySet();
    }
    protected void collectFreeTypeVariables(Set<TVar> freeVars) {
        // Constants have no free type variables
    }

    @Override
    public Type deepCloneAndFresh(Unifier unifier) {
        // Constants are immutable and have no type variables, so we can just return this instance
        return this;
    }

}
