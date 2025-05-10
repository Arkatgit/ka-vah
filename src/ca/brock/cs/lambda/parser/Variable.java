package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Map;

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

}