package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.combinators.CombinatorTranslator;
import ca.brock.cs.lambda.combinators.YCombinator;
import ca.brock.cs.lambda.intermediate.IntermediateRecursion;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Recursion extends Term {

    private String name;
    private Term body;

    public Recursion(String n, Term b)
    {
        name = n;
        body = b;
    }

    public String getName() {
        return name;
    }
    public Term getBody()
    {
        return body;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return name + ". " + body.toStringPrec(prec);
    }
    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        Map<String, Type> newEnv = new HashMap<>(env);
        TVar recType = TVar.fresh();
        newEnv.put(name, recType);

        body.type(newEnv, unifier); // Pass down the Unifier
        Type bodyType = body.getType();

        Map<TVar, Type> sub = unifier.unify(recType, bodyType); // Use the provided Unifier
        if (sub == null) {
            throw new RuntimeException("Recursive definition type mismatch");
        }

        return bodyType;
    }

    @Override
    public Term eval(Map<String, Term> env) {
        // For recursion, substitute 'name' with the entire 'Recursion' term into the body
        // and then evaluate the body. This allows for fixed-point iteration.
        Map<String, Term> newEnv = new HashMap<>(env);
        newEnv.put(name, this); // 'this' refers to the current Recursion term
        return body.eval(newEnv);
    }

    @Override
    public Term substitute(String varName, Term value) {
        if (name.equals(varName)) {
            return this; // If the variable to substitute is the name of this recursion, it's bound.
        }
        // Handle potential capture when substituting in the body:
        // If 'value' contains the recursive 'name', we might need to rename 'name'.
        // This is a complex case often handled by explicit fixed-point combinators (Y-combinator)
        // or by a more advanced recursion definition. For simple recursion,
        // we'll apply the same capture-avoiding logic as abstraction.
        Set<String> freeVarsInValue = value.getFreeVariables();
        if (freeVarsInValue.contains(name)) {
            // This is a subtle point for recursion. Typically, 'rec x. M' is syntactic sugar
            // for Y (λx. M). The 'x' in M is bound. If 'value' has 'x' as a free variable,
            // substituting into M shouldn't rename x in M.
            // However, if we're substituting some *other* variable, say 'y', with a 'value'
            // that contains 'x', then we must avoid capture by renaming 'x' within the recursion.
            // But 'x' is already bound by the 'rec'.
            // For now, let's stick to the simpler rule: if the variable *we are substituting* (varName)
            // is not the bound 'name', then substitute in the body directly.
            // If the recursive variable 'name' happens to be free in 'value', that's usually
            // an issue with the program's structure, not the substitution rule itself.

            // Given the original simplified `Recursion.substitute`, and the typical semantics
            // of `rec x. M`, the variable `x` is bound within `M`.
            // The capture avoidance rules for `Abstraction` are appropriate because `λx. M`
            // defines a new scope. `rec x. M` is effectively `M[x := rec x. M]`.
            // When substituting `y` with `V` in `rec x. M`, we substitute `y` in `M`.
            // If `V` has `x` free, `x` is *not* free in `rec x. M`, it's bound by `rec`.
            // So, renaming `x` in the context of `rec` is usually not needed for capture avoidance
            // because `x` is *already bound*.
            // The simpler `return new Recursion(name, body.substitute(varName, value));` is often correct.
            // Let's revert to that for Recursion, as its binding mechanism is different from lambda abstraction.
            return new Recursion(name, body.substitute(varName, value));
        } else {
            return new Recursion(name, body.substitute(varName, value));
        }
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = body.getFreeVariables();
        freeVars.remove(name); // The 'name' is bound by the 'rec' construct
        return freeVars;
    }

    @Override
    public Combinator translate() {
        // T[rec x. M] = Y (λx.M)
        // Here, λx.M needs to be translated first, which will be done by CombinatorTranslator.translateAbstraction.
        Combinator translatedLambdaBody = CombinatorTranslator.translateAbstraction(name, body);
        return new CombinatorApplication(new YCombinator(), translatedLambdaBody);
    }

    /**
     * Converts this Recursion term to an IntermediateRecursion.
     * This is the first step in the two-phase translation to combinators.
     * @return The equivalent IntermediateRecursion.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        // Recursively convert the body to an IntermediateTerm
        return new IntermediateRecursion(name, body.toIntermediateTerm());
    }


}
