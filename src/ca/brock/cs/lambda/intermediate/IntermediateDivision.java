package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.parser.Division;

import java.util.HashSet;
import java.util.Set;

/**
 * Intermediate representation of division operation.
 */
public class IntermediateDivision extends IntermediateTerm {
    private final IntermediateTerm left;
    private final IntermediateTerm right;

    public IntermediateDivision(IntermediateTerm left, IntermediateTerm right) {
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
    public String toString() {
        return "(" + left + " / " + right + ")";
    }

    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return new IntermediateDivision(left.methodT(optimize), right.methodT(optimize));
    }

    /**
     * Converts this IntermediateMultiplication to a CombinatorApplication representing the operation.
     * T[L * R] = ((* T[L]) T[R])
     * @return The equivalent Combinator term.
     */
    @Override
    public Combinator toCombinatorTerm() {
        Combinator mulOp = new CombinatorConstant("/");
        Combinator translatedLeft = left.toCombinatorTerm();
        Combinator translatedRight = right.toCombinatorTerm();
        return new CombinatorApplication(new CombinatorApplication(mulOp, translatedLeft), translatedRight);
    }


    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.addAll(left.getFreeVariables());
        freeVars.addAll(right.getFreeVariables());
        return freeVars;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = left.toStringPrec(Division.precedence) + " / " + right.toStringPrec(Division.precedence + 1);
        if (prec > Division.precedence) {
            result = "(" + result + ")";
        }
        return result;
    }
}