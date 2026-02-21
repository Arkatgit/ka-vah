package ca.brock.cs.lambda.combinators;

import java.util.*;

/**
 * Utility class for inlining function calls in combinators by substituting definitions.
 */
public class CombinatorInliner {

    /**
     * Inline all function calls in the combinator map.
     * @param combinatorMap Map of function names to their combinator definitions
     * @param functionsToInline Set of function names to inline (null means all)
     * @return Map with inlined combinators
     */
    public static Map<String, Combinator> inline(
        Map<String, Combinator> combinatorMap,
        Set<String> functionsToInline
    ) {
        Map<String, Combinator> result = new HashMap<>();

        for (Map.Entry<String, Combinator> entry : combinatorMap.entrySet()) {
            String name = entry.getKey();
            Combinator combinator = entry.getValue();

            // Create environment without this function to avoid infinite recursion
            Map<String, Combinator> env = new HashMap<>(combinatorMap);
            env.remove(name);

            // Inline this combinator
            Combinator inlined = substituteVariables(combinator, env, functionsToInline, new HashSet<>());
            result.put(name, inlined);
        }

        return result;
    }

    /**
     * Inline all function calls (all functions).
     */
    public static Map<String, Combinator> inlineAll(Map<String, Combinator> combinatorMap) {
        return inline(combinatorMap, null);
    }

    /**
     * Inline specific functions only.
     */
    public static Map<String, Combinator> inlineSelected(
        Map<String, Combinator> combinatorMap,
        Set<String> functionsToInline
    ) {
        return inline(combinatorMap, functionsToInline);
    }

    /**
     * Recursively substitute variables in a combinator.
     */
//    private static Combinator substituteVariables(
//        Combinator c,
//        Map<String, Combinator> env,
//        Set<String> functionsToInline,
//        Set<String> visited
//    ) {
//        // Variable that might be a function call
//        if (c instanceof CombinatorVariable) {
//            String varName = ((CombinatorVariable) c).getName();
//
//            // Check if we should inline this variable
//            if (shouldInline(varName, functionsToInline) && env.containsKey(varName)) {
//                // Avoid infinite recursion
//                if (visited.contains(varName)) {
//                    return c; // Keep as variable to break recursion
//                }
//
//                visited.add(varName);
//                Combinator definition = env.get(varName);
//                Combinator substituted = substituteVariables(definition, env, functionsToInline, visited);
//                visited.remove(varName);
//                return substituted;
//            }
//            return c;
//        }
//
//        // Application: substitute in both parts
//        else if (c instanceof CombinatorApplication) {
//            CombinatorApplication app = (CombinatorApplication) c;
//            Combinator newFunc = substituteVariables(app.getFunction(), env, functionsToInline, visited);
//            Combinator newArg = substituteVariables(app.getArgument(), env, functionsToInline, visited);
//            return new CombinatorApplication(newFunc, newArg);
//        }
//
//        // Constants, I, K, S, etc. remain unchanged
//        return c;
//    }
    /**
     * Recursively substitute variables in a combinator,
     * but skip inlining for recursive functions.
     */
    private static Combinator substituteVariables(
        Combinator c,
        Map<String, Combinator> env,
        Set<String> functionsToInline,
        Set<String> visited
    ) {
        // Variable that might be a function call
        if (c instanceof CombinatorVariable) {
            String varName = ((CombinatorVariable) c).getName();

            // Check if we should inline this variable
            if (shouldInline(varName, functionsToInline) && env.containsKey(varName)) {
                Combinator definition = env.get(varName);

                // NEW RULE: Do not inline if the function is recursive
                if (isRecursive(definition)) {
                    return c; // Keep as a named variable call
                }

                // Avoid infinite recursion in the inliner itself
                if (visited.contains(varName)) {
                    return c;
                }

                visited.add(varName);
                Combinator substituted = substituteVariables(definition, env, functionsToInline, visited);
                visited.remove(varName);
                return substituted;
            }
            return c;
        }

        // Application: substitute in both parts
        else if (c instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) c;
            Combinator newFunc = substituteVariables(app.getFunction(), env, functionsToInline, visited);
            Combinator newArg = substituteVariables(app.getArgument(), env, functionsToInline, visited);
            return new CombinatorApplication(newFunc, newArg);
        }

        return c;
    }

    /**
     * Helper to determine if a combinator represents a recursive function.
     * In this system, recursive functions are wrapped in a Y Combinator.
     */
    private static boolean isRecursive(Combinator c) {
        // Check if the definition starts with a Y Combinator application
        if (c instanceof CombinatorApplication) {
            Combinator func = ((CombinatorApplication) c).getFunction();
            // Rec x. M translates to Y (λx. M)
            return func instanceof YCombinator;
        }
        return false;
    }


    private static boolean shouldInline(String functionName, Set<String> functionsToInline) {
        // If null, inline everything
        if (functionsToInline == null) return true;
        // Otherwise only inline if in the set
        return functionsToInline.contains(functionName);
    }

    /**
     * Optimize combinators after inlining.
     */
    public static Map<String, Combinator> optimizeAfterInlining(Map<String, Combinator> inlinedMap) {
        Map<String, Combinator> optimized = new HashMap<>();

        for (Map.Entry<String, Combinator> entry : inlinedMap.entrySet()) {
            optimized.put(entry.getKey(), entry.getValue().optimize());
        }

        return optimized;
    }
}