package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorVariable;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.intermediate.IntermediateVariable;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Variable extends Term {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toStringPrec(int prec) {
        return name;
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        if (!env.containsKey(name)) {
            // For free variables, create a fresh type variable
            TVar fresh = TVar.fresh();
            env.put(name, fresh);
            return fresh;
        }
        return env.get(name);
    }

    @Override
    public Term eval(Map<String, Term> env) {
        // If the variable is in the environment, return its value. Otherwise, it's a free variable.
        return env.getOrDefault(name, this);
    }

    @Override
    public Term substitute(String varName, Term value) {
        if (name.equals(varName)) {
            return value; // If this variable is the one we're substituting, replace it with 'value'
        }
        return this; // Otherwise, return this variable unchanged
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.add(name); // A variable itself is a free variable
        return freeVars;
    }

    @Override
    public Combinator translate() {
        // Translate a variable to a CombinatorVariable
        return new CombinatorVariable(name);
    }

    /**
     * Converts this Variable to an IntermediateVariable.
     * @return The equivalent IntermediateVariable.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        return new IntermediateVariable(name);
    }

}