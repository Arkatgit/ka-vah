package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.logging.AppLogger;
import ca.brock.cs.lambda.types.AlgebraicDataType;
import ca.brock.cs.lambda.types.DefinedValue;
import ca.brock.cs.lambda.types.TApp;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a 'match' expression in the functional language.
 * A match expression consists of an input term and a list of cases.
 */
public class Match extends Term {
    public static class Case {
        private final Pattern pattern;
        private final Term result;

        public Case(Pattern pattern, Term result) {
            this.pattern = pattern;
            this.result = result;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public Term getResult() {
            return result;
        }
    }

    private final Term inputTerm;
    private final List<Case> cases;

    public Match(Term inputTerm, List<Case> cases) {
        this.inputTerm = inputTerm;
        this.cases = cases;
    }

    @Override
    public String toStringPrec(int prec) {
        StringBuilder sb = new StringBuilder();
        sb.append("match ").append(inputTerm.toString()).append(" with ");
        for (Case c : cases) {
            sb.append(c.getPattern().toString()).append(" -> ").append(c.getResult().toStringPrec(0));
        }
        sb.append(" end");
        return sb.toString();
    }

    private void applySubstitutionsToEnv(Map<String, Type> env, Map<TVar, Type> substitutions, Unifier unifier) {
        for (Map.Entry<String, Type> entry : env.entrySet()) {
            Type originalType = entry.getValue();
            Type substitutedType = unifier.applySubstitution(originalType, substitutions);
            env.put(entry.getKey(), substitutedType);
        }
    }

    private void applySubstitutionToEnv(Map<String, Type> env, Map<TVar, Type> substitution, Unifier unifier) {
        for (Map.Entry<String, Type> entry : env.entrySet()) {
            Type substituted = unifier.applySubstitution(entry.getValue(), substitution);
            env.put(entry.getKey(), substituted);
        }
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        if (cases.isEmpty()) {
            throw new TypeError("Match expression must have at least one case.");
        }

        // 1. Compute the type of the input term being matched
        Type inputType = inputTerm.computeType(env, unifier);
        AppLogger.info("DEBUG: Match input type: " + inputType);

        // 2. For each case, compute the pattern type and result type
        List<Type> resultTypes = new ArrayList<>();

        for (int i = 0; i < cases.size(); i++) {
            Case caseItem = cases.get(i);
            AppLogger.info("DEBUG: Trying pattern " + i + ": " + caseItem.getPattern());

            // Create a fresh environment for this case to avoid variable pollution
            Map<String, Type> caseEnv = new HashMap<>(env);

            // 3. Compute the pattern type - this will add pattern-bound variables to caseEnv
            Type patternType = caseItem.getPattern().computeType(caseEnv, unifier);
            AppLogger.info("DEBUG: Pattern type: " + patternType);
            AppLogger.info("DEBUG: About to unify pattern with input");

            // 4. Unify the pattern type with the input type
            Map<TVar, Type> patternSub = unifier.unify(patternType, inputType);
            AppLogger.info("DEBUG: Unification result: " + patternSub);

            if (patternSub == null) {
                AppLogger.info("DEBUG: Pattern " + i + " failed unification");
               throw new TypeError("Type mismatch between the pattern : " + caseItem.getPattern() + " and input : "
                   + inputTerm + " for the match expression");
              //  continue; // Try next pattern if this one doesn't match
            }
            AppLogger.info("DEBUG: Pattern " + i + " succeeded unification");

            // 5. Apply the unifier substitutions to the environment
            for (Map.Entry<String, Type> entry : caseEnv.entrySet()) {
                Type substitutedType = unifier.applySubstitution(entry.getValue(), unifier.getEnv());
                caseEnv.put(entry.getKey(), substitutedType);
            }

            // 6. Compute the result type in the updated environment (with pattern-bound variables)
            Type caseResultType = caseItem.getResult().computeType(caseEnv, unifier);
            AppLogger.info("DEBUG: Result type for pattern " + i + ": " + caseResultType);
            resultTypes.add(caseResultType);
        }

        if (resultTypes.isEmpty()) {
            throw new TypeError("No pattern in match expression matches input type: " + inputType);
        }

        // 7. All result types must be the same
        Type finalResultType = resultTypes.get(0);
        AppLogger.info("DEBUG: Initial result type: " + finalResultType);

        for (int i = 1; i < resultTypes.size(); i++) {
            AppLogger.info("DEBUG: Unifying result type " + finalResultType + " with " + resultTypes.get(i));
            Map<TVar, Type> resultSub = unifier.unify(finalResultType, resultTypes.get(i));
            AppLogger.info("DEBUG: Result unification result: " + resultSub);

            if (resultSub == null) {
                throw new TypeError("Result types are incompatible: " +
                    finalResultType + " vs " + resultTypes.get(i) + " for pattern(s)");
            }
            finalResultType = unifier.applySubstitution(finalResultType, resultSub);
            AppLogger.info("DEBUG: Unified result type after pattern " + i + ": " + finalResultType);
        }

        // 8. Apply final substitutions before returning
        Type resolvedResultType = unifier.applySubstitution(finalResultType, unifier.getEnv());
        AppLogger.info("DEBUG: Final result type of match: " + resolvedResultType);

        Type input = unifier.applySubstitution(inputType, unifier.getEnv());
        // Proper exhaustiveness check
        if (!isExhaustive(input)) {
            String typeName = extractTypeName(input.toString());
            List<String> expectedConstructors = ConstructorRegistry.getConstructors(typeName);
            Set<String> actualConstructors = cases.stream()
                .filter(c -> c.getPattern() instanceof ConstructorPattern)
                .map(c -> ((ConstructorPattern) c.getPattern()).getName())
                .collect(Collectors.toSet());

            List<String> missing = expectedConstructors.stream()
                .filter(c -> !actualConstructors.contains(c))
                .collect(Collectors.toList());

            throw new TypeError("Non-exhaustive patterns. Missing cases for: " + String.join(", ", missing));
        }


        return resolvedResultType;
    }


