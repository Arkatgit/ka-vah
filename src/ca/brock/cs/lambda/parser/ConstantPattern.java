package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents a constant pattern (True, False, or integer literals).
 */
public class ConstantPattern extends Pattern {
    private final Term constant;

    public ConstantPattern(Term constant) {
        this.constant = constant;
    }

    public Term getConstant() {
        return constant;
    }

    @Override
    public Map<String, Term> match(Term term) {
        // A constant pattern only matches if the term is equal to the constant
        if (constant.equals(term)) {
            return Collections.emptyMap(); // No variables bound
        }
        return null; // No match
    }

    @Override
    public Set<String> getBoundVariables() {
        return Collections.emptySet(); // Constants don't bind variables
    }

    @Override
    public Type computeType(Map<String, Type> env, Unifier unifier) {
        // The type of a constant pattern is the type of the constant
        constant.type(env, unifier);
        return constant.getType();
    }

    @Override
    public String toString()
    {
        return constant.toString();
    }




}