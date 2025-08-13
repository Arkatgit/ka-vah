package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.parser.Term;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IntegerLiteral extends Term {
    private int value;

    public IntegerLiteral(int v)
    {
        value = v;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toStringPrec(int prec) {
        return Integer.toString(value);
    }
    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        return new Constant("Int");
    }

    @Override
    public Term eval(Map<String, Term> env) {
        return this; // Integer literals evaluate to themselves
    }

    @Override
    public Term substitute(String varName, Term value) {
        return this; // Integer literals don't contain variables to substitute
    }

    @Override
    public Set<String> getFreeVariables() {
        return new HashSet<>(); // Literals have no free variables
    }

    /**
     * Translates this integer literal term into a combinator calculus term.
     * @return The equivalent Combinator term (a CombinatorConstant holding the integer value).
     */
    @Override
    public Combinator translate() {
        return new CombinatorConstant(value);
    }

    /**
     * Converts this IntegerLiteral to an IntermediateConstant.
     * @return The equivalent IntermediateConstant.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        return new IntermediateConstant(value);
    }
}
