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
        // This method is called as T[λx.E] where 'this' is the Abstraction 'λx.E'.
        // So, 'x' is 'this.parameter' and 'E' is 'this.body'.
        // The body must first be processed by methodT() to remove any inner lambdas.
        IntermediateTerm transformedBody = body.methodT(optimize); // Pass optimize flag down
        return CombinatorTranslator.transformAbstraction(parameter, transformedBody, optimize); // Pass optimize flag down
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
