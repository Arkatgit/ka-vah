package ca.brock.ca.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Unifier {
    private TypeEnv env;

    public Unifier() {
        this.env = new TypeEnv();
    }

    public Map<String, Type> unify(Type t1, Type t2) {
        env.clear();
        boolean success = unifyInternal(t1, t2);
        if (!success) {
            return null;
        }

        // Create substitution map with fully resolved types
        Map<String, Type> substitution = new HashMap<>();
        for (Map.Entry<TVar, Type> entry : env.getBindings().entrySet()) {
            substitution.put(entry.getKey().getName(),
                deepApplySubstitution(entry.getValue(), env.getBindings()));
        }
        return substitution;
    }

    private boolean unifyInternal(Type t1, Type t2) {
        t1 = applySubstitution(t1);
        t2 = applySubstitution(t2);

        // Handle primitive type mismatches first
        if (isPrimitiveMismatch(t1, t2)) {
            return false;
        }

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

        return false;
    }

    private boolean isPrimitiveMismatch(Type t1, Type t2) {
        // Check if trying to unify incompatible primitive types
        if (t1 instanceof TVar && t2 instanceof TVar) {
            String name1 = ((TVar)t1).getName();
            String name2 = ((TVar)t2).getName();

            if ((name1.equals("Int") && name2.equals("Bool")) ||
                (name1.equals("Bool") && name2.equals("Int"))) {
                return true;
            }
        }
        return false;
    }

    private boolean unifyVariable(TVar var, Type type) {
        type = applySubstitution(type);
        if (var.equals(type)) {
            return true;
        }
        if (occursCheck(var, type)) {
            return false;
        }
        env.bind(var, type);
        return true;
    }

    private boolean occursCheck(TVar var, Type type) {
        type = applySubstitution(type);
        if (type instanceof TVar) {
            return var.equals(type);
        }
        if (type instanceof FType) {
            FType ft = (FType) type;
            return occursCheck(var, ft.getInput()) || occursCheck(var, ft.getOutput());
        }
        return false;
    }

    private Type applySubstitution(Type type) {
        if (type instanceof TVar) {
            TVar var = (TVar) type;
            Type replacement = env.lookup(var);
            return replacement != null ? applySubstitution(replacement) : var;
        }
        if (type instanceof FType) {
            FType ft = (FType) type;
            return new FType(
                applySubstitution(ft.getInput()),
                applySubstitution(ft.getOutput())
            );
        }
        return type;
    }

    private Type deepApplySubstitution(Type type, Map<TVar, Type> substitution) {
        if (type instanceof TVar) {
            Type sub = substitution.get(type);
            return sub != null ? deepApplySubstitution(sub, substitution) : type;
        }
        if (type instanceof FType) {
            FType ft = (FType) type;
            return new FType(
                deepApplySubstitution(ft.getInput(), substitution),
                deepApplySubstitution(ft.getOutput(), substitution)
            );
        }
        return type;
    }

    public TypeEnv getEnv() {
        return env;
    }
    public static Type applySubstitution(Type type, Map<String, Type> substitution) {
        if (type instanceof TVar) {
            Type sub = substitution.get(((TVar)type).getName());
            return sub != null ? applySubstitution(sub, substitution) : type;
        }
        else if (type instanceof FType) {
            FType ft = (FType)type;
            return new FType(
                applySubstitution(ft.getInput(), substitution),
                applySubstitution(ft.getOutput(), substitution)
            );
        }
        return type;
    }

}