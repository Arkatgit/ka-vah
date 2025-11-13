package ca.brock.cs.lambda.combinators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a match expression in combinator calculus.
 * MatchCombinator takes an input and a list of cases, and evaluates to the first matching case.
 */
public class MatchCombinator extends Combinator {
    private final Combinator input;
    private final List<Case> cases;

    public static class Case {
        private final Combinator pattern;
        private final Combinator result;

        public Case(Combinator pattern, Combinator result) {
            this.pattern = pattern;
            this.result = result;
        }

        public Combinator getPattern() { return pattern; }
        public Combinator getResult() { return result; }
    }

    public MatchCombinator(Combinator input, List<Case> cases) {
        this.input = input;
        this.cases = cases;
    }

    public Combinator getInput() { return input; }
    public List<Case> getCases() { return cases; }

    @Override
    public String toStringPrec(int prec) {
        StringBuilder sb = new StringBuilder();
        sb.append("match ").append(input.toStringPrec(0)).append(" with ");
        for (Case c : cases) {
            sb.append(c.pattern.toStringPrec(0)).append(" -> ").append(c.result.toStringPrec(0)).append(" | ");
        }
        if (!cases.isEmpty()) {
            sb.setLength(sb.length() - 3); // Remove last " | "
        }
        sb.append(" end");
        return sb.toString();
    }

    @Override
    public Combinator eval(Map<String, Combinator> env) {
        // Evaluate the input first
        Combinator evaluatedInput = input.eval(env);

        // Try each case in order
        for (Case c : cases) {
            // For now, we'll do simple equality matching
            // In a full implementation, this would handle constructor patterns, variable binding, etc.
            Combinator patternEval = c.pattern.eval(env);

            // Simple equality check - this is a placeholder for proper pattern matching
            if (matches(evaluatedInput, patternEval, env)) {
                return c.result.eval(env);
            }
        }

        // No match found - this should ideally throw an error, but for now return the original
        return this;
    }

    /**
     * Simple matching logic - in a real implementation, this would handle:
     * - Constructor patterns
     * - Variable binding
     * - Nested patterns
     * - etc.
     */
    private boolean matches(Combinator input, Combinator pattern, Map<String, Combinator> bindings) {
        // Case 1: Variable pattern - matches anything and binds the variable
        if (pattern instanceof CombinatorVariable) {
            String varName = ((CombinatorVariable) pattern).getName();
            bindings.put(varName, input);
            return true;
        }

        // Case 2: Constant patterns
        if (pattern instanceof CombinatorConstant && input instanceof CombinatorConstant) {
            CombinatorConstant patternConst = (CombinatorConstant) pattern;
            CombinatorConstant inputConst = (CombinatorConstant) input;
            return patternConst.getValue().equals(inputConst.getValue());
        }

        // Case 3: Constructor patterns with arguments
        if (pattern instanceof CombinatorApplication && input instanceof CombinatorApplication) {
            CombinatorApplication patternApp = (CombinatorApplication) pattern;
            CombinatorApplication inputApp = (CombinatorApplication) input;

            // First, try to match the constructors (functions)
            if (matches(inputApp.getFunction(), patternApp.getFunction(), bindings)) {
                // Then match the arguments
                return matches(inputApp.getArgument(), patternApp.getArgument(), bindings);
            }
        }

        // Case 4: Simple constructor match
        if (pattern instanceof CombinatorConstant && input instanceof CombinatorConstant) {
            CombinatorConstant patternConst = (CombinatorConstant) pattern;
            CombinatorConstant inputConst = (CombinatorConstant) input;

            if (patternConst.getValue() instanceof String && inputConst.getValue() instanceof String) {
                return patternConst.getValue().equals(inputConst.getValue());
            }
        }

        return false;
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = input.getFreeVariables();
        for (Case c : cases) {
            freeVars.addAll(c.pattern.getFreeVariables());
            freeVars.addAll(c.result.getFreeVariables());
        }
        return freeVars;
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        List<Case> substitutedCases = new ArrayList<>();
        for (Case c : cases) {
            substitutedCases.add(new Case(
                c.pattern.substitute(varName, value),
                c.result.substitute(varName, value)
            ));
        }
        return new MatchCombinator(input.substitute(varName, value), substitutedCases);
    }
}