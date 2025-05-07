package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeInferencer;

import java.util.HashMap;

public class TypeTest {
    public static void main(String[] args) {
        ProgParser parser = new ProgParser();
        TypeInferencer inferencer = new TypeInferencer();

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
            "5 + True",                        // Int
            "5 - 6",                            //  Int
            "True and False",               // Bool
            "(λx.x) 5",                     // Int
            "(λf.λx.f x) (λy.y)",           // a -> a
            "λx.λy.x",                     //  a → b → a
        };

        for (String test : tests) {
            try {
                Term term = parser.parse(test);
                term.type(new HashMap<>());
                Type type = term.getType();
                System.out.printf("%-30s => %s%n", test, type);
            } catch (Exception e) {
                System.out.printf("%-30s => ERROR: %s%n", test, e.getMessage());
            }
        }
    }
}