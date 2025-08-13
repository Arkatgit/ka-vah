package ca.brock.cs.lambda;

import ca.brock.cs.lambda.intermediate.IntermediateApplication;
import ca.brock.cs.lambda.intermediate.IntermediateAbstraction;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.intermediate.IntermediateVariable;
import ca.brock.cs.lambda.intermediate.IntermediateConstant;
import ca.brock.cs.lambda.intermediate.IntermediateICombinator;
import ca.brock.cs.lambda.intermediate.IntermediateKCombinator;
import ca.brock.cs.lambda.intermediate.IntermediateSCombinator;
import ca.brock.cs.lambda.intermediate.IntermediateBCombinator;
import ca.brock.cs.lambda.intermediate.IntermediateCCombinator;

import java.util.Set;


/**
 * A static helper class responsible for the second phase of the translation:
 * eliminating lambda abstractions from IntermediateTerms to produce CombinatorTerms.
 * This implements the T[] transformation rules.
 */
public class CombinatorTranslator {

    /**
     * Applies the T[] transformation rules for lambda abstraction elimination.
     * This method is called by IntermediateAbstraction.methodT().
     *
     * T[λx.E] rules:
     * 1. T[λx.x] ⇒ I
     * 2. T[λx.E] ⇒ (K T[E]) (if x is not free in E)
     * 3. T[λx.(E1 E2)] ⇒ (S T[λx.E1] T[λx.E2]) (if x is free in both E1 and E2)
     * 4. T[λx.(E1 E2)] ⇒ (C T[λx.E1] T[E2]) (if x is free in E1 but not E2) - OPTIMIZED
     * 5. T[λx.(E1 E2)] ⇒ (B T[E1] T[λx.E2]) (if x is free in E2 but not E1) - OPTIMIZED
     *
     * @param parameter The variable being abstracted (x).
     * @param body The body of the abstraction (E), which has already had its inner lambdas eliminated by methodT().
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return An IntermediateTerm representing the combinator form of the abstraction.
     */
    public static IntermediateTerm transformAbstraction(String parameter, IntermediateTerm body, boolean optimize) {
        // Rule 1: T[λx.x] ⇒ I
        if (body instanceof IntermediateVariable && ((IntermediateVariable) body).getName().equals(parameter)) {
            return new IntermediateICombinator();
        }

        // Rule 2: T[λx.E] ⇒ (K T[E]) (if x is not free in E)
        Set<String> bodyFreeVars = body.getFreeVariables();
        if (!bodyFreeVars.contains(parameter)) {
            return new IntermediateApplication(new IntermediateKCombinator(), body); // body here is already T[E]
        }

        // Handle T[λx.(E1 E2)] cases
        if (body instanceof IntermediateApplication) {
            IntermediateApplication appBody = (IntermediateApplication) body;
            IntermediateTerm E1 = appBody.getFunction();
            IntermediateTerm E2 = appBody.getArgument();

            Set<String> E1FreeVars = E1.getFreeVariables();
            Set<String> E2FreeVars = E2.getFreeVariables();

            boolean xFreeInE1 = E1FreeVars.contains(parameter);
            boolean xFreeInE2 = E2FreeVars.contains(parameter);

            // Optimization Rules (B and C) - only applied if optimize is true
            if (optimize) {
                // Rule 4: T[λx.(E1 E2)] ⇒ (C T[λx.E1] T[E2]) (if x is free in E1 but not E2)
                if (xFreeInE1 && !xFreeInE2) {
                    IntermediateTerm transformedE1 = new IntermediateAbstraction(parameter, E1).methodT(optimize); // Pass optimize
                    IntermediateTerm transformedE2 = E2; // E2 is already T[E2] from the recursive methodT call
                    return new IntermediateApplication(
                        new IntermediateApplication(new IntermediateCCombinator(), transformedE1),
                        transformedE2
                    );
                }
                // Rule 5: T[λx.(E1 E2)] ⇒ (B T[E1] T[λx.E2]) (if x is free in E2 but not E1)
                else if (!xFreeInE1 && xFreeInE2) {
                    IntermediateTerm transformedE1 = E1; // E1 is already T[E1] from the recursive methodT call
                    IntermediateTerm transformedE2 = new IntermediateAbstraction(parameter, E2).methodT(optimize); // Pass optimize
                    return new IntermediateApplication(
                        new IntermediateApplication(new IntermediateBCombinator(), transformedE1),
                        transformedE2
                    );
                }
            }

            // Default Rule for Applications (S Combinator) - applies if not optimized, or if both E1 and E2 need x
            // This covers Rule 3 (x free in both) and acts as fallback if B/C don't apply due to optimize=false
            // It also covers cases where x is free in neither E1 nor E2, but that should ideally be caught by Rule 2.
            // However, to be safe, if we're in an Application and Rule 2 wasn't hit, S is the general fallback.
            // This condition ensures we only apply S if 'x' is relevant to the application (i.e., free in E1 or E2).
            if (xFreeInE1 || xFreeInE2) {
                IntermediateTerm transformedE1 = new IntermediateAbstraction(parameter, E1).methodT(optimize); // Pass optimize
                IntermediateTerm transformedE2 = new IntermediateAbstraction(parameter, E2).methodT(optimize); // Pass optimize
                return new IntermediateApplication(
                    new IntermediateApplication(new IntermediateSCombinator(), transformedE1),
                    transformedE2
                );
            }
        }

        // This fallback should ideally not be reached if rules are exhaustive and correctly applied.
        // If it is reached, it indicates a term structure that doesn't fit the specified lambda elimination rules.
        throw new IllegalStateException("Unhandled abstraction transformation case for parameter '" + parameter + "' and body '" + body + "'. This indicates a missing rule or logic error.");
    }
}