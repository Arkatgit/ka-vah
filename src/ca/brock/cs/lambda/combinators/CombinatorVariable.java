package ca.brock.cs.lambda.combinators;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CombinatorVariable extends Combinator {
    private String name;

    public CombinatorVariable(String name) {
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
    public Combinator eval(Map<String, Combinator> env) {
        // In pure combinator calculus, variables are usually not "evaluated" in the same way.
        // If they exist, they are placeholders or part of a larger term that will reduce.
        // If we allow an environment, it would be to substitute variables.
        return env.getOrDefault(name, this);
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.add(name);
        return freeVars;
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        if (name.equals(varName)) {
            return value;
        }
        return this;
    }
}