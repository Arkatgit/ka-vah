package ca.brock.cs.lambda.types;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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

    @Override
    public Type apply(Map<TVar, Type> s) {
        // Apply the substitution recursively to both the left and right types.
        return new ProdType(left.apply(s), right.apply(s));
    }

    @Override
    public Set<TVar> getFreeTypeVariables() {
        // Collect free type variables from both the left and right types.
        Set<TVar> freeVars = new HashSet<>();
        freeVars.addAll(left.getFreeTypeVariables());
        freeVars.addAll(right.getFreeTypeVariables());
        return freeVars;
    }

    @Override
    public Type deepCloneAndFresh(Unifier unifier) {
        // Recursively clone the input and output types
        return new FType(left.deepCloneAndFresh(unifier), right.deepCloneAndFresh(unifier));
    }
}
