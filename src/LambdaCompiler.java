import ca.brock.cs.lambda.combinators.*;
import ca.brock.cs.lambda.parser.*;
import ca.brock.cs.lambda.types.*;

import java.util.*;

/**
 * Compiler that parses a program, type-checks functions, optimizes terms,
 * and translates them to combinators.
 */
public class LambdaCompiler {

    public static class CompilationResult {
        public final Map<String, DefinedValue> originalSymbolMap;
        public final Map<String, DefinedValue> optimizedSymbolMap;
        public final Map<String, Type> typeMap;
        public final Map<String, Combinator> rawCombinatorMap;
        public final Map<String, Combinator> inlinedCombinatorMap;
        public final Map<String, Combinator> optimizedCombinatorMap;
        public final FunctionDefinition mainFunction;

        public CompilationResult(
            Map<String, DefinedValue> originalSymbolMap,
            Map<String, DefinedValue> optimizedSymbolMap,
            Map<String, Type> typeMap,
            Map<String, Combinator> rawCombinatorMap,
            Map<String, Combinator> inlinedCombinatorMap,
            Map<String, Combinator> optimizedCombinatorMap,
            FunctionDefinition mainFunction
        ) {
            this.originalSymbolMap = originalSymbolMap;
            this.optimizedSymbolMap = optimizedSymbolMap;
            this.typeMap = typeMap;
            this.rawCombinatorMap = rawCombinatorMap;
            this.inlinedCombinatorMap = inlinedCombinatorMap;
            this.optimizedCombinatorMap = optimizedCombinatorMap;
            this.mainFunction = mainFunction;
        }
    }

    /**
     * Parse a program string.
     */
    public static ProgParser.ParsedProgram parse(String programString) {
        Map<String, DefinedValue> symbolMap = new HashMap<>();
        return ProgParser.parse(programString, symbolMap);
    }

    /**
     * Type check all functions.
     */
    public static Map<String, Type> typeCheck(
        Map<String, DefinedValue> symbolMap
    ) throws TypeError {
        Map<String, Type> typeMap = new HashMap<>();

        // First, populate the environment with all constructors and function signatures
        Map<String, Type> baseEnv = new HashMap<>();

        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            DefinedValue def = entry.getValue();

            if (def instanceof Constructor) {
                baseEnv.put(entry.getKey(), def.getType());
            } else if (def instanceof FunctionDefinition) {
                FunctionDefinition funcDef = (FunctionDefinition) def;
                if (funcDef.getType() != null) {
                    // Add declared type signatures to environment
                    baseEnv.put(entry.getKey(), funcDef.getType());
                }
            }
        }

        // Now, iterate and type check each function definition
        for (DefinedValue definedValue : symbolMap.values()) {
            if (definedValue instanceof FunctionDefinition) {
                FunctionDefinition funcDef = (FunctionDefinition) definedValue;
                String name = funcDef.getName();

                Type declaredType = funcDef.getType();

                if (funcDef.getTerm() == null) {
                    throw new TypeError("No term defined for function: " + name);
                }

                try {
                    // Create fresh unifier and environment for each function
                    Unifier unifier = new Unifier();
                    Map<String, Type> functionEnv = new HashMap<>(baseEnv);
                    unifier.setSymbolMap(symbolMap);

                    // Compute the type
                    Type inferredType = funcDef.getTerm().computeType(functionEnv, unifier);

                    if (declaredType != null) {
                        // Try to unify declared and inferred types
                        Map<TVar, Type> unificationResult = unifier.unify(declaredType, inferredType);
                        if (unificationResult == null) {
                            throw new TypeError("Type mismatch for " + name +
                                ": declared " + declaredType + " vs inferred " + inferredType);
                        }
                        // Apply substitutions to get the resolved type
                        Type resolvedType = unifier.applySubstitution(declaredType, unifier.getEnv());
                        typeMap.put(name, resolvedType);
                    } else {
                        // No declared type - use the inferred type
                        // Apply substitutions to resolve any type variables
                        Type resolvedType = unifier.applySubstitution(inferredType, unifier.getEnv());
                        typeMap.put(name, resolvedType);
                    }

                } catch (Exception e) {
                    throw new TypeError("Failed to compute type for function '" + name + "': " + e.getMessage(), e);
                }
            }
        }

