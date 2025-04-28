package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;

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
    public void type(Map<String, Type> env) {
        if (!env.containsKey(name)) {
            throw new TypeError("Unbound variable: " + name);
        }
        type = env.get(name);
    }

}