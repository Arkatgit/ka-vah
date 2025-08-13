package ca.brock.cs.lambda.intermediate;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.YCombinator;
import java.util.HashSet;
import java.util.Set;

public class IntermediateRecursion extends IntermediateTerm {
    private String name;
    private IntermediateTerm body;

    private static final int precedence = 5; // Low precedence for rec

    public IntermediateRecursion(String name, IntermediateTerm body) {
        this.name = name;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public IntermediateTerm getBody() {
        return body;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = "rec " + name + ". " + body.toStringPrec(precedence);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = body.getFreeVariables();
        freeVars.remove(name); // The name is bound by this recursion
        return freeVars;
    }

//    /**
//     * For recursion, methodT() recursively calls methodT() on its body.
//     * The `rec` itself is a binding construct, so the abstraction elimination
//     * will apply to the lambda that the `rec` effectively creates.
//     * @return A new IntermediateRecursion with transformed body.
//     */
//    @Override
//    public IntermediateTerm methodT() {
//        // The T[] transformation is applied to the body.
//        // The 'rec' itself is a binding form, and its translation to Y (lambda)
//        // happens at the toCombinatorTerm() stage.
//        return new IntermediateRecursion(name, body.methodT());
//    }
    /**
     * For recursion, methodT() recursively calls methodT() on its body.
     * The `rec` itself is a binding construct, so the abstraction elimination
     * will apply to the lambda that the `rec` effectively creates.
     * @param optimize A flag indicating whether to apply optimizations (e.g., B and C combinators).
     * @return A new IntermediateRecursion with transformed body.
     */
    @Override
    public IntermediateTerm methodT(boolean optimize) {
        // The T[] transformation is applied to the body.
        // The 'rec' itself is a binding form, and its translation to Y (lambda)
        // happens at the toCombinatorTerm() stage.
        return new IntermediateRecursion(name, body.methodT(optimize)); // Pass optimize flag down
    }

    /**
     * Converts this IntermediateRecursion to a CombinatorApplication using the Y combinator.
     * T[rec x. M] = Y (T[λx.M])
     * @return The equivalent Combinator term.
     */
    @Override
    public Combinator toCombinatorTerm() {
        // The body of the recursion is effectively lambda-abstracted over the recursive variable 'name'.
        // This is where the Y combinator is introduced.
        // The body has already been processed by methodT() to remove inner lambdas.
        // Now, we abstract 'name' over the *transformed* body.
        // Note: The `methodT` call here should use `false` for `optimize` if we want
        // a consistent translation of the Y-combinator's internal lambda,
        // or it should be passed from the context if `Y` itself can be optimized.
        // For standard Y-combinator definition, the internal lambda is usually translated without B/C.
        // However, if the overall translation is optimized, it should propagate.
        // For simplicity and consistency, let's assume the optimize flag is propagated.
        IntermediateAbstraction lambdaEquivalent = new IntermediateAbstraction(name, body);
        // The `methodT` here needs to be called with the same `optimize` flag as the current context.
        // However, `toCombinatorTerm` doesn't receive `optimize`. This highlights a design choice.
        // For now, let's assume `methodT` on `lambdaEquivalent` will be called with `true` if the overall
        // translation is optimized. A more robust solution might involve passing `optimize` to `toCombinatorTerm`
        // or having a global configuration. For now, let's assume it's part of the `methodT` chain.
        // Since `body` was already processed by `methodT(optimize)`, its internal structure is already optimized.
        // The `IntermediateAbstraction` constructor creates a new abstraction, and its `methodT` will then apply
        // the rules with the `optimize` flag.
        Combinator translatedLambda = lambdaEquivalent.methodT(true).toCombinatorTerm(); // Assuming optimization for Y's internal lambda

        return new CombinatorApplication(new YCombinator(), translatedLambda);
    }
}
