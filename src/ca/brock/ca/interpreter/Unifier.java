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
        this.env.clear(); // Fresh start for every unify call

        boolean success = unifyInternal(t1, t2);
        return success ? envToMap() : null;
    }

    private boolean unifyInternal(Type t1, Type t2) {
        t1 = applySubstitution(t1);
        t2 = applySubstitution(t2);

        if (t1.equals(t2)) {
            return true;
        }

        if (t1 instanceof TVar) {
            return unifyVariable((TVar) t1, t2);
        }

        if (t2 instanceof TVar) {
            return unifyVariable((TVar) t2, t1);
        }

        if (t1 instanceof FType && t2 instanceof FType) {
            FType f1 = (FType) t1;
            FType f2 = (FType) t2;
            return unifyInternal(f1.getInput(), f2.getInput()) &&
                unifyInternal(f1.getOutput(), f2.getOutput());
        }

        if (t1 instanceof ProdType && t2 instanceof ProdType) {
            ProdType p1 = (ProdType) t1;
            ProdType p2 = (ProdType) t2;
            return unifyInternal(p1.getLeft(), p2.getLeft()) &&
                unifyInternal(p1.getRight(), p2.getRight());
        }

        if (t1 instanceof Constant && t2 instanceof Constant) {
            return ((Constant) t1).getName().equals(((Constant) t2).getName());
        }

        return false; // Types don't match
    }

    private boolean unifyVariable(TVar var, Type type) {
        type = applySubstitution(type); // Ensure latest substitution applied

        if (var.equals(type)) {
            return true;
        }

        if (occursIn(var, type)) {
            return false; // Occurs check fails
        }

        env.bind(var, type);
        return true;
    }

    private Map<String, Type> envToMap() {
        Map<String, Type> map = new HashMap<>();
        for (Map.Entry<TVar, Type> entry : env.getBindings().entrySet()) {
            map.put(entry.getKey().getName(), entry.getValue());
        }
        return map;
    }

    // Apply current environment substitution
    private Type applySubstitution(Type type) {
        if (type instanceof TVar) {
            TVar var = (TVar) type;
            Type replacement = env.lookup(var);
            return replacement != null ? replacement : var;
        }
        if (type instanceof FType) {
            FType f = (FType) type;
            return new FType(
                applySubstitution(f.getInput()),
                applySubstitution(f.getOutput())
            );
        }
        if (type instanceof ProdType) {
            ProdType p = (ProdType) type;
            return new ProdType(
                applySubstitution(p.getLeft()),
                applySubstitution(p.getRight())
            );
        }
        return type; // Constants and others
    }

    // Apply given substitution map (static version, used elsewhere if needed)
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

    public TypeEnv getEnv() {
        return env;
    }
}
