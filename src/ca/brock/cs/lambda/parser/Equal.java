package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateApplication;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateEqual;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Equal extends Term {
    private Term left;
    private Term right;

    public static final int precedence = 10;

    public Equal(Term l, Term r)
    {
        left = l;
        right = r;
    }

    public Term getLeft(){
        return left;
    }
    public Term getRight(){
        return right;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return left.toStringPrec(prec) + " = " + right.toStringPrec(prec);
    }
    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        left.type(env, unifier);
        right.type(env, unifier);

        Type leftType = left.getType();
        Type rightType = right.getType();

        Map<TVar, Type> sub = unifier.unify(leftType, rightType); // Use the provided unifier
        if (sub == null) {
            throw new RuntimeException("Comparison operands must have same type");
        }

        return new Constant("Bool");
    }

    @Override
    public Term eval(Map<String, Term> env) {
        Term evaluatedLeft = left.eval(env);
        Term evaluatedRight = right.eval(env);

        if (evaluatedLeft instanceof IntegerLiteral && evaluatedRight instanceof IntegerLiteral) {
            return new BooleanLiteral(((IntegerLiteral) evaluatedLeft).getValue() == ((IntegerLiteral) evaluatedRight).getValue());
        } else if (evaluatedLeft instanceof BooleanLiteral && evaluatedRight instanceof BooleanLiteral) {
            return new BooleanLiteral(((BooleanLiteral) evaluatedLeft).getValue() == ((BooleanLiteral) evaluatedRight).getValue());
        }
        return new Equal(evaluatedLeft, evaluatedRight); // Partially evaluated
    }

    @Override
    public Term substitute(String varName, Term value) {
        return new Equal(left.substitute(varName, value), right.substitute(varName, value));
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
        // Translate 'equal' to an application of the '=' combinator
        // T[L = R] = (= T[L]) T[R]
        Combinator equalOp = new CombinatorConstant("="); // Represents the '=' operator
        Combinator translatedLeft = left.translate();
        Combinator translatedRight = right.translate();
        return new CombinatorApplication(new CombinatorApplication(equalOp, translatedLeft), translatedRight);
    }

//    /**
//     * Converts this Equal to an IntermediateEqual.
//     * This is the first step in the two-phase translation to combinators.
//     * @return The equivalent IntermediateEqual.
//     */
//    @Override
//    public IntermediateTerm toIntermediateTerm() {
//        // Recursively convert left and right operands
//        return new IntermediateEqual(left.toIntermediateTerm(), right.toIntermediateTerm());
//    }
    /**
     * Converts this Equal to an IntermediateApplication.
     * T[L = R] is represented as (( = L) R) in the intermediate form.
     * @return The equivalent IntermediateApplication.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        // Represent 'L = R' as an application: (( = L) R)
        IntermediateConstant equalOp = new IntermediateConstant("=");
        IntermediateTerm translatedLeft = left.toIntermediateTerm();
        IntermediateTerm translatedRight = right.toIntermediateTerm();

        // First application: (= L)
        IntermediateApplication partialApplication = new IntermediateApplication(equalOp, translatedLeft);
        // Second application: (( = L) R)
        return new IntermediateApplication(partialApplication, translatedRight);
    }
}
