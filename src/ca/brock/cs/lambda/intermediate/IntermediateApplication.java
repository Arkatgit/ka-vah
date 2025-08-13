package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an application of one intermediate term to another.
 */
public class IntermediateApplication extends IntermediateTerm {
    private IntermediateTerm function;
    private IntermediateTerm argument;

    private static final int precedence = 20;

    public IntermediateApplication(IntermediateTerm function, IntermediateTerm argument) {
        this.function = function;
        this.argument = argument;
    }

    public IntermediateTerm getFunction() {
        return function;
    }

    public IntermediateTerm getArgument() {
        return argument;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = function.toStringPrec(precedence) + " " + argument.toStringPrec(precedence + 1);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.addAll(function.getFreeVariables());
        freeVars.addAll(argument.getFreeVariables());
        return freeVars;
    }

//    /**
//     * Applies the T[] transformation rules.
//     * T[(E1 E2)] = (T[E1] T[E2])
//     * This method recursively calls methodT() on its function and argument.
//     * @return The transformed IntermediateApplication.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        // T[(E1 E2)] = (T[E1] T[E2])
//        return new IntermediateApplication(function.methodT(), argument.methodT());
//    }
    /**
     * Applies the T[] transformation to the sub-terms of this application.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return A new IntermediateApplication with transformed sub-terms.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        // Recursively apply methodT to function and argument
        return new IntermediateApplication(function.methodT(optimize), argument.methodT(optimize));
    }



    /**
     * Converts this IntermediateApplication to a CombinatorApplication.
     * @return The equivalent CombinatorApplication.
     */
    @Override
    public Combinator toCombinatorTerm() {
        return new CombinatorApplication(function.toCombinatorTerm(), argument.toCombinatorTerm());
    }
}
