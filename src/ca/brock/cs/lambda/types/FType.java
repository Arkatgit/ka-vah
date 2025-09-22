package ca.brock.cs.lambda.types;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

// Function type (e.g., α → β)
public class FType extends Type {
    private Type input;
    private Type output;

    public FType(Type input, Type output) {
        this.input = input;
        this.output = output;
    }

    public Type getInput() {
        return input;
    }

    public Type getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "(" + input + " → " + output + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FType) {
            FType other = (FType) obj;
            return this.input.equals(other.input) && this.output.equals(other.output);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return input.hashCode() + output.hashCode();
    }

    @Override
    public Type apply(Map<TVar, Type> s) {
        // Apply the substitution recursively to both the input and output types.
        return new FType(input.apply(s), output.apply(s));
    }

    @Override
    public Set<TVar> getFreeTypeVariables() {
        // Collect free type variables from both the input and output types.
        Set<TVar> freeVars = new HashSet<>();
        freeVars.addAll(input.getFreeTypeVariables());
        freeVars.addAll(output.getFreeTypeVariables());
        return freeVars;
    }


    @Override
    public Type deepCloneAndFresh(Unifier unifier) {
        return new FType(
            input.deepCloneAndFresh(unifier),
            output.deepCloneAndFresh(unifier)
        );
    }
//    public Type deepCloneAndFresh(Unifier unifier) {
//        // Recursively clone the input and output types
//        return new FType(input.deepCloneAndFresh(unifier), output.deepCloneAndFresh(unifier));
//    }
}
