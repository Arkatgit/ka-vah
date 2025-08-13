package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateApplication;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateOr;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Or extends Term {
    private Term left;
    private Term right;
    public static final int precedence = 5;
    public Or(Term l, Term r)
    {
        left = l;
        right = r;
    }
    public Term getLeft() {
        return left;
    }

    public Term getRight() {
        return right;
    }
    @Override
    public String toStringPrec(int prec)
    {
        return left.toStringPrec(prec) + " or " + right.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        // Type check both operands
        left.type(env, unifier);
        right.type(env, unifier);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<TVar, Type> substitution = unifier.unify(leftType , new Constant("Bool"));
        if (substitution == null)
        {
            throw new TypeError("Left operand of OR must be an boolean, but got: " + leftType);
        }

        substitution = unifier.unify( rightType , new Constant("Bool"));

        if (substitution == null)
        {
            throw new TypeError("Right operand of OR must be an boolean, but got: " + rightType);
        }

        return new Constant("Bool");
    }
    @Override
    public Term eval(Map<String, Term> env) {
        Term evaluatedLeft = left.eval(env);
        if (evaluatedLeft instanceof BooleanLiteral) {
            if (((BooleanLiteral) evaluatedLeft).getValue()) {
                return new BooleanLiteral(true); // Short-circuiting for true
            } else {
                Term evaluatedRight = right.eval(env);
                if (evaluatedRight instanceof BooleanLiteral) {
                    return new BooleanLiteral(((BooleanLiteral) evaluatedLeft).getValue() || ((BooleanLiteral) evaluatedRight).getValue());
                }
                return new Or(new BooleanLiteral(false), evaluatedRight); // Partially evaluated
            }
        }
        return new Or(evaluatedLeft, right); // Partially evaluated if left is not a boolean literal
    }

    @Override
    public Term substitute(String varName, Term value) {
        return new Or(left.substitute(varName, value), right.substitute(varName, value));
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.addAll(left.getFreeVariables());
        freeVars.addAll(right.getFreeVariables());
        return freeVars;
    }

    @Override
    public Combinator translate() {
        // Translate 'or' to an application of the 'or' combinator
        // T[L or R] = (or T[L]) T[R]
        Combinator orOp = new CombinatorConstant("or"); // Represents the 'or' operator
        Combinator translatedLeft = left.translate();
        Combinator translatedRight = right.translate();
        return new CombinatorApplication(new CombinatorApplication(orOp, translatedLeft), translatedRight);
    }

//    /**
//     * Converts this Or to an IntermediateOr.
//     * This is the first step in the two-phase translation to combinators.
//     * @return The equivalent IntermediateOr.
//     */
//    @Override
//    public IntermediateTerm toIntermediateTerm() {
//        // Recursively convert left and right operands
//        return new IntermediateOr(left.toIntermediateTerm(), right.toIntermediateTerm());
//    }
    /**
     * Converts this Or to an IntermediateApplication.
     * T[L or R] is represented as ((or L) R) in the intermediate form.
     * @return The equivalent IntermediateApplication.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        // Represent 'L or R' as an application: ((or L) R)
        IntermediateConstant orOp = new IntermediateConstant("or");
        IntermediateTerm translatedLeft = left.toIntermediateTerm();
        IntermediateTerm translatedRight = right.toIntermediateTerm();

        // First application: (or L)
        IntermediateApplication partialApplication = new IntermediateApplication(orOp, translatedLeft);
        // Second application: ((or L) R)
        return new IntermediateApplication(partialApplication, translatedRight);
    }

}
