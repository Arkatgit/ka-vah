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

    private static boolean isRecursive(Combinator c) {
        if (c instanceof CombinatorApplication) {
            return ((CombinatorApplication) c).getFunction() instanceof YCombinator;
        }
        return false;
    }
}