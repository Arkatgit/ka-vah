package ca.brock.cs.lambda.intermediate;

import java.util.Collections;
import java.util.Set;

public class IntermediateConstantPattern extends IntermediatePattern {
    private final Object value;

    public IntermediateConstantPattern(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Set<String> getBoundVariables() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "True" : "False";
        } else if (value instanceof Integer) {
            return String.valueOf(value);
        }
        return value.toString();
    }
}