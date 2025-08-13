package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateApplication;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateNot;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.parser.Term;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Map;
import java.util.Set;

public class Not extends Term {
    private Term operand;
    public static final int precedence = 4;
    public Not(Term op){
        operand = op;
    }

    public Term getOperand() {
        return operand;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return  " not " + operand.toStringPrec(prec);
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        // Type check the operand, passing down the Unifier
        operand.type(env, unifier);
        Type operandType = operand.getType();

        Map<TVar, Type> substitution = unifier.unify(operandType , new Constant("Bool"));
        if (substitution == null)
        {
            throw new TypeError("NOT operand must be boolean (found " + operandType + ")");
        }

        return new Constant("Bool");
    }
    @Override
    public Term eval(Map<String, Term> env) {
        Term evaluatedOperand = operand.eval(env);
        if (evaluatedOperand instanceof BooleanLiteral) {
            return new BooleanLiteral(!((BooleanLiteral) evaluatedOperand).getValue());
        }
        return new Not(evaluatedOperand); // Return partially evaluated
    }

    @Override
    public Term substitute(String varName, Term value) {
        return new Not(operand.substitute(varName, value));
    }

    @Override
    public Set<String> getFreeVariables() {
        return operand.getFreeVariables();
    }

    @Override
    public Combinator translate() {
        // Translate 'not' to an application of the 'not' combinator
        // T[not Op] = (not T[Op])
        Combinator notOp = new CombinatorConstant("not"); // Represents the 'not' operator
        Combinator translatedOperand = operand.translate();
        return new CombinatorApplication(notOp, translatedOperand);
    }

//    /**
//     * Converts this Not to an IntermediateNot.
//     * This is the first step in the two-phase translation to combinators.
//     * @return The equivalent IntermediateNot.
//     */
//    @Override
//    public IntermediateTerm toIntermediateTerm() {
//        // Recursively convert the operand
//        return new IntermediateNot(operand.toIntermediateTerm());
//    }
    /**
     * Converts this Not to an IntermediateApplication.
     * T[not O] is represented as (not O) in the intermediate form.
     * @return The equivalent IntermediateApplication.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        // Represent 'not O' as an application: (not O)
        IntermediateConstant notOp = new IntermediateConstant("not");
        IntermediateTerm translatedOperand = operand.toIntermediateTerm();

        return new IntermediateApplication(notOp, translatedOperand);
    }

}
