package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorVariable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a variable in the intermediate representation.
 */
public class IntermediateVariable extends IntermediateTerm {
    private String name;

    public IntermediateVariable(String name) {
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
        Set<String> freeVars = new HashSet<>();
        freeVars.add(name);
        return freeVars;
    }

//    /**
//     * For variables, methodT() simply returns the variable itself, as it's a base case
//     * and doesn't involve lambda abstraction elimination.
//     * @return This IntermediateVariable.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        return this;
//    }
    /**
     * For variables, methodT() simply returns the variable itself, as it's a base case
     * and doesn't involve lambda abstraction elimination.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateVariable.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }

    /**
     * Converts this IntermediateVariable to a CombinatorVariable.
     * @return The equivalent CombinatorVariable.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new CombinatorVariable(name);
    }
}
