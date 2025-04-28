package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Type;

import java.util.HashMap;
import java.util.Map;

public abstract class Term {
    protected Type type;

    public abstract void type(Map<String, Type> env);

    public abstract String toStringPrec(int prec);

    public String toString() {
        return toStringPrec(0);
    }

    public Type getType() {
        if (type == null) {
            type(new HashMap<>());
        }
        return type;
    }

}
