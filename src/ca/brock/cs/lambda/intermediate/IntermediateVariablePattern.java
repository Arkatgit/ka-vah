package ca.brock.cs.lambda.intermediate;

import java.util.Collections;
import java.util.Set;

public class IntermediateVariablePattern extends IntermediatePattern {
    private final String name;

    public IntermediateVariablePattern(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Set<String> getBoundVariables() {
        return Collections.singleton(name);
    }

    @Override
    public String toString() {
        return name;
    }
}