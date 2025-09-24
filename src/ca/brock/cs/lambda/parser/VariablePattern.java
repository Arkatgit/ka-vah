package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a variable pattern, which matches any term and binds it to a variable.
 */
public class VariablePattern extends Pattern {
    private final String name;

    public VariablePattern(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Map<String, Term> match(Term term) {
        Map<String, Term> bindings = new HashMap<>();
        bindings.put(name, term);
        return bindings;
    }

    @Override
    public Set<String> getBoundVariables() {
        return Collections.singleton(name);
    }

    @Override
    public Type computeType(Map<String, Type> env, Unifier unifier) {
        //TVar freshType = unifier.fresh();
        TVar freshType = TVar.fresh();
        env.put(name, freshType);
        return freshType;
    }

    @Override
    public String toString() {
        return name;
    }
}
