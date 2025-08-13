package ca.brock.cs.lambda;

import ca.brock.cs.lambda.parser.ProgParser;
import ca.brock.cs.lambda.parser.Term;

import java.util.HashMap;
import java.util.Map;

public class EvalTest {
    public static void main(String[] args) {
        ProgParser parser = new ProgParser();
        Map<String, Term> initialEnv = new HashMap<>(); // Start with an empty environment for global evaluation

        System.out.println("--- Testing Lambda Calculus Evaluator (eval method) ---");
        System.out.println("-----------------------------------------------------");

        // Test Case 1: Basic arithmetic
        testEval(parser, "1 + 2", initialEnv);
        testEval(parser, "5 * (2 + 3)", initialEnv);
        testEval(parser, "(10 - 4) * 2", initialEnv);

        System.out.println("\n-----------------------------------------------------");

        // Test Case 2: Boolean logic
        testEval(parser, "True and False", initialEnv);
        testEval(parser, "True or False", initialEnv);
        testEval(parser, "not True", initialEnv);
        testEval(parser, "not (True and False)", initialEnv);

        System.out.println("\n-----------------------------------------------------");

        // Test Case 3: Comparisons
        testEval(parser, "5 = 5", initialEnv);
        testEval(parser, "3 <= 5", initialEnv);
        testEval(parser, "5 = 6", initialEnv);
        testEval(parser, "True = False", initialEnv);

        System.out.println("\n-----------------------------------------------------");

        // Test Case 4: Conditionals
        testEval(parser, "if True then 10 else 20", initialEnv);
        testEval(parser, "if False then 10 else 20", initialEnv);
        testEval(parser, "if (1 = 1) then (2 * 3) else (4 + 5)", initialEnv);

        System.out.println("\n-----------------------------------------------------");

        // Test Case 5: Lambda Abstraction and Application (Beta-reduction)
        testEval(parser, "(λx. x + 1) 5", initialEnv);
        testEval(parser, "(λf. λx. f (f x)) (λy. y * 2) 3", initialEnv); // (f (f x)) with f = *2, x = 3 -> (2* (2*3)) -> 12
        testEval(parser, "(λa. λb. a + b) 10 20", initialEnv);

        System.out.println("\n-----------------------------------------------------");

        // Test Case 6: Recursion (Factorial example)
        // rec fact. (\n. if n = 0 then 1 else n * (fact (n - 1))) 4
        // Note: The parsing for 'rec' is `rec name . body`. The application of '4' is outside the recursion definition.
        // It's equivalent to (rec fact. (\n. if n = 0 then 1 else n * (fact (n - 1)))) 4
        // If your parser expects the application outside, this works.
        // If your recursion is defined for a fixed-point combinator (e.g., Y combinator), this syntax might vary.
        // Assuming your 'rec' evaluates to a function that can then be applied.
        String factorialExpr = "rec fact. (λn. if n = 0 then 1 else n * (fact (n - 1)))";
        Term parsedFactorialFunc = parser.parse(factorialExpr);
        System.out.println("Original Term: " + parsedFactorialFunc);
        System.out.println("Evaluating factorial function...");
        Term evaluatedFactorialFunc = parsedFactorialFunc.eval(initialEnv);
        System.out.println("Evaluated Factorial Function (if it reduces to a function): " + evaluatedFactorialFunc);

        // Now apply the evaluated factorial function to a number
        System.out.println("\nApplying factorial function to 4:");
        testEval(parser, "(" + evaluatedFactorialFunc.toStringPrec(0) + ") 4", initialEnv); // Apply the evaluated func to 4
        System.out.println("\nApplying factorial function to 0:");
        testEval(parser, "(" + evaluatedFactorialFunc.toStringPrec(0) + ") 0", initialEnv);


        System.out.println("\n-----------------------------------------------------");

        // Test Case 7: Operator Sections and Flip
        // (1 +) 2  should become 1 + 2 = 3
        testEval(parser, "(1 +) 2", initialEnv);
        // (+ 1) 2 should become 2 + 1 = 3
        testEval(parser, "(+ 1) 2", initialEnv);
        // (*) 2 3 should become 2 * 3 = 6
        testEval(parser, "(*) 2 3", initialEnv);
        // (flip +) 2 1 should become 1 + 2 = 3
        testEval(parser, "( (flip +) 2) 1", initialEnv);
        testEval(parser, "((flip +) 5) 10", initialEnv); // Should be 10 + 5 = 15

        System.out.println("\n--- Evaluation Tests Complete ---");
    }

    private static void testEval(ProgParser parser, String expression, Map<String, Term> env) {
        try {
            Term parsedTerm = parser.parse(expression);
            System.out.println("Expression: " + expression);
            System.out.println("Parsed Term: " + parsedTerm);
            Term evaluatedTerm = parsedTerm.eval(new HashMap<>(env)); // Use a copy of the environment
            System.out.println("Evaluated Result: " + evaluatedTerm);
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.err.println("Error evaluating '" + expression + "': " + e.getMessage());
            e.printStackTrace();
            System.out.println("-------------------------------------");
        }
    }
}