    public boolean isExhaustive(Type inputType) {
        if (cases.isEmpty()) {
            return false;
        }

        // Variable patterns are always exhaustive
        if (cases.stream().anyMatch(c -> c.getPattern() instanceof VariablePattern)) {
            return true;
        }

        // Get the type name
        String typeName = extractTypeName(inputType.toString());
        if (typeName == null) {
            return true; // Unknown type, be conservative
        }

        // Get expected constructors for this type
        List<String> expectedConstructors = ConstructorRegistry.getConstructors(typeName);
        if (expectedConstructors.isEmpty()) {
            return true; // No constructor info, be conservative
        }

        // Get actual constructor patterns
        Set<String> actualConstructors = cases.stream()
            .filter(c -> c.getPattern() instanceof ConstructorPattern)
            .map(c -> ((ConstructorPattern) c.getPattern()).getName())
            .collect(Collectors.toSet());

        // Check if all expected constructors are covered
        return actualConstructors.containsAll(expectedConstructors);
    }

    private String extractTypeName(String typeString) {
        // Extract the base type name from type expressions like:
        // "list a", "(list a)", "Maybe Int", etc.

        // Remove parentheses
        String cleaned = typeString.replaceAll("[()]", "");

        // Take the first word (type constructor name)
        String[] parts = cleaned.split("\\s+");
        return parts.length > 0 ? parts[0] : null;
    }

    @Override
    public Term eval(Map<String, Term> env) {
        Term evaluatedInput = inputTerm.eval(env);

        for (Case c : cases) {
            // Attempt to match the evaluated input against the pattern
            Map<String, Term> bindings = c.getPattern().match(evaluatedInput);
            if (bindings != null) {
                // A match was found. Substitute the bound variables in the result term and evaluate.
                Term resultTerm = c.getResult();
                for (Map.Entry<String, Term> entry : bindings.entrySet()) {
                    resultTerm = resultTerm.substitute(entry.getKey(), entry.getValue());
                }
                return resultTerm.eval(env);
            }
        }
        // If no match is found, it's a runtime error
        throw new RuntimeException("Match expression failed to find a matching pattern for " + evaluatedInput.toString());
    }

    @Override
    public Term substitute(String varName, Term value) {
        Term substitutedInput = inputTerm.substitute(varName, value);
        List<Case> substitutedCases = cases.stream()
            .map(c -> new Case(c.getPattern(), c.getResult().substitute(varName, value)))
            .collect(Collectors.toList());
        return new Match(substitutedInput, substitutedCases);
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = inputTerm.getFreeVariables();
        for (Case c : cases) {
            // Get free variables from the result term, but remove any variables bound by the pattern
            Set<String> caseFreeVars = c.getResult().getFreeVariables();
            caseFreeVars.removeAll(c.getPattern().getBoundVariables());
            freeVars.addAll(caseFreeVars);
        }
        return freeVars;
    }

    @Override
    public Combinator translate() {
        // T[M N] = T[M] T[N]
        return null;
    }

    /**
     * Converts this lambda calculus term into an IntermediateTerm.
     * This is the first step in the two-phase translation to combinators.
     *
     * @return The equivalent IntermediateTerm.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        return null;
    }
}
