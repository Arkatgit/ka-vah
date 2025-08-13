package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.FType;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Constant extends Term {
    private String value;
    private final InfixOperator operator;

    // Constructor for normal constants
    public Constant(String value) {
        this.value = value;
        this.operator = InfixOperator.fromSymbol(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toStringPrec(int prec) {
        if (operator != null) {
            return operator.getSymbol(); // Use operator symbol if it's an infix operator
        }
        return value;
    }

    public boolean isInfixOperator() {
        return operator != null;
    }

    public boolean canBeUsedAsSection() {
        return isInfixOperator() && operator.canBeUsedAsSection();
    }

    public int getArity() {
        return isInfixOperator() ? operator.getArity() : 0;
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        if (value.equals("True") || value.equals("False")) {
            return new ca.brock.cs.lambda.types.Constant("Bool");
        }

        switch (value) {
            case "+":
            case "-":
            case "*":
                return new FType(new ca.brock.cs.lambda.types.Constant("Int"), new FType(new ca.brock.cs.lambda.types.Constant("Int"), new ca.brock.cs.lambda.types.Constant("Int")));
            case "and":
            case "or":
                return new FType(new ca.brock.cs.lambda.types.Constant("Bool"), new FType(new ca.brock.cs.lambda.types.Constant("Bool"), new ca.brock.cs.lambda.types.Constant("Bool")));
            case "=":
            case "<=":
                TVar a = TVar.fresh();
                return new FType(a, new FType(a, new ca.brock.cs.lambda.types.Constant("Bool")));
            case "not":
                return new FType(new ca.brock.cs.lambda.types.Constant("Bool"), new ca.brock.cs.lambda.types.Constant("Bool"));
            default:
                return TVar.fresh();
        }
    }
    @Override
    public Term eval(Map<String, Term> env) {
        return this; // Constants evaluate to themselves
    }

    @Override
    public Term substitute(String varName, Term value) {
        return this; // Constants don't contain variables to substitute
    }
    @Override
    public Set<String> getFreeVariables() {
        return new HashSet<>(); // Constants have no free variables
    }

    @Override
    public Combinator translate() {
        // Translate a constant to a CombinatorConstant
        // Special case for operators that are constants (e.g., "+", "and")
        // They will be wrapped as CombinatorConstant containing their string representation.
        if (operator != null) {
            return new CombinatorConstant(operator.getSymbol());
        }
        return new CombinatorConstant(value);
    }

    /**
     * Converts this Constant to an IntermediateConstant.
     * @return The equivalent IntermediateConstant.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        return new IntermediateConstant(value);
    }

}