        return typeMap;
    }

    /**
     * Optimize terms using Common Subexpression Elimination (CSE).
     */
    public static Map<String, DefinedValue> optimizeTerms(
        Map<String, DefinedValue> symbolMap
    ) {
        TermOptimizer optimizer = new TermOptimizer();
        return optimizer.optimizeProgram(symbolMap);
    }

    /**
     * Translate all function terms to raw combinators (no inlining yet).
     */
    public static Map<String, Combinator> translateToRawCombinators(
        Map<String, DefinedValue> symbolMap
    ) {
        Map<String, Combinator> rawCombinators = new HashMap<>();

        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            if (entry.getValue() instanceof FunctionDefinition) {
                FunctionDefinition funcDef = (FunctionDefinition) entry.getValue();
                String name = entry.getKey();

                // Convert term to combinator
                Combinator combinator = funcDef.getTerm().toIntermediateTerm()
                    .methodT(true)  // With optimizations
                    .toCombinatorTerm();

                rawCombinators.put(name, combinator);
            }
        }

        return rawCombinators;
    }

//    /**
//     * Main compilation pipeline.
//     */
//    public static CompilationResult compile(String programString) throws Exception {
//        // 1. Parse
//        ProgParser.ParsedProgram parsed = parse(programString);
//
//        // 2. Type check
//        Map<String, Type> types = typeCheck(parsed.symbolMap);
//
//        // 3. Optimize terms with CSE
//        Map<String, DefinedValue> optimizedSymbolMap = optimizeTerms(parsed.symbolMap);
//
//        // 4. Translate to raw combinators
//        Map<String, Combinator> rawCombinators = translateToRawCombinators(optimizedSymbolMap);
//
//        // 5. Inline all functions using CombinatorInliner
//        Map<String, Combinator> inlinedCombinators =
//            CombinatorInliner.inlineAll(rawCombinators);
//
//        // 6. Optimize after inlining
//        Map<String, Combinator> optimizedCombinators =
//            CombinatorInliner.optimizeAfterInlining(inlinedCombinators);
//
//        return new CompilationResult(
//            parsed.symbolMap,
//            optimizedSymbolMap,
//            types,
//            rawCombinators,
//            inlinedCombinators,
//            optimizedCombinators,
//            parsed.mainFunction
//        );
//    }
    /**
     * Main compilation pipeline with debug printing.
     */
