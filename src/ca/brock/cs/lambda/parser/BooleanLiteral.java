package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BooleanLiteral extends Term {
        private boolean value;

        public BooleanLiteral(boolean v)
        {
            value = v;
        }

        public boolean getValue()
        {
            return value;
        }
        @Override
        public String toStringPrec(int prec)
        {
           return value? "True" : "False";
        }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        return new Constant("Bool");
    }

    @Override
    public Term eval(Map<String, Term> env) {
        return this; // Boolean literals evaluate to themselves
    }

    @Override
    public Term substitute(String varName, Term value) {
        return this; // Boolean literals don't contain variables to substitute
    }
    @Override
    public Set<String> getFreeVariables() {
        return new HashSet<>(); // Literals have no free variables
    }

    @Override
    public Combinator translate() {
        // Translate boolean literal to a CombinatorConstant
        return new CombinatorConstant(value);
    }

    /**
     * Converts this BooleanLiteral to an IntermediateConstant.
     * @return The equivalent IntermediateConstant.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        return new IntermediateConstant(value);
    }
}
