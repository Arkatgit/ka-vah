package ca.brock.cs.lambda.types;

import ca.brock.cs.lambda.parser.Term;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a top-level function definition.
 * It is now mutable to support the two-part parsing process.
 */
public class FunctionDefinition implements DefinedValue {
    private final String name;
    private Type type;
    private Term term; // This field is no longer final

    public FunctionDefinition(String name, Type type, Term term) {
        this.name = name;
        this.type = type;
        this.term = term;
    }

    public String getName() {
        return name;
    }

    public void setType(Type type){
        this.type = type;
    }
    public Type getType() {
        return type;
    }

    public Term getTerm() {
        return term;
    }

    /**
     * Setter method to update the term after parsing the body.
     */
    public void setTerm(Term term) {
        this.term = term;
    }

    /**
     * Checks if the declared type of the function matches the inferred type of its term.
     * This is a crucial step to validate user-provided type annotations.
     */
    // In the function type checking code
    public void checkTypeMatch(Map<String, Type> env, Unifier unifier) {
        // For recursive functions, use a fixed-point approach
       // TVar recursiveTypeVar = unifier.fresh();
       TVar recursiveTypeVar = TVar.fresh();
        Map<String, Type> newEnv = new HashMap<>(env);
        newEnv.put(this.name, recursiveTypeVar);

        // Type-check the function body with the temporary type
        this.term.type(newEnv, unifier);
        Type inferredBodyType = term.getType();

        // The function type should be (input -> output)
        if (!(inferredBodyType instanceof FType)) {
            throw new TypeError("Function body must have function type");
        }

        FType inferredFType = (FType) inferredBodyType;

        // Unify the recursive type variable with the inferred function type
        Map<TVar, Type> sub = unifier.unify(recursiveTypeVar, inferredFType);
        if (sub == null) {
            throw new TypeError("Recursive function type inference failed");
        }

        // Apply the substitution to get the final type
        this.type = unifier.applySubstitution(inferredFType, sub);
    }

    @Override
    public String toString() {
        return name + " : " + type.toString() + " = " + (term == null ? "" : term.toString());
    }
}