package ca.brock.ca.interpreter;

import java.util.HashMap;
import java.util.Map;

// Type environment for storing type variable bindings
public class TypeEnv {
    private Map<TVar, Type> bindings;

    public TypeEnv() {
        this.bindings = new HashMap<>();
    }

    public void bind(TVar var, Type type) {
        bindings.put(var, type);
    }

    public Type lookup(TVar var) {
        return bindings.get(var);
    }

    public boolean contains(TVar var) {
        return bindings.containsKey(var);
    }

    @Override
    public String toString() {
        return bindings.toString();
    }
}
