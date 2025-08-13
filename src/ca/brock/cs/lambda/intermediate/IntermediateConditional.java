package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import java.util.HashSet;
import java.util.Set;

public class IntermediateConditional extends IntermediateTerm {
    private IntermediateTerm condition;
    private IntermediateTerm trueBranch;
    private IntermediateTerm falseBranch;

    private static final int precedence = 5; // Lowest precedence for conditionals

    public IntermediateConditional(IntermediateTerm condition, IntermediateTerm trueBranch, IntermediateTerm falseBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public IntermediateTerm getCondition() {
        return condition;
    }

    public IntermediateTerm getTrueBranch() {
        return trueBranch;
    }

    public IntermediateTerm getFalseBranch() {
        return falseBranch;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "if " + condition.toStringPrec(precedence) +
            " then " + trueBranch.toStringPrec(precedence) +
            " else " + falseBranch.toStringPrec(precedence);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.addAll(condition.getFreeVariables());
        freeVars.addAll(trueBranch.getFreeVariables());
        freeVars.addAll(falseBranch.getFreeVariables());
        return freeVars;
    }

//    /**
//     * For conditionals, methodT() recursively calls methodT() on its sub-terms.
//     * @return A new IntermediateConditional with transformed sub-terms.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        return new IntermediateConditional(
//            condition.methodT(),
//            trueBranch.methodT(),
//            falseBranch.methodT()
//        );
//    }
    /**
     * For conditionals, methodT() recursively calls methodT() on its sub-terms.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return A new IntermediateConditional with transformed sub-terms.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return new IntermediateConditional(
            condition.methodT(optimize),
            trueBranch.methodT(optimize),
            falseBranch.methodT(optimize)
        );
    }

    /**
     * Converts this IntermediateConditional to a CombinatorApplication representing the conditional logic.
     * T[if C then T else F] = ((T[C] T[T]) T[F]) (assuming Church Boolean style)
     * @return The equivalent Combinator term.
     */
    @Override
    public Combinator toCombinatorTerm() {
        Combinator translatedCondition = condition.toCombinatorTerm();
        Combinator translatedTrueBranch = trueBranch.toCombinatorTerm();
        Combinator translatedFalseBranch = falseBranch.toCombinatorTerm();
        return new CombinatorApplication(
            new CombinatorApplication(translatedCondition, translatedTrueBranch),
            translatedFalseBranch
        );
    }
}
