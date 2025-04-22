package ca.brock.ca.interpreter;

import ca.brock.cs.lambda.ProgParser;
import ca.brock.cs.lambda.Term;

import java.util.Map;

public class Main {
    public static void main(String[] args) {

        // In your main application
        ProgParser parser = new ProgParser();
        TypeInferencer inferencer = new TypeInferencer();

        Term expr1 = parser.parse("λx. λx.( x + 2) + x");
        Map<String, Type> typeSubst1 = inferencer.infer(expr1);

        System.out.println("Inferred type substitution: " + typeSubst1);

        // Infer types for an expression
        Term expr2 = parser.parse("(*) 2 3");
        Map<String, Type> typeSubst2 = inferencer.infer(expr2);

        System.out.println("Inferred type substitution: " + typeSubst2);

//        // Define some types
//        TVar alpha = new TVar("α");
//        TVar beta = new TVar("β");
//        FType funcType1 = new FType(alpha, beta);
//        FType funcType2 = new FType(new Constant("Int"), alpha);
//
//        // Create a unifier
//        Unifier unifier = new Unifier();
//
//        // Unify the types
//        Map<String, Type> result = unifier.unify(funcType1, funcType2);
//
//        // Print the result
//        if (result.) {
//            System.out.println("Unification succeeded!");
//            System.out.println("Substitution: " + unifier.getEnv());
//        } else {
//            System.out.println("Unification failed.");
//        }
    }
}