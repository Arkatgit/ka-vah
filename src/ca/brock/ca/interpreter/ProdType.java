package ca.brock.ca.interpreter;


// Product type (e.g., α × β)
public class ProdType extends Type {
    private Type left;
    private Type right;

    public ProdType(Type left, Type right) {
        this.left = left;
        this.right = right;
    }

    public Type getLeft() {
        return left;
    }

    public Type getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + left + " × " + right + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProdType) {
            ProdType other = (ProdType) obj;
            return this.left.equals(other.left) && this.right.equals(other.right);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return left.hashCode() + right.hashCode();
    }
}