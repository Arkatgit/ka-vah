package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.CStarCombinator;
import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents the C* combinator: C* x y = y x (flip applied twice)
 * This is equivalent to C I where C is the standard C combinator.
 */
public class IntermediateCStarCombinator extends IntermediateCombinator {

    public IntermediateCStarCombinator() {
        // No specific fields, just represents the C* combinator
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "C*";
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return this;
    }

    @Override
    public Combinator toCombinatorTerm() {
        return new CStarCombinator();
    }
}