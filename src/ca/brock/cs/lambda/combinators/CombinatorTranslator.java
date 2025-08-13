package ca.brock.cs.lambda.combinators;

import ca.brock.cs.lambda.parser.*; // Import all parser terms
import java.util.Set;

public class CombinatorTranslator {

    /**
     * Translates a lambda abstraction λx.M into its equivalent S-K-I combinator form.
     * This method recursively processes the structure of M.
     *
     * @param x The variable being abstracted.
     * @param M The body of the lambda abstraction (a Term).
     * @return The equivalent Combinator term.
     */
    public static Combinator translateAbstraction(String x, Term M) {
        // Rule 1: T[λx.x] = I
        if (M instanceof Variable && ((Variable) M).getName().equals(x)) {
            return new ICombinator();
        }

        // Rule 2: T[λx.M] = K (T[M]) if x is not free in M
        // This covers Constants, IntegerLiterals, BooleanLiterals, and any other
        // term where the abstracted variable 'x' does not appear freely.
        // This also applies to Recursion terms, as their bound variable is internal,
        // so `x` should generally not be free in a `rec` term.
        if (!M.getFreeVariables().contains(x)) {
            return new CombinatorApplication(new KCombinator(), M.translate()); // M.translate() ensures M is a Combinator
        }

        // Rule 3: T[λx.(A B)] = S (T[λx.A]) (T[λx.B])
        if (M instanceof Application) {
            Application app = (Application) M;
            Combinator translatedFunction = translateAbstraction(x, app.getFunction());
            Combinator translatedArgument = translateAbstraction(x, app.getArgument());
            return new CombinatorApplication(new CombinatorApplication(new SCombinator(), translatedFunction), translatedArgument);
        }

        // Rule 4: Handling composite terms that are NOT applications (e.g., Abstraction, Addition, Conditional)
        // If x is free in M, and M is not a simple variable 'x' or an 'Application',
        // we must decompose M based on its structure and apply S-K-I rules recursively.
        // This effectively "lifts" the lambda abstraction over the structure of M.

        if (M instanceof Abstraction) {
            Abstraction innerAbs = (Abstraction) M;
            // First, translate the inner abstraction (λy.P) to its combinator form.
            // This will recursively call translateAbstraction on the inner body.
            Combinator innerTranslatedCombinator = CombinatorTranslator.translateAbstraction(innerAbs.getParameter(), innerAbs.getBody());

            // Now, we need to abstract `x` over this `innerTranslatedCombinator`.
            // This is equivalent to applying the S-K-I rules to `λx. C_inner`.
            // The `translateAbstractionCombinator` helper is exactly for this.
            return translateAbstractionCombinator(x, innerTranslatedCombinator);
        }

        if (M instanceof Addition) {
            Addition add = (Addition) M;
            Combinator op = new CombinatorConstant("+");
            Combinator translatedLeft = translateAbstraction(x, add.getLeft());
            Combinator translatedRight = translateAbstraction(x, add.getRight());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.+] = K +
                ),
                new CombinatorApplication(
                    new CombinatorApplication(new SCombinator(), translatedLeft),
                    translatedRight
                )
            );
        }
        if (M instanceof Subtraction) {
            Subtraction sub = (Subtraction) M;
            Combinator op = new CombinatorConstant("-");
            Combinator translatedLeft = translateAbstraction(x, sub.getLeft());
            Combinator translatedRight = translateAbstraction(x, sub.getRight());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.-] = K -
                ),
                new CombinatorApplication(
                    new CombinatorApplication(new SCombinator(), translatedLeft),
                    translatedRight
                )
            );
        }
        if (M instanceof Multiplication) {
            Multiplication mul = (Multiplication) M;
            Combinator op = new CombinatorConstant("*");
            Combinator translatedLeft = translateAbstraction(x, mul.getLeft());
            Combinator translatedRight = translateAbstraction(x, mul.getRight());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.*] = K *
                ),
                new CombinatorApplication(
                    new CombinatorApplication(new SCombinator(), translatedLeft),
                    translatedRight
                )
            );
        }
        if (M instanceof Equal) {
            Equal eq = (Equal) M;
            Combinator op = new CombinatorConstant("=");
            Combinator translatedLeft = translateAbstraction(x, eq.getLeft());
            Combinator translatedRight = translateAbstraction(x, eq.getRight());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.=] = K =
                ),
                new CombinatorApplication(
                    new CombinatorApplication(new SCombinator(), translatedLeft),
                    translatedRight
                )
            );
        }
        if (M instanceof LEqual) {
            LEqual le = (LEqual) M;
            Combinator op = new CombinatorConstant("<=");
            Combinator translatedLeft = translateAbstraction(x, le.getLeft());
            Combinator translatedRight = translateAbstraction(x, le.getRight());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.<=] = K <=
                ),
                new CombinatorApplication(
                    new CombinatorApplication(new SCombinator(), translatedLeft),
                    translatedRight
                )
            );
        }
        if (M instanceof And) {
            And and = (And) M;
            Combinator op = new CombinatorConstant("and");
            Combinator translatedLeft = translateAbstraction(x, and.getLeft());
            Combinator translatedRight = translateAbstraction(x, and.getRight());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.and] = K and
                ),
                new CombinatorApplication(
                    new CombinatorApplication(new SCombinator(), translatedLeft),
                    translatedRight
                )
            );
        }
        if (M instanceof Or) {
            Or or = (Or) M;
            Combinator op = new CombinatorConstant("or");
            Combinator translatedLeft = translateAbstraction(x, or.getLeft());
            Combinator translatedRight = translateAbstraction(x, or.getRight());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.or] = K or
                ),
                new CombinatorApplication(
                    new CombinatorApplication(new SCombinator(), translatedLeft),
                    translatedRight
                )
            );
        }
        if (M instanceof Not) {
            Not not = (Not) M;
            Combinator op = new CombinatorConstant("not");
            Combinator translatedOperand = translateAbstraction(x, not.getOperand());
            return new CombinatorApplication(
                new CombinatorApplication(
                    new SCombinator(),
                    new CombinatorApplication(new KCombinator(), op) // T[λx.not] = K not
                ),
                translatedOperand
            );
        }
        if (M instanceof Conditional) {
            Conditional condTerm = (Conditional) M;
            // A more standard translation for `if C then T else F` is `C T F`
            // assuming `C` evaluates to a Church Boolean (λt.λf.t for True, λt.λf.f for False).
            // So, `T[λx. (C T F)] = S (T[λx.C]) (S (T[λx.T]) (T[λx.F]))`

            Combinator translatedCondition = translateAbstraction(x, condTerm.getCondition());
            Combinator translatedTrueBranch = translateAbstraction(x, condTerm.getTrueBranch());
            Combinator translatedFalseBranch = translateAbstraction(x, condTerm.getFalseBranch());

            return new CombinatorApplication(
                new CombinatorApplication(
                    translatedCondition,
                    translatedTrueBranch
                ),
                translatedFalseBranch
            );
        }
        // The `if (M instanceof Recursion)` block is removed as it's redundant and could cause NPEs.
        // Recursion terms (like `rec y. M'`) do not typically have `x` as a free variable bound by `rec`.
        // Thus, they should fall under Rule 2 (`K (T[M])`).

        // If none of the specific rules above apply, it indicates a structural mismatch
        // or a term type that the S-K-I translation isn't designed to directly deconstruct.
        // This should ideally not be reached if all Term types are handled correctly.
        throw new UnsupportedOperationException("Unsupported term type for lambda abstraction translation: λ" + x + "." + M.getClass().getSimpleName() + " (Free vars in M: " + M.getFreeVariables() + ")");
    }

    /**
     * Helper to translate a combinator term with respect to an abstraction.
     * T[λx. C] where C is a Combinator term.
     * @param x The variable being abstracted.
     * @param C The Combinator term (body of the lambda abstraction).
     * @return The equivalent Combinator term after abstraction elimination.
     */
    public static Combinator translateAbstractionCombinator(String x, Combinator C) {
        // Rule: T[λx.x] = I (if C is CombinatorVariable x)
        if (C instanceof CombinatorVariable && ((CombinatorVariable) C).getName().equals(x)) {
            return new ICombinator();
        }

        // Optimization: T[λx. (F x)] = F, if x is not free in F
        // This handles cases like λx. (K x) -> K, or λx. ((+ 2) x) -> (+ 2)
        // This makes the translation more concise for common patterns, especially for λx. (K x) -> K.
        if (C instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) C;
            Combinator F = app.getFunction(); // The function part of the application (F in F x)
            Combinator Arg = app.getArgument(); // The argument part of the application (x in F x)

            if (Arg instanceof CombinatorVariable && ((CombinatorVariable) Arg).getName().equals(x)) {
                // If the argument is the variable 'x' we are abstracting,
                // and 'x' is NOT free in the function 'F', then we can apply the optimization.
                if (!F.getFreeVariables().contains(x)) {
                    return F; // Optimization applied: T[λx. (F x)] = F
                }
            }
        }

        // Rule: T[λx.C] = K (C) if x is not free in C (This rule should be after the optimization for Fx pattern)
        if (!C.getFreeVariables().contains(x)) {
            return new CombinatorApplication(new KCombinator(), C);
        }


        // Fallback to S rule for other applications
        if (C instanceof CombinatorApplication) { // Re-check after optimization attempts (if not optimized, it falls here)
            CombinatorApplication app = (CombinatorApplication) C;
            Combinator translatedFunction = translateAbstractionCombinator(x, app.getFunction());
            Combinator translatedArgument = translateAbstractionCombinator(x, app.getArgument());
            return new CombinatorApplication(new CombinatorApplication(new SCombinator(), translatedFunction), translatedArgument);
        }

        // Other combinator types (K, S, I, Y, Constant) should not have free variables.
        // If they did, they would have been caught by the `!C.getFreeVariables().contains(x)` rule.
        throw new UnsupportedOperationException("Unsupported combinator type within abstraction: λ" + x + "." + C.getClass().getSimpleName() + " (Free vars in C: " + C.getFreeVariables() + ")");
    }
}
