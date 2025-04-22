package ca.brock.ca.interpreter;

import java.util.HashMap;
import java.util.Map;

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

    // Add this method to expose the bindings map
    public Map<TVar, Type> getBindings() {
        return new HashMap<>(bindings); // Return a copy for immutability
    }

    @Override
    public String toString() {
        return bindings.toString();
    }
}