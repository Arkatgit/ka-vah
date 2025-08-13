package ca.brock.cs.lambda.combinators;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class CombinatorConstant extends Combinator {
    private Object value; // Can hold String (for operators), Boolean, Integer

    public CombinatorConstant(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public boolean isInteger() {
        return value instanceof Integer;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    @Override
    public String toStringPrec(int prec) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "True" : "False";
        } else if (value instanceof Integer) {
            return String.valueOf(value);
        }
        return value.toString();
    }

    @Override
    public Combinator eval(Map<String, Combinator> env) {
        return this; // Constants evaluate to themselves
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Constants have no free variables
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        return this; // Constants don't contain variables to substitute
    }
}