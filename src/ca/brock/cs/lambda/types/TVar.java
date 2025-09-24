package ca.brock.cs.lambda.types;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class TVar extends Type {
    private static int nextVarId = 0;
    private static int nextGenericId = 0;
    private String name;

    public TVar(String name) {
        this.name = name;
    }

    // New constructor for fresh variables
    public static TVar fresh() {
        return new TVar("a" + nextVarId++);
    }

    // New constructor for specific type variables
    public static TVar named(String prefix) {
        return new TVar(prefix + nextVarId++);
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
        if (obj instanceof TVar) {
            return this.name.equals(((TVar) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public Type apply(Map<TVar, Type> s) {
        // If this type variable is in the substitution map, return the substituted type.
        // Otherwise, return itself.
        if (s.containsKey(this)) {
            return s.get(this);
        }
        return this;
    }

    @Override
    public Set<TVar> getFreeTypeVariables() {
        // A type variable's only free variable is itself.
        return Collections.singleton(this);
    }


    @Override
    public Type deepCloneAndFresh(Unifier unifier) {
        // When cloning a type variable, we must create a brand new, fresh type variable
        //return unifier.fresh();
        return TVar.fresh();
    }


}
