package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.FType;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.Unifier;

import java.util.HashMap;
import java.util.Map;

public class TypeTest {
    public static void main(String[] args) {
        ProgParser parser = new ProgParser();

        // Test cases
        String[] tests = {
            "5",                            // Int
            "True",                         // Bool
            "λx.x",                         // a -> a
            "λf.λx.f (f x)",                // (a -> a) -> a -> a
            "λx.λy.x",                     // a -> b -> a
            "λf.λg.λx.f (g x)",             // (b -> c) -> (a -> b) -> a -> c
            "if True then 5 else 10",       // Int
            "not False",                    // Bool
            "5 + True",                        // ERROR: Left operand must be an integer, but got: Constant(Bool)
            "5 - 6",                            // Int
            "True and False",               // Bool
            "(λx.x) 5",                     // Int
            "(λf.λx.f x) (λy.y)",           // a -> a
            "λx.λy.x",                     //  a → b → a
            "λx.λy.(+ x y)",                // Int -> Int -> Int
            "λx.(if x then True else False)", // Bool -> Bool
            "λf.(f True)",                   // (Bool -> a) -> a
            "λf.λg.λx.(f x) (g x)",         // (a -> b) -> (a -> (b -> c)) -> a -> c
            "λx.(not x)",                   // Bool -> Bool
            "λx.λy.(x = y)",                // a -> a -> Bool
            "λx.λy.(x <= y)",               // Int -> Int -> Bool
            "rec fact. λn.(if (n = 0) then 1 else ( (*)  n (fact ( n -  1))))", // Int -> Int
        };

        for (String test : tests) {
            try {
                Term term = parser.parse(test);
                Map<String, Type> initialEnv = new HashMap<>();
                Unifier finalUnifier = new Unifier(); // Create a single Unifier

                term.type(initialEnv, finalUnifier); // Pass the Unifier
                Type inferredType = term.getType();

                System.out.printf("%-60s => %s%n", test, inferredType);

            } catch (Exception e) {
                System.out.printf("%-60s => ERROR: %s%n", test, e.getMessage());
            }
        }
    }
}