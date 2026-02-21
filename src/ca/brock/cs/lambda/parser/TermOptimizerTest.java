package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.*;

import java.util.*;

/**
 * Test class for TermOptimizer that demonstrates common subexpression elimination.
 */
public class TermOptimizerTest {

    public static void main(String[] args) {
        System.out.println("=== COMPREHENSIVE TERM OPTIMIZER TESTS ===\n");

        String[] testPrograms = {
            // Test 1: Factorial function
            "fact : Int -> Int;\n" +
                "fact = rec f. λn.\n" +
                "    if n <= 1\n" +
                "    then 1\n" +
                "    else n * (f (n - 1));\n\n" +
                "main : Int; " +
                "main = fact 5;",

            // Test 2: List data type with common subexpressions
            "data list a = emptylist | cons a (list a);\n" +
                "doubleList : list Int -> list Int;\n" +
                "doubleList = λxs. match xs with\n" +
                "    emptylist -> emptylist\n" +
                "    | cons x xs_tail -> cons (x * 2) (doubleList xs_tail)\n" +
                "end;\n" +
                "main = doubleList (cons 1 (cons 2 emptylist));",

            // Test 3: Common subexpression example - (x + 1) appears twice
            "squarePlusOne : Int -> Int;\n" +
                "squarePlusOne = λx. (x + 1) * (x + 1);\n" +
                "main = squarePlusOne 5;",

//            // Test 4: Nested common subexpressions
//            "complexCalc : Int -> Int -> Int;\n" +
//                "complexCalc = λa. λb. \n" +
//                "    let x = a * a in\n" +
//                "    let y = b * b in\n" +
//                "    (x + y) * (x + y);\n" +
//                "main = complexCalc 3 4;",

            // Test 5: Basic arithmetic with common operations
            "add : Int -> Int -> Int;\n" +
                "add = λy. λx. x + y;\n" +
                "main = add 5 3;",

            // Test 6: Multiple identical function applications
            "applyTwice : (Int -> Int) -> Int -> Int;\n" +
                "applyTwice = λf. λx. f (f x);\n" +
                "increment : Int -> Int;\n" +
                "increment = λx. x + 1;\n" +
                "main = applyTwice increment 5;",

            // Test 7: Complex nested expressions
            "nestedExpr : Int -> Int;\n" +
                "nestedExpr = λx.\n" +
                "    if x <= 0\n" +
                "    then 0\n" +
                "    else (x * x) + (x * x) + (x * x);\n" +
                "main = nestedExpr 4;",

            // Test 8: Logical expressions with duplicates
            "logicalExpr : Bool -> Bool;\n" +
                "logicalExpr = λx. (x and True) or (x and True);\n" +
                "main = logicalExpr False;",

            // Test 9: Multiple parameter function with duplicates
            "multiParam : Int -> Int -> Int;\n" +
                "multiParam = λa. λb. (a + b) * (a + b) + (a + b);\n" +
                "main = multiParam 2 3;",

            // Test 10: Deeply nested duplicates
            "deepNested : Int -> Int;\n" +
                "deepNested = λx. \n" +
                "    ((x + 1) * (x + 2)) + \n" +
                "    ((x + 1) * (x + 2)) + \n" +
                "    ((x + 1) * (x + 2));\n" +
                "main = deepNested 4;",

            // Test 14: Division and Subtraction
            // Tests operators with lower precedence and different associativity
            "mathTest : Int -> Int -> Int;\n" +
                "mathTest = λx. λy. \n" +
                "    ((x - y) / 2) * ((x - y) / 2) + ((x - y) / 2);\n" +
                "main = mathTest 10 2;"
        };

        for (int i = 0; i < testPrograms.length; i++) {
            System.out.println("=".repeat(80));
            System.out.println("TEST " + (i + 1));
            System.out.println("=".repeat(80));

            try {
                testOptimizer(testPrograms[i], i + 1);
            } catch (Exception e) {
                System.out.println("Error in test " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("\n\n");
        }

        System.out.println("=== ALL TESTS COMPLETED ===");
    }

    private static void testOptimizer(String programString, int testNumber) {
        Map<String, DefinedValue> symbolMap = new HashMap<>();

        try {
            System.out.println("Original Program:\n");
            System.out.println(programString);
            System.out.println("\n" + "-".repeat(40));

            // Step 1: Parse the program
            System.out.println("1. PARSING");
            System.out.println("-".repeat(20));
            ProgParser.ParsedProgram program = ProgParser.parse(programString, symbolMap);
            System.out.println("✓ Parsed successfully");
            System.out.println("Main function: " + program.mainFunction.getName());

            // Step 2: Extract and display original terms
            System.out.println("\n2. ORIGINAL TERMS");
            System.out.println("-".repeat(20));
            Map<Term, Integer> originalStats = displayTerms("Original", symbolMap);

            // Step 3: Apply term optimization
            System.out.println("\n3. OPTIMIZED TERMS (Common Subexpression Elimination)");
            System.out.println("-".repeat(20));
            ca.brock.cs.lambda.parser.TermOptimizer optimizer = new ca.brock.cs.lambda.parser.TermOptimizer();
            Map<String, DefinedValue> optimizedSymbolMap = optimizer.optimizeProgram(symbolMap);
            Map<Term, Integer> optimizedStats = displayTerms("Optimized", optimizedSymbolMap);

//            // Step 4: Compare results
//            System.out.println("\n4. COMPARISON");
//            System.out.println("-".repeat(20));
//            compareTerms(symbolMap, optimizedSymbolMap, originalStats, optimizedStats);

            // Step 5: Test evaluation
            System.out.println("\n5. EVALUATION TEST");
            System.out.println("-".repeat(20));
            testEvaluation(program, optimizedSymbolMap);

        } catch (ParserException e) {
            System.out.println("✗ Parser Error: " + e.getMessage());
        } catch (TypeError e) {
            System.out.println("✗ Type Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Map<Term, Integer> displayTerms(String label, Map<String, DefinedValue> symbolMap) {
        Map<Term, Integer> stats = new HashMap<>();

        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            if (entry.getValue() instanceof FunctionDefinition) {
                FunctionDefinition funcDef = (FunctionDefinition) entry.getValue();
                System.out.println(funcDef.getName() + ":");

                if (funcDef.getTerm() != null) {
                    Term term = funcDef.getTerm();
                    System.out.println("  Term: " + term);

                    // Calculate metrics
                    int size = calculateTermSize(term);
                    int duplicates = countDuplicates(term);

                    System.out.println("  Size (nodes): " + size);
                    System.out.println("  Duplicate subterms: " + duplicates);

                    // Store for comparison
                    stats.put(term, size);
                }
                System.out.println();
            } else if (entry.getValue() instanceof AlgebraicDataType) {
                AlgebraicDataType adt = (AlgebraicDataType) entry.getValue();
                System.out.println("Data type: " + adt.getName());
                System.out.println("  Constructors: " +
                    String.join(", ", adt.getConstructors().stream()
                        .map(Constructor::getName)
                        .toArray(String[]::new)));
                System.out.println();
            }
        }

        return stats;
    }

    private static void compareTerms(Map<String, DefinedValue> original,
        Map<String, DefinedValue> optimized,
        Map<Term, Integer> originalStats,
        Map<Term, Integer> optimizedStats) {

        for (String funcName : original.keySet()) {
            if (original.get(funcName) instanceof FunctionDefinition &&
                optimized.containsKey(funcName) &&
                optimized.get(funcName) instanceof FunctionDefinition) {

                FunctionDefinition origFunc = (FunctionDefinition) original.get(funcName);
                FunctionDefinition optFunc = (FunctionDefinition) optimized.get(funcName);

                if (origFunc.getTerm() != null && optFunc.getTerm() != null) {
                    System.out.println("Function: " + funcName);

                    int origSize = originalStats.getOrDefault(origFunc.getTerm(), 0);
                    int optSize = optimizedStats.getOrDefault(optFunc.getTerm(), 0);

                    System.out.println("  Original size: " + origSize);
                    System.out.println("  Optimized size: " + optSize);

                    if (optSize < origSize) {
                        int reduction = ((origSize - optSize) * 100) / origSize;
                        System.out.println("  ✓ Size reduction: " + reduction + "%");
                    } else if (optSize == origSize) {
                        System.out.println("  Size unchanged");
                    } else {
                        System.out.println("  Note: Size increased (check optimization)");
                    }

                    // Check if terms are semantically equivalent (simple check)
                    boolean termsEqual = termsAreEquivalent(origFunc.getTerm(), optFunc.getTerm());
                    System.out.println("  Terms semantically equivalent: " + (termsEqual ? "✓" : "✗"));

                    System.out.println();
                }
            }
        }
    }

    private static void testEvaluation(ProgParser.ParsedProgram original,
        Map<String, DefinedValue> optimizedSymbolMap) {
        try {
            // Create environment for evaluation
            Map<String, Term> env = new HashMap<>();

            // Add flip definition to environment
            Term flipDefinition = new Abstraction("f",
                new Abstraction("x",
                    new Abstraction("y",
                        new Application(
                            new Application(new Variable("f"), new Variable("y")),
                            new Variable("x")
                        )
                    )
                )
            );
            env.put("flip", flipDefinition);

            // Add all function definitions to environment
            for (Map.Entry<String, DefinedValue> entry : original.symbolMap.entrySet()) {
                if (entry.getValue() instanceof FunctionDefinition) {
                    FunctionDefinition funcDef = (FunctionDefinition) entry.getValue();
                    env.put(entry.getKey(), funcDef.getTerm());
                }
            }

            // Get main terms
            Term originalMain = original.mainFunction.getTerm();
            Term optimizedMain = null;

            // Find optimized main function
            if (optimizedSymbolMap.get("main") instanceof FunctionDefinition) {
                FunctionDefinition optMainFunc = (FunctionDefinition) optimizedSymbolMap.get("main");
                optimizedMain = optMainFunc.getTerm();
            }

            if (optimizedMain == null) {
                System.out.println("Could not find optimized main function");
                return;
            }

            // Evaluate both
            Term originalResult = evaluateToNormalForm(originalMain, env);
            Term optimizedResult = evaluateToNormalForm(optimizedMain, env);

            System.out.println("Original evaluation: " + originalResult);
            System.out.println("Optimized evaluation: " + optimizedResult);

            if (originalResult.toString().equals(optimizedResult.toString())) {
                System.out.println("✓ Results are identical");
            } else {
                System.out.println("✗ Results differ!");
                System.out.println("Original: " + originalResult);
                System.out.println("Optimized: " + optimizedResult);
            }

        } catch (Exception e) {
            System.out.println("Evaluation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Term evaluateToNormalForm(Term term, Map<String, Term> env) {
        Term current = term;
        Term previous = null;
        int steps = 0;
        int maxSteps = 1000;

        do {
            previous = current;
            current = current.eval(env);
            steps++;

            if (steps >= maxSteps) {
                System.out.println("  (Evaluation stopped after " + maxSteps + " steps)");
                break;
            }
        } while (!current.equals(previous));

        return current;
    }

    // Helper methods for analysis

    private static int calculateTermSize(Term term) {
        if (term instanceof Application) {
            Application app = (Application) term;
            return 1 + calculateTermSize(app.getFunction()) + calculateTermSize(app.getArgument());
        } else if (term instanceof Abstraction) {
            Abstraction abs = (Abstraction) term;
            return 1 + calculateTermSize(abs.getBody());
        } else if (term instanceof Addition) {
            Addition add = (Addition) term;
            return 1 + calculateTermSize(add.getLeft()) + calculateTermSize(add.getRight());
        } else if (term instanceof Subtraction) {
            Subtraction sub = (Subtraction) term;
            return 1 + calculateTermSize(sub.getLeft()) + calculateTermSize(sub.getRight());
        } else if (term instanceof Multiplication) {
            Multiplication mul = (Multiplication) term;
            return 1 + calculateTermSize(mul.getLeft()) + calculateTermSize(mul.getRight());
        } else if (term instanceof Division) {
            Division div = (Division) term;
            return 1 + calculateTermSize(div.getLeft()) + calculateTermSize(div.getRight());
        } else if (term instanceof And) {
            And and = (And) term;
            return 1 + calculateTermSize(and.getLeft()) + calculateTermSize(and.getRight());
        } else if (term instanceof Or) {
            Or or = (Or) term;
            return 1 + calculateTermSize(or.getLeft()) + calculateTermSize(or.getRight());
        } else if (term instanceof Equal) {
            Equal eq = (Equal) term;
            return 1 + calculateTermSize(eq.getLeft()) + calculateTermSize(eq.getRight());
        } else if (term instanceof LEqual) {
            LEqual le = (LEqual) term;
            return 1 + calculateTermSize(le.getLeft()) + calculateTermSize(le.getRight());
        } else if (term instanceof Not) {
            Not not = (Not) term;
            return 1 + calculateTermSize(not.getOperand());
        } else if (term instanceof Conditional) {
            Conditional cond = (Conditional) term;
            return 1 + calculateTermSize(cond.getCondition()) +
                calculateTermSize(cond.getTrueBranch()) +
                calculateTermSize(cond.getFalseBranch());
        } else if (term instanceof Recursion) {
            Recursion rec = (Recursion) term;
            return 1 + calculateTermSize(rec.getBody());
        } else if (term instanceof Match) {
            Match match = (Match) term;
            int size = 1 + calculateTermSize(match.getInputTerm());
            for (Match.Case c : match.getCases()) {
                size += calculateTermSize(c.getResult());
            }
            return size;
        } else if (term instanceof Variable || term instanceof IntegerLiteral ||
            term instanceof BooleanLiteral || term instanceof Constant ||
            term instanceof Constructor) {
            return 1;
        }
        return 0;
    }

    private static int countDuplicates(Term term) {
        Map<String, Integer> termCounts = new HashMap<>();
        countSubterms(term, termCounts);

        int duplicates = 0;
        for (int count : termCounts.values()) {
            if (count > 1) {
                duplicates += count - 1;
            }
        }
        return duplicates;
    }

    private static void countSubterms(Term term, Map<String, Integer> termCounts) {
        if (term == null) return;

        String key = term.toString();
        termCounts.put(key, termCounts.getOrDefault(key, 0) + 1);

        // Recursively count subterms
        if (term instanceof Application) {
            Application app = (Application) term;
            countSubterms(app.getFunction(), termCounts);
            countSubterms(app.getArgument(), termCounts);
        } else if (term instanceof Abstraction) {
            Abstraction abs = (Abstraction) term;
            countSubterms(abs.getBody(), termCounts);
        } else if (term instanceof Addition) {
            Addition add = (Addition) term;
            countSubterms(add.getLeft(), termCounts);
            countSubterms(add.getRight(), termCounts);
        } else if (term instanceof Subtraction) {
            Subtraction sub = (Subtraction) term;
            countSubterms(sub.getLeft(), termCounts);
            countSubterms(sub.getRight(), termCounts);
        } else if (term instanceof Multiplication) {
            Multiplication mul = (Multiplication) term;
            countSubterms(mul.getLeft(), termCounts);
            countSubterms(mul.getRight(), termCounts);
        } else if (term instanceof Division) {
            Division div = (Division) term;
            countSubterms(div.getLeft(), termCounts);
            countSubterms(div.getRight(), termCounts);
        } else if (term instanceof And) {
            And and = (And) term;
            countSubterms(and.getLeft(), termCounts);
            countSubterms(and.getRight(), termCounts);
        } else if (term instanceof Or) {
            Or or = (Or) term;
            countSubterms(or.getLeft(), termCounts);
            countSubterms(or.getRight(), termCounts);
        } else if (term instanceof Equal) {
            Equal eq = (Equal) term;
            countSubterms(eq.getLeft(), termCounts);
            countSubterms(eq.getRight(), termCounts);
        } else if (term instanceof LEqual) {
            LEqual le = (LEqual) term;
            countSubterms(le.getLeft(), termCounts);
            countSubterms(le.getRight(), termCounts);
        } else if (term instanceof Not) {
            Not not = (Not) term;
            countSubterms(not.getOperand(), termCounts);
        } else if (term instanceof Conditional) {
            Conditional cond = (Conditional) term;
            countSubterms(cond.getCondition(), termCounts);
            countSubterms(cond.getTrueBranch(), termCounts);
            countSubterms(cond.getFalseBranch(), termCounts);
        } else if (term instanceof Recursion) {
            Recursion rec = (Recursion) term;
            countSubterms(rec.getBody(), termCounts);
        } else if (term instanceof Match) {
            Match match = (Match) term;
            countSubterms(match.getInputTerm(), termCounts);
            for (Match.Case c : match.getCases()) {
                countSubterms(c.getResult(), termCounts);
            }
        }
    }

    private static boolean termsAreEquivalent(Term t1, Term t2) {
        // Simple check: evaluate both terms and compare results
        Map<String, Term> env = new HashMap<>();

        try {
            Term result1 = evaluateToNormalForm(t1, env);
            Term result2 = evaluateToNormalForm(t2, env);
            return result1.toString().equals(result2.toString());
        } catch (Exception e) {
            // If evaluation fails, fall back to structural comparison
            return t1.toString().equals(t2.toString());
        }
    }
}