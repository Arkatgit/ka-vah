package ca.brock.cs.lambda;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.parser.ProgParser;
import ca.brock.cs.lambda.parser.Term;

import java.util.HashMap;
import java.util.Map;

public class CombinatorConversionTest {

    public static void main(String[] args) {
        ProgParser parser = new ProgParser();

        System.out.println("--- Testing Combinator Translation (translate method) ---");
        System.out.println("-------------------------------------------------------");

        // Test Case 1: Variables
        testTranslate(parser, "x");
        testTranslate(parser, "y");

        System.out.println("\n-------------------------------------------------------");

        // Test Case 2: Constants (Integer, Boolean, Operators)
        testTranslate(parser, "123");
        testTranslate(parser, "True");
        testTranslate(parser, "False");
        testTranslate(parser, "(+)");
        testTranslate(parser, "(-)");
        testTranslate(parser, "(*)");
        //testTranslate(parser, "(and)");
        //testTranslate(parser, "(not)");
        //testTranslate(parser, "(=)");

        System.out.println("\n-------------------------------------------------------");

        // Test Case 3: Application
       // testTranslate(parser, "f x"); // Should be (f x)
        //testTranslate(parser, "f (g x)"); // Should be (f (g x))
        //testTranslate(parser, "(f x) y"); // Should be ((f x) y)

        System.out.println("\n-------------------------------------------------------");

        // Test Case 4: Abstraction (Lambda Elimination)
        testTranslate(parser, "λx. x"); // Should translate to I
        testTranslate(parser, "λx. y"); // Should translate to (K y)
        testTranslate(parser, "λx. (x y)"); // Should translate to (S I (K y))
        testTranslate(parser, "λx. (y x)"); // Should translate to (S (K y) I)
        testTranslate(parser, "λx. (λy. x)"); // Should translate to (K I)
        testTranslate(parser, "λx. (λy. y)"); // Should translate to (K I)
        testTranslate(parser, "λx. (λy. (x y))"); // Should translate to (S (K I) I)
        testTranslate(parser, "λx. (λy. (y x))"); // Should translate to (S (K (S I)) (K I)) - more complex, verifies nested abstraction
        testTranslate(parser, "λx. (+) 2 x "); // Should translate to (S (K (S I)) (K I)) - more complex, verifies nested abstraction

        testTranslate(parser, "λx. λy. f x y");
        testTranslate(parser, "(λn. 2 * n + 3) 2 ");
        testTranslate(parser, "(λn.if n= 0 then 1 else 2) 1");


        System.out.println("\n-------------------------------------------------------");

        // Test Case 5: Binary Operations (Addition, Subtraction, Multiplication, And, Or, Equal, LessThanEqual)
        testTranslate(parser, "1 + 2"); // Should be ((+ 1) 2)
        testTranslate(parser, "a - b"); // Should be ((- a) b)
        testTranslate(parser, "x * y"); // Should be ((* x) y)
        testTranslate(parser, "True and False"); // Should be ((and True) False)
        testTranslate(parser, "A or B"); // Should be ((or A) B)
        testTranslate(parser, "5 = 5"); // Should be (( = 5) 5)
        testTranslate(parser, "a <= b"); // Should be ((<= a) b)

        System.out.println("\n-------------------------------------------------------");

        // Test Case 6: Unary Operation (Not)
        testTranslate(parser, "not True"); // Should be (not True)

        System.out.println("\n-------------------------------------------------------");

        // Test Case 7: Conditionals (assuming a generic IF combinator)
        testTranslate(parser, "if True then 10 else 20"); // Should be (((IF True) 10) 20)
        testTranslate(parser, "if (x = y) then (a + b) else (c - d)"); // Complex nested translation

        System.out.println("\n-------------------------------------------------------");

        // Test Case 8: Recursion (Y combinator application)
        // T[rec fact. (λn. if n = 0 then 1 else n * (fact (n - 1)))]
        String factorialLambda = "(λn. if n = 0 then 1 else n * (fact (n - 1)))";
        testTranslate(parser, "rec fact. " + factorialLambda); // Should be (Y (T[λn. ...]))

        System.out.println("\n-------------------------------------------------------");
        System.out.println("--- Test Cases for B and C Combinators ---");
        System.out.println("-------------------------------------------------------");

        // Test Case 9: B Combinator (T[λx.(E1 E2)] ⇒ (B T[E1] T[λx.E2]) if x is free in E2 but not E1)
        // Example: λx. (f (g x))  => B f (λx. g x) => B f (B g I)
        testTranslate(parser, "λx. (f (g x))");

        // Example: λx. (y (z x)) => B y (λx. z x) => B y (B z I)
        testTranslate(parser, "λx. (y (z x))");

        System.out.println("\n-------------------------------------------------------");

        // Test Case 10: C Combinator (T[λx.(E1 E2)] ⇒ (C T[λx.E1] T[E2]) if x is free in E1 but not E2)
        // Example: λx. (f x y) => C (λx. f x) y => C (B f I) y
        testTranslate(parser, "λx. (f x y)");

        // Example: λx. ( (f x) g) => C (λx. f x) g => C (B f I) g
        testTranslate(parser, "λx. ((f x) g)");

        System.out.println("\n--- Combinator Translation Tests Complete ---");
    }

    /**
     * Helper method to parse an expression and print its combinator translation.
     * @param parser The ProgParser instance.
     * @param expression The lambda calculus expression string.
     */
    private static void testTranslate(ProgParser parser, String expression) {
        try {
            Term parsedTerm = parser.parse(expression);
            System.out.println("Expression: " + expression);
            System.out.println("Parsed Term: " + parsedTerm);
            Combinator translatedCombinator = parsedTerm.toIntermediateTerm().methodT(false).toCombinatorTerm();
            System.out.println("Translated Combinator: " + translatedCombinator);
            System.out.println("-------------------------------------");
        } catch (Exception e) {
            System.err.println("Error translating '" + expression + "': " + e.getMessage());
            e.printStackTrace();
            System.out.println("-------------------------------------");
        }
    }
}
