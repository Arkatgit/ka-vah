package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.ICombinator;
import ca.brock.cs.lambda.combinators.KCombinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.CombinatorTranslator; // Import the static helper
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a lambda abstraction (λx.E) in the intermediate representation.
 * This class is the primary target for the T[] transformation to eliminate abstractions.
 */
public class IntermediateAbstraction extends IntermediateTerm {
    private String parameter;
    private IntermediateTerm body;

    private static AtomicInteger freshVarCounter = new AtomicInteger(); // For capture avoidance during methodT

    public IntermediateAbstraction(String parameter, IntermediateTerm body) {
        this.parameter = parameter;
        this.body = body;
    }

    public String getParameter() {
        return parameter;
    }

    public IntermediateTerm getBody() {
        return body;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "λ" + parameter + ". " + body.toStringPrec(10); // Precedence for abstraction body
        if (prec > 10) { // Abstraction typically has low precedence
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = body.getFreeVariables();
        freeVars.remove(parameter); // The parameter is bound by this abstraction
        return freeVars;
    }

//    /**
//     * Applies the T[] transformation rules to eliminate this lambda abstraction.
//     * This method implements the core rules for T[λx.E].
//     * It calls a static helper in CombinatorTranslator to apply the specific abstraction elimination rules.
//     * @return An IntermediateTerm with this layer of abstraction eliminated.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        // This method is called as T[λx.E] where 'this' is the Abstraction 'λx.E'.
//        // So, 'x' is 'this.parameter' and 'E' is 'this.body'.
//        // The body must first be processed by methodT() to remove any inner lambdas.
//        IntermediateTerm transformedBody = body.methodT();
//        return CombinatorTranslator.transformAbstraction(parameter, transformedBody);
//    }

    /**
     * Applies the T[] transformation rules to eliminate this lambda abstraction.
     * This method implements the core rules for T[λx.E].
     * It calls a static helper in CombinatorTranslator to apply the specific abstraction elimination rules.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return An IntermediateTerm with this layer of abstraction eliminated.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
//        // This method is called as T[λx.E] where 'this' is the Abstraction 'λx.E'.
//        // So, 'x' is 'this.parameter' and 'E' is 'this.body'.
//        // The body must first be processed by methodT() to remove any inner lambdas.
//        IntermediateTerm transformedBody = body.methodT(optimize); // Pass optimize flag down
//        return CombinatorTranslator.transformAbstraction(parameter, transformedBody, optimize); // Pass optimize flag down
        // First, recursively apply methodT to the body to ensure inner lambdas are eliminated first.
        IntermediateTerm transformedBody = body.methodT(optimize);

        // Rule 1: T[λx.x] -> I
        if (transformedBody instanceof IntermediateVariable) {
            if (((IntermediateVariable) transformedBody).getName().equals(parameter)) {
                return new IntermediateICombinator();
            }
        }

        // Rule 2: T[λx.E] -> (K T[E]) if x is not free in E
        // This is a crucial base case that handles all constants and other terms where the
        // abstracted variable is not used.
        if (!transformedBody.getFreeVariables().contains(parameter)) {
            return new IntermediateApplication(new IntermediateKCombinator(), transformedBody);
        }

        // Rule 3-5: T[λx.(E1 E2)] -> ...
        if (transformedBody instanceof IntermediateApplication) {
            IntermediateApplication application = (IntermediateApplication) transformedBody;
            IntermediateTerm func = application.getFunction();
            IntermediateTerm arg = application.getArgument();

            boolean xFreeInFunc = func.getFreeVariables().contains(parameter);
            boolean xFreeInArg = arg.getFreeVariables().contains(parameter);

            // Optimization 1 (Rule 4): T[λx.(E1 E2)] -> (C T[λx.E1] T[E2]) if x is free in E1 but not E2
            // This rule is only applied if the 'optimize' flag is true.
            if (optimize && xFreeInFunc && !xFreeInArg) {
                IntermediateTerm transformedFunc = new IntermediateAbstraction(parameter, func).methodT(optimize);
                return new IntermediateApplication(
                    new IntermediateApplication(new IntermediateCCombinator(), transformedFunc),
                    arg
                );
            }

            // Optimization 2 (Rule 5): T[λx.(E1 E2)] -> (B T[E1] T[λx.E2]) if x is free in E2 but not E1
            // This rule is also only applied if the 'optimize' flag is true.
            if (optimize && !xFreeInFunc && xFreeInArg) {
                IntermediateTerm transformedArg = new IntermediateAbstraction(parameter, arg).methodT(optimize);
                return new IntermediateApplication(
                    new IntermediateApplication(new IntermediateBCombinator(), func),
                    transformedArg
                );
            }

            // Default Application Rule (Rule 3): T[λx.(E1 E2)] -> (S T[λx.E1] T[λx.E2])
            // This is the most general rule and is used when the above optimizations don't apply,
            // or if the 'optimize' flag is false.
            IntermediateTerm transformedFunc = new IntermediateAbstraction(parameter, func).methodT(optimize);
            IntermediateTerm transformedArg = new IntermediateAbstraction(parameter, arg).methodT(optimize);
            return new IntermediateApplication(
                new IntermediateApplication(new IntermediateSCombinator(), transformedFunc),
                transformedArg
            );
        }

        // This case should theoretically not be reached if all abstractions have been handled
        // recursively, but serves as a failsafe.
        // If the body is a single term where 'x' is free, but it's not an application,
        // we can't reduce it further with the standard rules. This could happen with
        // an expression like `λx.(x)`.
        return this;

    }



    /**
     * An IntermediateAbstraction cannot be directly converted to a CombinatorTerm
     * because it still contains a lambda abstraction.
     * This method should only be called AFTER methodT() has been applied to eliminate all abstractions.
     * @throws IllegalStateException if called before abstraction elimination.
     */
    @Override
    public Combinator toCombinatorTerm() {
        throw new IllegalStateException("Cannot convert IntermediateAbstraction to CombinatorTerm. Run methodT() first.");
    }
}
