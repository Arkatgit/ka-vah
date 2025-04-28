package ca.brock.ca.interpreter;

import java.util.HashMap;
import java.util.Map;

public class TypeEnv {
    private final Map<TVar, Type> bindings;

    public TypeEnv() {
        this.bindings = new HashMap<>();
    }

    public void bind(TVar var, Type type) {
        bindings.put(var, type);
    }

    public Type lookup(TVar var) {
        Type type = bindings.get(var);
        if (type instanceof TVar && bindings.containsKey(type)) {
            return lookup((TVar) type); // Follow transitive substitution
        }
        return type;
    }

    public boolean contains(TVar var) {
        return bindings.containsKey(var);
    }

    public Map<TVar, Type> getBindings() {
        return new HashMap<>(bindings); // Defensive copy
    }

    public void clear() {
        bindings.clear();
    }

    @Override
    public String toString() {
        return bindings.toString();
    }
}
