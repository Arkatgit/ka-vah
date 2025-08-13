package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import java.util.HashSet;
import java.util.Set;

public class IntermediateOr extends IntermediateTerm {
    private IntermediateTerm left;
    private IntermediateTerm right;

    private static final int precedence = 15; // Precedence for 'or'

    public IntermediateOr(IntermediateTerm left, IntermediateTerm right) {
        this.left = left;
        this.right = right;
    }

    public IntermediateTerm getLeft() {
        return left;
    }

    public IntermediateTerm getRight() {
        return right;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = left.toStringPrec(precedence) + " or " + right.toStringPrec(precedence);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.addAll(left.getFreeVariables());
        freeVars.addAll(right.getFreeVariables());
        return freeVars;
    }

//    /**
//     * For logical operations, methodT() recursively calls methodT() on its operands.
//     * @return A new IntermediateOr with transformed operands.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        return new IntermediateOr(left.methodT(), right.methodT());
//    }
    /**
     * For logical operations, methodT() recursively calls methodT() on its operands.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return A new IntermediateOr with transformed operands.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return new IntermediateOr(left.methodT(optimize), right.methodT(optimize));
    }

    /**
     * Converts this IntermediateOr to a CombinatorApplication representing the operation.
     * T[L or R] = ((or T[L]) T[R])
     * @return The equivalent Combinator term.
     */
    @Override
    public Combinator toCombinatorTerm() {
        Combinator orOp = new CombinatorConstant("or");
        Combinator translatedLeft = left.toCombinatorTerm();
        Combinator translatedRight = right.toCombinatorTerm();
        return new CombinatorApplication(new CombinatorApplication(orOp, translatedLeft), translatedRight);
    }
}
