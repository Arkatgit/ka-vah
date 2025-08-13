package ca.brock.cs.lambda;

import ca.brock.cs.lambda.parser.ProgParser;
import ca.brock.cs.lambda.parser.Term;

import java.util.HashMap;
import java.util.Map;

public class SubstituteTest {
    public static void main(String[] args) {
        ProgParser parser = new ProgParser();
        Map<String, Term> initialEnv = new HashMap<>(); // Environment for evaluation, not directly for substitution

        System.out.println("--- Testing Term Substitution (substitute method) ---");
        System.out.println("-----------------------------------------------------");

        // Test Case 1: Simple variable substitution
        testSubstitute(parser, "x", "x", "5");
        testSubstitute(parser, "y", "x", "10"); // Variable not present

        System.out.println("\n-----------------------------------------------------");

        // Test Case 2: Substitution into Applications
        testSubstitute(parser, "f x", "x", "g y");
        testSubstitute(parser, "f x", "f", "λz. z + 1");
        testSubstitute(parser, "(a + b) * c", "a", "10");

        System.out.println("\n-----------------------------------------------------");

        // Test Case 3: Substitution into Abstractions (handling bound variables)
        // λx. x + y  with x := 5  -> should remain λx. x + y (x is bound)
        testSubstitute(parser, "λx. x + y", "x", "5");
        // λx. x + y  with y := 10 -> should be λx. x + 10 (y is free)
        testSubstitute(parser, "λx. x + y", "y", "10");
        // λf. λx. f (f x) with f := λz. z * 2
        testSubstitute(parser, "λf. λx. f (f x)", "f", "λz. z * 2");
        // λx. λy. x + y with x := 10 (should not substitute the bound x)
        testSubstitute(parser, "λx. λy. x + y", "x", "10");
        // λx. λy. x + y with z := 10 (z is not present)
        testSubstitute(parser, "λx. λy. x + y", "z", "10");

        testSubstitute(parser, "(λx. λy. x)", "y", "x");

        // Another capture example: (λx. λy. x)[x := y]
        // Here, the outer x is replaced by y. The inner y is bound.
        // Naive: λx. λy. y
        // Correct: λz. λy. y (if x was renamed to z) OR λy. λz. y (if y was renamed to z)
        // With current Abstraction.substitute, the outer x is replaced by y.
        // The inner λy. will check if 'y' (from the substituted value) is free in its body.
        // It should rename its own parameter 'y' to avoid capture.
        testSubstitute(parser, "λx. λy. x", "x", "y");

        System.out.println("\n-----------------------------------------------------");

        // Test Case 4: Substitution into Binary Operators (Addition, Subtraction, Multiplication, etc.)
        testSubstitute(parser, "a + b", "a", "100");
        testSubstitute(parser, "x - y", "y", "z");
        testSubstitute(parser, "p * q", "p", "q + r");

        System.out.println("\n-----------------------------------------------------");

        // Test Case 5: Substitution into Unary Operators (Not)
        testSubstitute(parser, "not x", "x", "True");
        testSubstitute(parser, "not (a and b)", "a", "False");

        System.out.println("\n-----------------------------------------------------");

        // Test Case 6: Substitution into Conditionals
        testSubstitute(parser, "if cond then true_branch else false_branch", "cond", "True");
        testSubstitute(parser, "if (x = y) then z else w", "z", "a + b");

        System.out.println("\n-----------------------------------------------------");

        // Test Case 7: Substitution into Recursion (handling bound variable)
        // rec fact. λn. if n = 0 then 1 else n * (fact (n - 1)) with fact := new_fact
        // 'fact' is bound by 'rec', so it should not be substituted inside the body.
        testSubstitute(parser, "rec fact. (λn. if n = 0 then 1 else n * (fact (n - 1)))", "fact", "new_fact");
        // rec fact. λn. if n = 0 then 1 else n * (fact (n - 1)) with n := 5 (n is bound by lambda)
        testSubstitute(parser, "rec fact. (λn. if n = 0 then 1 else n * (fact (n - 1)))", "n", "5");
        // rec fact. λn. if n = 0 then 1 else n * (fact (n - 1)) with x := 10 (x is not present)
        testSubstitute(parser, "rec fact. (λn. if n = 0 then 1 else n * (fact (n - 1)))", "x", "10");


        System.out.println("\n--- Substitution Tests Complete ---");
    }

    /**
     * Helper method to parse an expression, perform a substitution, and print the results.
     * @param parser The ProgParser instance.
     * @param originalExpression The original lambda calculus expression string.
     * @param varToSubstitute The name of the variable to substitute.
     * @param valueToSubstitute The expression string to substitute in place of the variable.
     */
    private static void testSubstitute(ProgParser parser, String originalExpression, String varToSubstitute, String valueToSubstitute) {
        try {
            Term parsedOriginalTerm = parser.parse(originalExpression);
            Term parsedValueTerm = parser.parse(valueToSubstitute);

            System.out.println("Original Expression: " + originalExpression);
            System.out.println("Parsed Original Term: " + parsedOriginalTerm);
            System.out.println("Substituting '" + varToSubstitute + "' with: " + parsedValueTerm);

            Term substitutedTerm = parsedOriginalTerm.substitute(varToSubstitute, parsedValueTerm);
            System.out.println("Resulting Term: " + substitutedTerm);
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.err.println("Error during substitution test for '" + originalExpression + "': " + e.getMessage());
            e.printStackTrace();
            System.out.println("-------------------------------------");
        }
    }
}