//    public static CompilationResult compile(String programString) throws Exception {
//        // 1. Parse
//        ProgParser.ParsedProgram parsed = parse(programString);
//
//        // 2. Type check
//        Map<String, Type> types = typeCheck(parsed.symbolMap);
//        System.out.println("\n--- Resolved Types ---");
//        types.forEach((name, type) -> System.out.println(name + " : " + type));
//
//        // 3. Optimize terms with CSE
//        Map<String, DefinedValue> optimizedSymbolMap = optimizeTerms(parsed.symbolMap);
//        System.out.println("\n--- Optimized Symbol Map ---");
//        optimizedSymbolMap.forEach((k, v) -> System.out.println(k + " = " + v));
//
//        // 4. Translate to raw combinators
//        Map<String, Combinator> rawCombinators = translateToRawCombinators(optimizedSymbolMap);
//
//        System.out.println("\n--- Raw Combinators ---");
//        rawCombinators.forEach((k, v) -> System.out.println(k + " = " + v));
//
//        // 5. Inline all functions using CombinatorInliner
//        Map<String, Combinator> inlinedCombinators =
//            CombinatorInliner.inlineAll(rawCombinators);
//
//        System.out.println("\n--- Inlined Combinators ---");
//        inlinedCombinators.forEach((k, v) -> System.out.println(k + " = " + v));
//
//        // 6. Optimize after inlining
//        Map<String, Combinator> optimizedCombinators =
//            CombinatorInliner.optimizeAfterInlining(inlinedCombinators);
//
//        System.out.println("\n--- Optimized Combinators ---");
//        optimizedCombinators.forEach((k, v) -> System.out.println(k + " = " + v));
//
//        return new CompilationResult(
//            parsed.symbolMap,
//            optimizedSymbolMap,
//            types,
//            rawCombinators,
//            inlinedCombinators,
//            optimizedCombinators,
//            parsed.mainFunction
//        );
//    }
//
    public static CompilationResult compile(String programString) throws Exception {
        // 1. Parse
        ProgParser.ParsedProgram parsed = parse(programString);
        for (Map.Entry<String, DefinedValue> entry : parsed.symbolMap.entrySet()) {
            DefinedValue value = entry.getValue();

            if (value instanceof Constructor) {
                Constructor constructor = (Constructor) value;
                System.out.println("[Constructor] " + constructor.getName() + " : " + constructor.getType());
            }
            else if (value instanceof FunctionDefinition) {
                FunctionDefinition func = (FunctionDefinition) value;

                // Print Type Signature if declared
                if (func.getType() != null) {
                    System.out.println("[Signature]   " + func.getName() + " : " + func.getType());
                }

                // Print the actual Function Definition/Term
                System.out.println("[Definition]  " + func.getName() + " = " + func.getTerm());
            }
        }

        // 2. Type check
        Map<String, Type> types = typeCheck(parsed.symbolMap);
        System.out.println("\n--- Resolved Types ---");
        types.forEach((name, type) -> System.out.println(name + " : " + type));

        // 3. Optimize terms with CSE
        Map<String, DefinedValue> optimizedSymbolMap = optimizeTerms(parsed.symbolMap);
        System.out.println("\n--- Optimized Terms with CSE ---");
        optimizedSymbolMap.forEach((k, v) -> System.out.println(k + " = " + v));

        // 4. Translate to raw combinators
        Map<String, Combinator> rawCombinators = translateToRawCombinators(optimizedSymbolMap);
        System.out.println("\n--- Raw Combinators ---");
        rawCombinators.forEach((k, v) -> System.out.println(k + " = " + v));

        // 5. Inline all functions
        Map<String, Combinator> inlinedCombinators = CombinatorInliner.inlineAll(rawCombinators);
        System.out.println("\n--- [DEBUG] Inlined Combinators ---");
        inlinedCombinators.forEach((k, v) -> System.out.println(k + " = " + v));
        // 6. Optimize after inlining (Algebraic simplifications)
        Map<String, Combinator> optimizedAfterInlining = CombinatorInliner.optimizeAfterInlining(inlinedCombinators);
        System.out.println("\n--- [DEBUG] Optimized After Inlining ---");
        optimizedAfterInlining.forEach((k, v) -> System.out.println(k + " = " + v));


        // 7. NEW: Partial Evaluation (Constant folding and simplification)
        Map<String, Combinator> partiallyEvaluatedMap = CombinatorPartialEvaluator.partialEvaluateAll(optimizedAfterInlining);
        System.out.println("\n--- [DEBUG] Partially Evaluated Combinators ---");
        partiallyEvaluatedMap.forEach((k, v) -> System.out.println(k + " = " + v));




        return new CompilationResult(
            parsed.symbolMap,
            optimizedSymbolMap,
            types,
            rawCombinators,
            inlinedCombinators,
            partiallyEvaluatedMap, // Store the final partially evaluated map here
            parsed.mainFunction
        );
    }


    /**
     * Simple test/demo.
     */
    public static void main(String[] args) {
        // Updated testProgram to be an array of program strings
        String[] testPrograms = {
            // Program 1: Basic arithmetic and lambdas
            "square : Int -> Int;\n" +
                "square = λx.x * x;\n" +
                "addSquare : Int -> Int -> Int;\n" +
                "addSquare = λa.λb.((a + 1) * (a + 1)) + (b * b);\n" +
                "main = addSquare 3 4;",

//            // Program 2: Algebraic Data Types and Pattern Matching
//            "data list a = emptylist | cons a (list a);\n" +
//                "sum : list Int -> Int;\n" +
//                "sum = λxs. match xs with\n" +
//                "    emptylist -> 0\n" +
//                "    | cons x xs_tail -> x + (sum xs_tail)\n" +
//                "end;\n" +
//                "main = sum (cons 1 (cons 2 (cons 3 emptylist)));",

            // Program 3: Simple Recursion (Factorial)
            "factorial : Int -> Int;\n" +
                "factorial = rec f. λn. \n" +
                "    if n <= 1 \n" +
                "    then 1 \n" +
                "    else n * (f (n - 1));\n" +
                "main = factorial 5;"
        };

        // Iterate over and test each program
        for (int i = 0; i < testPrograms.length; i++) {
            System.out.println("\n========================================");
            System.out.println("TESTING PROGRAM " + (i + 1));
            System.out.println("========================================");

            try {
                CompilationResult result = compile(testPrograms[i]);

                System.out.println("\n--- Compilation Successful ---");
                System.out.println("Main function: " + result.mainFunction.getName());
                System.out.println("Main type: " + result.typeMap.get("main"));

                // Evaluate the optimized/partially evaluated main combinator
                Map<String, Combinator> evalEnv = new HashMap<>();
                // Populate environment with the compiled combinators for lookup
                evalEnv.putAll(result.optimizedCombinatorMap);

//                Combinator finalResult = result.optimizedCombinatorMap.get("main").eval(evalEnv);
//
//                System.out.println("\nFinal Main Term (Combinator): " + result.optimizedCombinatorMap.get("main"));
//                System.out.println("Evaluated Result: " + finalResult);

            } catch (Exception e) {
                System.err.println("\nCompilation failed for Program " + (i + 1) + ":");
                e.printStackTrace();
            }
        }
    }


}