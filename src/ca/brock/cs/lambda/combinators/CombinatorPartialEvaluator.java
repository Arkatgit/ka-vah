package ca.brock.cs.lambda.combinators;

import java.util.*;

/**
 * Performs partial evaluation on combinator terms while skipping recursion.
 */
public class CombinatorPartialEvaluator {

    /**
     * Partially evaluates a map of combinators.
     */
    public static Map<String, Combinator> partialEvaluateAll(Map<String, Combinator> combinatorMap) {
        Map<String, Combinator> result = new HashMap<>();
        for (Map.Entry<String, Combinator> entry : combinatorMap.entrySet()) {
            result.put(entry.getKey(), partialEval(entry.getValue(), combinatorMap));
        }
        return result;
    }

    /**
     * The core logic for partial evaluation.
     */
    public static Combinator partialEval(Combinator c, Map<String, Combinator> env) {
        // 1. Skip evaluation if it's a recursive function (contains Y combinator)
        if (isRecursive(c)) {
            return c;
        }

        // 2. If it's an application, try to evaluate it partially
        if (c instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) c;

            // Recursively partially evaluate function and argument
            Combinator func = partialEval(app.getFunction(), env);
            Combinator arg = partialEval(app.getArgument(), env);

            // --- CONSISTENCY FIX: Distribute arguments applied to a MatchCombinator ---
            if (func instanceof MatchCombinator) {
                MatchCombinator match = (MatchCombinator) func;

                // Distribute argument to the match input
                Combinator newInput = new CombinatorApplication(match.getInput(), arg);

                // Distribute argument to all branch results
                List<MatchCombinator.Case> newCases = new java.util.ArrayList<>();
                for (MatchCombinator.Case mc : match.getCases()) {
                    newCases.add(new MatchCombinator.Case(mc.getPattern(), new CombinatorApplication(mc.getResult(), arg)));
                }

                // Return the newly constructed match to continue evaluation
                return partialEval(new MatchCombinator(newInput, newCases), env);
            }

            Combinator newApp = new CombinatorApplication(func, arg);

            // Try to reduce the application if it's not a recursive call
            try {
                // We use an empty local env to ensure we only fold constants and basic combinators
                return newApp.eval(new HashMap<>());
            } catch (Exception e) {
                // If evaluation fails (e.g., missing arguments), return the partially reduced application
                return newApp;
            }
        }

        // =======================================================================
        // --- CONSISTENCY FIX: Resolve MatchCombinator when input is a Constructor ---
        // =======================================================================
        if (c instanceof MatchCombinator) {
            MatchCombinator match = (MatchCombinator) c;
            Combinator evaluatedInput = partialEval(match.getInput(), env);

            // Check if the evaluated input is a Constructor Application
            if (isConstructorApplication(evaluatedInput)) {
                Combinator branchBody = getMatchingBranch(match, evaluatedInput);
                if (branchBody != null) {
                    List<Combinator> constructorArgs = extractConstructorArguments(evaluatedInput);
                    Combinator result = branchBody;

                    // Apply the constructor arguments to the branch body
                    for (Combinator constrArg : constructorArgs) {
                        result = new CombinatorApplication(result, constrArg);
                    }
                    return partialEval(result, env);
                }
            }
            // If it's not a constructor yet, just return the match with evaluated input
            return new MatchCombinator(evaluatedInput, match.getCases());
        }

        // 3. Variables: look up in environment if they are not recursive
        if (c instanceof CombinatorVariable) {
            String name = ((CombinatorVariable) c).getName();
            if (env.containsKey(name)) {
                Combinator def = env.get(name);
                if (!isRecursive(def)) {
                    return partialEval(def, env);
                }
            }
        }

        return c;
    }

    // =======================================================================
    // HELPER METHODS FOR MATCH AND CONSTRUCTOR RESOLUTION
    // =======================================================================

    /**
     * Finds the base head of a combinator chain (usually a CombinatorConstant representing the constructor name).
     */
    private static Combinator getApplicationHead(Combinator c) {
        while (c instanceof CombinatorApplication) {
            c = ((CombinatorApplication) c).getFunction();
        }
        return c;
    }

    /**
     * Checks if an evaluated combinator represents a fully formed constructor application.
     */
    private static boolean isConstructorApplication(Combinator c) {
        Combinator head = getApplicationHead(c);
        // Constructors and literals are represented as CombinatorConstants
        return head instanceof CombinatorConstant;
    }

    /**
     * Extracts the arguments passed to a constructor application.
     * For example, for 'cons 1 tail', it traverses the applications and extracts [1, tail].
     */
    private static List<Combinator> extractConstructorArguments(Combinator c) {
        List<Combinator> args = new java.util.ArrayList<>();
        while (c instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) c;
            // Add to the front to maintain order: [arg1, arg2, ...]
            args.add(0, app.getArgument());
            c = app.getFunction();
        }
        return args;
    }

    /**
     * Finds the correct branch body in a MatchCombinator for a given evaluated constructor input.
     */
    private static Combinator getMatchingBranch(MatchCombinator match, Combinator evaluatedInput) {
        Combinator inputHead = getApplicationHead(evaluatedInput);

        if (inputHead instanceof CombinatorConstant) {
            Object inputVal = ((CombinatorConstant) inputHead).getValue();

            for (MatchCombinator.Case matchCase : match.getCases()) {
                Combinator patternHead = getApplicationHead(matchCase.getPattern());

                if (patternHead instanceof CombinatorConstant) {
                    Object patternVal = ((CombinatorConstant) patternHead).getValue();

                    // If the constructor names (or values) match exactly, return the branch result
                    if (inputVal != null && inputVal.equals(patternVal)) {
                        return matchCase.getResult();
                    }
                }
            }
        }
        return null; // No match found
    }

    private static boolean isRecursive(Combinator c) {
        if (c instanceof CombinatorApplication) {
            return ((CombinatorApplication) c).getFunction() instanceof YCombinator;
        }
        return false;
    }
}