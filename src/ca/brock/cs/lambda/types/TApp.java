package ca.brock.cs.lambda.types;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a type application, where a type is applied to another type.
 * For example, 'list a' is a type application where 'list' is applied to 'a'.
 */
public class TApp extends Type {
    private final Type target; // The type being applied, e.g., 'list'
    private final Type argument; // The argument type, e.g., 'a'

    public TApp(Type target, Type argument) {
        this.target = Objects.requireNonNull(target);
        this.argument = Objects.requireNonNull(argument);
    }

    public Type getTarget() {
        return target;
    }

    public Type getArgument() {
        return argument;
    }

    @Override
    public Type apply(Map<TVar, Type> s) {
        // Recursively apply the substitution to both the target and the argument.
        return new TApp(target.apply(s), argument.apply(s));
    }

    @Override
    public Set<TVar> getFreeTypeVariables() {
        // The free variables of a type application are the union of the free variables
        // of its target and argument types.
        Set<TVar> freeVars = new HashSet<>();
        freeVars.addAll(target.getFreeTypeVariables());
        freeVars.addAll(argument.getFreeTypeVariables());
        return freeVars;
    }

    @Override
    public String toString() {
        // This format correctly represents the type application.
        return "(" + target.toString() + " " + argument.toString() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TApp tapp = (TApp) o;
        return target.equals(tapp.target) && argument.equals(tapp.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, argument);
    }

    @Override
    public Type deepCloneAndFresh(Unifier unifier) {
        Type freshTarget = target;
        Type freshArgument = argument.deepCloneAndFresh(unifier);

        // Only freshen the target if it's not a type constructor
        if (target instanceof TVar) {
            TVar targetVar = (TVar) target;
            if (!isTypeConstructor(targetVar.getName())) {
                freshTarget = target.deepCloneAndFresh(unifier);
            }
        } else {
            freshTarget = target.deepCloneAndFresh(unifier);
        }

        return new TApp(freshTarget, freshArgument);
    }

    private boolean isTypeConstructor(String name) {
        return name.equals("list"); // Add other constructors as needed
    }
//    public Type deepCloneAndFresh(Unifier unifier) {
//        // Recursively clone the function and argument types
//        return new TApp(target.deepCloneAndFresh(unifier), argument.deepCloneAndFresh(unifier));
//    }

}

