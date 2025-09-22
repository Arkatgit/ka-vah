package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents a constructor of a user-defined data type in the intermediate representation.
 * This class is a direct analog of IntermediateVariable, but for Constructors.
 */
public class IntermediateConstructor extends IntermediateTerm {
    private final String name;

    public IntermediateConstructor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toStringPrec(int prec) {
        return name;
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Constructors have no free variables
    }

    @Override
    public IntermediateTerm methodT(boolean optimize) {
        // A constructor is a constant term, so its T[] transformation is itself.
        return this;
    }

    @Override
    public Combinator toCombinatorTerm() {
        // Converts the IntermediateConstructor to its final Combinator form.
        return new CombinatorConstant(name);
    }
}
