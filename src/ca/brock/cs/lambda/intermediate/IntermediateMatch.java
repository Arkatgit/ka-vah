package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.MatchCombinator;

import java.util.List;
import java.util.Set;

public class IntermediateMatch extends IntermediateTerm {
    public static class Case {
        private final IntermediatePattern pattern;
        private final IntermediateTerm result;

        public Case(IntermediatePattern pattern, IntermediateTerm result) {
            this.pattern = pattern;
            this.result = result;
        }

        public IntermediatePattern getPattern() { return pattern; }
        public IntermediateTerm getResult() { return result; }
    }

    private final IntermediateTerm input;
    private final List<Case> cases;

    public IntermediateMatch(IntermediateTerm input, List<Case> cases) {
        this.input = input;
        this.cases = cases;
    }

    public IntermediateTerm getInput() { return input; }
    public List<Case> getCases() { return cases; }

    @Override
    public String toStringPrec(int prec) {
        StringBuilder sb = new StringBuilder();
        sb.append("match ").append(input.toStringPrec(0)).append(" with ");
        for (Case c : cases) {
            sb.append(c.pattern.toString()).append(" -> ").append(c.result.toStringPrec(0)).append(" | ");
        }
        if (!cases.isEmpty()) {
            sb.setLength(sb.length() - 3);
        }
        sb.append(" end");
        return sb.toString();
    }

    @Override
//    public Set<String> getFreeVariables() {
//        Set<String> freeVars = input.getFreeVariables();
//        for (Case c : cases) {
//            freeVars.addAll(c.result.getFreeVariables());
//            // Remove pattern-bound variables
//            freeVars.removeAll(c.pattern.getBoundVariables());
//        }
//        return freeVars;
//    }
    public Set<String> getFreeVariables() {
        // THE FIX: Wrap the result in a new HashSet so it is mutable
        Set<String> freeVars = new java.util.HashSet<>(input.getFreeVariables());

        for (Case c : cases) {
            freeVars.addAll(c.result.getFreeVariables());
            // Remove pattern-bound variables
            freeVars.removeAll(c.pattern.getBoundVariables());
        }
        return freeVars;
    }

    @Override
//    public IntermediateTerm methodT(boolean optimize) {
//        List<Case> transformedCases = new java.util.ArrayList<>();
//        for (Case c : cases) {
//            transformedCases.add(new Case(
//                c.pattern, // Patterns don't get transformed by methodT
//                c.result.methodT(optimize)
//            ));
//        }
//        return new IntermediateMatch(input.methodT(optimize), transformedCases);
//    }
    public IntermediateTerm methodT(boolean optimize) {
        List<Case> transformedCases = new java.util.ArrayList<>();
        for (Case c : cases) {
            IntermediateTerm branchBody = c.getResult();

            // 1. Get the variables bound by this case's pattern
            java.util.List<String> boundVars = new java.util.ArrayList<>(c.getPattern().getBoundVariables());

            // 2. Wrap the branch body in lambda abstractions for each bound variable
            for (int i = boundVars.size() - 1; i >= 0; i--) {
                branchBody = new IntermediateAbstraction(boundVars.get(i), branchBody);
            }

            // 3. THE FIX: Recursively compile the newly created abstractions to combinators
            branchBody = branchBody.methodT(optimize);

            // 4. Store the compiled branch back into a new Case
            transformedCases.add(new Case(c.getPattern(), branchBody));
        }
        return new IntermediateMatch(input.methodT(optimize), transformedCases);
    }

    @Override
    public Combinator toCombinatorTerm() {
        // Convert to MatchCombinator
        List<MatchCombinator.Case> combinatorCases = new java.util.ArrayList<>();
        for (Case c : cases) {
            // Convert intermediate pattern to combinator pattern
            Combinator combinatorPattern = convertPatternToCombinator(c.pattern);
            Combinator combinatorResult = c.result.toCombinatorTerm();
            combinatorCases.add(new MatchCombinator.Case(combinatorPattern, combinatorResult));
        }
        return new MatchCombinator(input.toCombinatorTerm(), combinatorCases);
    }

    private Combinator convertPatternToCombinator(IntermediatePattern pattern) {
        if (pattern instanceof IntermediateVariablePattern) {
            IntermediateVariablePattern varPattern = (IntermediateVariablePattern) pattern;
            return new ca.brock.cs.lambda.combinators.CombinatorVariable(varPattern.getName());
        } else if (pattern instanceof IntermediateConstructorPattern) {
            IntermediateConstructorPattern constrPattern = (IntermediateConstructorPattern) pattern;
            List<IntermediatePattern> subPatterns = constrPattern.getPatterns();

            // Build the constructor application: Constructor arg1 arg2 ...
            Combinator constructor = new ca.brock.cs.lambda.combinators.CombinatorConstant(constrPattern.getConstructorName());

            // Apply constructor to each argument pattern
            for (IntermediatePattern subPattern : subPatterns) {
                Combinator argPattern = convertPatternToCombinator(subPattern);
                constructor = new ca.brock.cs.lambda.combinators.CombinatorApplication(constructor, argPattern);
            }

            return constructor;
        } else if (pattern instanceof IntermediateConstantPattern) {
            IntermediateConstantPattern constPattern = (IntermediateConstantPattern) pattern;
            return new ca.brock.cs.lambda.combinators.CombinatorConstant(constPattern.getValue());
        } else {
            throw new IllegalArgumentException("Unknown pattern type: " + pattern.getClass().getSimpleName());
        }
    }
}