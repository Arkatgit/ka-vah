package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import java.util.HashSet;
import java.util.Set;

public class IntermediateNot extends IntermediateTerm {
    private IntermediateTerm operand;

    private static final int precedence = 25; // Precedence for 'not' (higher than binary ops)

    public IntermediateNot(IntermediateTerm operand) {
        this.operand = operand;
    }

    public IntermediateTerm getOperand() {
        return operand;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "not " + operand.toStringPrec(precedence);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Set<String> getFreeVariables() {
        return operand.getFreeVariables();
    }

//    /**
//     * For unary logical operations, methodT() recursively calls methodT() on its operand.
//     * @return A new IntermediateNot with transformed operand.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        return new IntermediateNot(operand.methodT());
//    }
    /**
     * For unary logical operations, methodT() recursively calls methodT() on its operand.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return A new IntermediateNot with transformed operand.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        return new IntermediateNot(operand.methodT(optimize));
    }

    /**
     * Converts this IntermediateNot to a CombinatorApplication representing the operation.
     * T[not O] = (not T[O])
     * @return The equivalent Combinator term.
     */
    @Override
    public Combinator toCombinatorTerm() {
        Combinator notOp = new CombinatorConstant("not");
        Combinator translatedOperand = operand.toCombinatorTerm();
        return new CombinatorApplication(notOp, translatedOperand);
    }
}
