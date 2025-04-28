package ca.brock.ca.interpreter;

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
}