package ca.brock.ca.interpreter;

// Constant types (e.g., Int, Bool)
public class Constant extends Type {
    private String name;

    public Constant(String name) {
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
        if (obj instanceof Constant) {
            return this.name.equals(((Constant) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}