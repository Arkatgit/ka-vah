package ca.brock.ca.interpreter;
import java.util.HashMap;
import java.util.Map;

public class Unifier {
    private TypeEnv env;

    public Unifier() {
        this.env = new TypeEnv();
    }

    // Unify two types
    public boolean unify(Type t1, Type t2) {
        t1 = applySubstitution(t1);
        t2 = applySubstitution(t2);

        // Case 1: Both types are the same
        if (t1.equals(t2)) {
            return true;
        }

        // Case 2: One of the types is a type variable
        if (t1 instanceof TVar) {
            bind((TVar) t1, t2);
            return true;
        }
        if (t2 instanceof TVar) {
            bind((TVar) t2, t1);
            return true;
        }

        // Case 3: Both types are function types
        if (t1 instanceof FType && t2 instanceof FType) {
            FType f1 = (FType) t1;
            FType f2 = (FType) t2;
            return unify(f1.getInput(), f2.getInput()) && unify(f1.getOutput(), f2.getOutput());
        }

        // Case 4: Both types are product types
        if (t1 instanceof ProdType && t2 instanceof ProdType) {
            ProdType p1 = (ProdType) t1;
            ProdType p2 = (ProdType) t2;
            return unify(p1.getLeft(), p2.getLeft()) && unify(p1.getRight(), p2.getRight());
        }

        // Case 5: Both types are constants
        if (t1 instanceof Constant && t2 instanceof Constant) {
            return ((Constant) t1).getName().equals(((Constant) t2).getName());
        }

        // If none of the above cases apply, unification fails
        return false;
    }

    // Bind a type variable to a type
    private void bind(TVar var, Type type) {
        if (occursIn(var, type)) {
            throw new RuntimeException("Circular dependency detected: " + var + " occurs in " + type);
        }
        env.bind(var, type);
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

    // Apply substitutions to a type
    private Type applySubstitution(Type type) {
        if (type instanceof TVar) {
            TVar var = (TVar) type;
            if (env.contains(var)) {
                return env.lookup(var);
            }
        }
        if (type instanceof FType) {
            FType f = (FType) type;
            return new FType(applySubstitution(f.getInput()), applySubstitution(f.getOutput()));
        }
        if (type instanceof ProdType) {
            ProdType p = (ProdType) type;
            return new ProdType(applySubstitution(p.getLeft()), applySubstitution(p.getRight()));
        }
        return type;
    }

    // Get the type environment
    public TypeEnv getEnv() {
        return env;
    }
}