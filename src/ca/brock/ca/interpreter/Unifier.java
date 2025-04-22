package ca.brock.ca.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Unifier {
    private TypeEnv env;

    public Unifier() {
        this.env = new TypeEnv();
    }

    // Unify two types and return substitution map or null
    public Map<String, Type> unify(Type t1, Type t2) {
        t1 = applySubstitution(t1);
        t2 = applySubstitution(t2);

        // Case 1: Both types are the same
        if (t1.equals(t2)) {
            return envToMap();
        }

        // Case 2: One of the types is a type variable
        if (t1 instanceof TVar) {
            return unifyVariable((TVar) t1, t2);
        }
        if (t2 instanceof TVar) {
            return unifyVariable((TVar) t2, t1);
        }

        // Case 3: Both types are function types
        if (t1 instanceof FType && t2 instanceof FType) {
            FType f1 = (FType) t1;
            FType f2 = (FType) t2;
            Map<String, Type> sub1 = unify(f1.getInput(), f2.getInput());
            if (sub1 == null) return null;
            Map<String, Type> sub2 = unify(
                applySubstitution(f1.getOutput(), sub1),
                applySubstitution(f2.getOutput(), sub1)
            );
            return combineSubstitutions(sub1, sub2);
        }

        // Case 4: Both types are product types
        if (t1 instanceof ProdType && t2 instanceof ProdType) {
            ProdType p1 = (ProdType) t1;
            ProdType p2 = (ProdType) t2;
            Map<String, Type> sub1 = unify(p1.getLeft(), p2.getLeft());
            if (sub1 == null) return null;
            Map<String, Type> sub2 = unify(
                applySubstitution(p1.getRight(), sub1),
                applySubstitution(p2.getRight(), sub1)
            );
            return combineSubstitutions(sub1, sub2);
        }

        // Case 5: Both types are constants
        if (t1 instanceof Constant && t2 instanceof Constant) {
            return ((Constant) t1).getName().equals(((Constant) t2).getName())
                ? envToMap()
                : null;
        }

        // Unification failed
        return null;
    }

    private Map<String, Type> unifyVariable(TVar var, Type type) {
        if (occursIn(var, type)) {
            return null; // Occurs check failed
        }
        env.bind(var, type);
        return envToMap();
    }

    // Convert TypeEnv to Map<String, Type>
    private Map<String, Type> envToMap() {
        Map<String, Type> map = new HashMap<>();
        for (Map.Entry<TVar, Type> entry : env.getBindings().entrySet()) {
            map.put(entry.getKey().getName(), entry.getValue());
        }
        return map;
    }

    // Combine two substitutions
    private Map<String, Type> combineSubstitutions(Map<String, Type> sub1, Map<String, Type> sub2) {
        if (sub2 == null) return null;
        Map<String, Type> combined = new HashMap<>(sub1);
        combined.putAll(sub2);
        return combined;
    }

    // Apply substitutions from current environment
    private Type applySubstitution(Type type) {
        return applySubstitution(type, envToMap());
    }

    // Apply substitutions from a given map
    public static Type applySubstitution(Type type, Map<String, Type> substitution) {
        if (type instanceof TVar) {
            TVar var = (TVar) type;
            Type replacement = substitution.get(var.getName());
            return replacement != null ? replacement : var;
        }
        if (type instanceof FType) {
            FType f = (FType) type;
            return new FType(
                applySubstitution(f.getInput(), substitution),
                applySubstitution(f.getOutput(), substitution)
            );
        }
        if (type instanceof ProdType) {
            ProdType p = (ProdType) type;
            return new ProdType(
                applySubstitution(p.getLeft(), substitution),
                applySubstitution(p.getRight(), substitution)
            );
        }
        return type;
    }

    // Check if a type variable occurs in a type
    private boolean occursIn(TVar var, Type type) {
        type = applySubstitution(type);
        if (type instanceof TVar) {
            return type.equals(var);
        }
        if (type instanceof FType) {
            FType f = (FType) type;
            return occursIn(var, f.getInput()) || occursIn(var, f.getOutput());
        }
        if (type instanceof ProdType) {
            ProdType p = (ProdType) type;
            return occursIn(var, p.getLeft()) || occursIn(var, p.getRight());
        }
        return false;
    }

    // Get the type environment
    public TypeEnv getEnv() {
        return env;
    }
}