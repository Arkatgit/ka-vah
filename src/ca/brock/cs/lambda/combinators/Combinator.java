package ca.brock.cs.lambda.combinators;

import java.util.Map;
import java.util.Set;

public abstract class Combinator {

    public abstract String toStringPrec(int prec);

    @Override
    public String toString() {
        return toStringPrec(0);
    }

    /**
     * Evaluates the combinator expression to its normal form.
     * @param env The environment for evaluating free variables (though combinator calculus usually has no free variables,
     * this can be used for constants or external bindings if extended).
     * @return The evaluated Combinator term.
     */
    public abstract Combinator eval(Map<String, Combinator> env);

    /**
     * Finds the free variables in the combinator term.
     * While pure combinator calculus doesn't have free variables in the lambda calculus sense,
     * if you introduce external constants or variables, this would be useful.
     * For basic S, K, I, it will return an empty set.
     * @return A set of free variable names.
     */
    public abstract Set<String> getFreeVariables(); // Will implement this later

    /**
     * Substitutes a combinator term for a variable name within this combinator term.
     * This is generally less common in combinator calculus as variables are not explicit in the same way.
     * But for potential extensions or if we introduce variables as stand-ins.
     */
    public abstract Combinator substitute(String varName, Combinator value); // Will implement this later

}
