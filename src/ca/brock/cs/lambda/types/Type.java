package ca.brock.cs.lambda.types;

import java.util.Map;
import java.util.Set;

/**
 * The base abstract class for all types in the lambda calculus system.
 * This class now includes abstract methods for type substitution and free type variable collection.
 */
public abstract class Type {

    /**
     * Applies a type substitution to this type.
     *
     * @param s The substitution map from type variables to types.
     * @return A new Type with the substitution applied.
     */
    public abstract Type apply(Map<TVar, Type> s);

    /**
     * Collects all free type variables from this type.
     *
     * @return A Set of free type variables.
     */
    public abstract Set<TVar> getFreeTypeVariables();

    /**
     * Creates a deep clone of the type, with all type variables replaced
     * by fresh, unique ones. This is essential to prevent type variable
     * capture when a polymorphic type is used multiple times.
     * @param unifier The unifier to generate fresh type variables.
     * @return A new instance of the type with fresh type variables.
     */
    public abstract Type deepCloneAndFresh(Unifier unifier);


}
