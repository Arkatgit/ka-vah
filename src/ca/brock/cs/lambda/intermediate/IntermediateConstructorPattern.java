package ca.brock.cs.lambda.intermediate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntermediateConstructorPattern extends IntermediatePattern {
    private final String constructorName;
    private final List<IntermediatePattern> patterns;

    public IntermediateConstructorPattern(String constructorName, List<IntermediatePattern> patterns) {
        this.constructorName = constructorName;
        this.patterns = patterns;
    }

    public String getConstructorName() {
        return constructorName;
    }

    public List<IntermediatePattern> getPatterns() {
        return patterns;
    }

    @Override
    public Set<String> getBoundVariables() {
        Set<String> boundVars = new HashSet<>();
        for (IntermediatePattern pattern : patterns) {
            boundVars.addAll(pattern.getBoundVariables());
        }
        return boundVars;
    }

    @Override
    public String toString() {
        if (patterns.isEmpty()) {
            return constructorName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(constructorName);
        for (IntermediatePattern pattern : patterns) {
            sb.append(" ").append(pattern.toString());
        }
        return sb.toString();
    }
}