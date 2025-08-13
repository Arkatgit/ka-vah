package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a constant (like an integer, boolean, or operator symbol) in the intermediate representation.
 */
public class IntermediateConstant extends IntermediateTerm {
    private Object value; // Can hold String (for operators), Boolean, Integer

    public IntermediateConstant(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public boolean isInteger() {
        return value instanceof Integer;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    @Override
    public String toStringPrec(int prec) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "True" : "False";
        } else if (value instanceof Integer) {
            return String.valueOf(value);
        }
        return value.toString();
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Constants have no free variables
    }

//    /**
//     * For constants, methodT() simply returns the constant itself, as it's a base case
//     * and doesn't involve lambda abstraction elimination.
//     * @return This IntermediateConstant.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        return this;
//    }
    /**
     * For constants, methodT() simply returns the constant itself, as it's a base case
     * and doesn't involve lambda abstraction elimination.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return This IntermediateConstant.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }


    /**
     * Converts this IntermediateConstant to a CombinatorConstant.
     * @return The equivalent CombinatorConstant.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new CombinatorConstant(value);
    }
}
