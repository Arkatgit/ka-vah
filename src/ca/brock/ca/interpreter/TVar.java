package ca.brock.ca.interpreter;

// Type variable (e.g., α, β)
public class TVar extends Type {
    private String name;

    public TVar(String name) {
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