package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateAnd;
import ca.brock.cs.lambda.intermediate.IntermediateApplication;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class And extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 5;
    public And(Term l, Term r)
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
        return left.toStringPrec(prec) + " and " + right.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        left.type(env, unifier);
        right.type(env, unifier);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<TVar, Type> substitution = unifier.unify(leftType , new Constant("Bool"));
        if (substitution == null)
        {
            throw new TypeError("Left operand of AND must be an boolean, but got: " + leftType);
        }

        substitution = unifier.unify( rightType , new Constant("Bool"));

        if (substitution == null)
        {
            throw new TypeError("Right operand of AND must be an boolean, but got: " + rightType);
        }

        return new Constant("Bool");
    }
    @Override
    public Term eval(Map<String, Term> env) {
        Term evaluatedLeft = left.eval(env);
        if (evaluatedLeft instanceof BooleanLiteral) {
            if (!((BooleanLiteral) evaluatedLeft).getValue()) {
                return new BooleanLiteral(false); // Short-circuiting for false
            } else {
                Term evaluatedRight = right.eval(env);
                if (evaluatedRight instanceof BooleanLiteral) {
                    return new BooleanLiteral(((BooleanLiteral) evaluatedLeft).getValue() && ((BooleanLiteral) evaluatedRight).getValue());
                }
                return new And(new BooleanLiteral(true), evaluatedRight); // Partially evaluated
            }
        }
        return new And(evaluatedLeft, right); // Partially evaluated if left is not a boolean literal
    }

    @Override
    public Term substitute(String varName, Term value) {
        return new And(left.substitute(varName, value), right.substitute(varName, value));
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
        // Translate 'and' to an application of the 'and' combinator
        // T[L and R] = (and T[L]) T[R]
        Combinator andOp = new CombinatorConstant("and"); // Represents the 'and' operator
        Combinator translatedLeft = left.translate();
        Combinator translatedRight = right.translate();
        return new CombinatorApplication(new CombinatorApplication(andOp, translatedLeft), translatedRight);
    }

//    /**
//     * Converts this And to an IntermediateAnd.
//     * This is the first step in the two-phase translation to combinators.
//     * @return The equivalent IntermediateAnd.
//     */
//    @Override
//    public IntermediateTerm toIntermediateTerm() {
//        // Recursively convert left and right operands
//        return new IntermediateAnd(left.toIntermediateTerm(), right.toIntermediateTerm());
//    }
    /**
     * Converts this And to an IntermediateApplication.
     * T[L and R] is represented as ((and L) R) in the intermediate form.
     * @return The equivalent IntermediateApplication.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        // Represent 'L and R' as an application: ((and L) R)
        IntermediateConstant andOp = new IntermediateConstant("and");
        IntermediateTerm translatedLeft = left.toIntermediateTerm();
        IntermediateTerm translatedRight = right.toIntermediateTerm();

        // First application: (and L)
        IntermediateApplication partialApplication = new IntermediateApplication(andOp, translatedLeft);
        // Second application: ((and L) R)
        return new IntermediateApplication(partialApplication, translatedRight);
    }


}
