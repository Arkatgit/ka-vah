package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorTranslator; // Import the helper
import ca.brock.cs.lambda.intermediate.IntermediateAbstraction;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.types.FType;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Abstraction extends Term {
    private String parameter;
    private Term body;

    // freshVarCounter is used for capture-avoiding substitution in Abstraction.substitute
    private static AtomicInteger freshVarCounter = new AtomicInteger();

    private static final int precedence = 10;

    public Abstraction(String parameter, Term body) {
        this.parameter = parameter;
        this.body = body;
    }

    public String getParameter() {
        return parameter;
    }

    public Term getBody() {
        return body;
    }

    @Override
    public String toStringPrec(int prec) {
        String result =  "λ" + parameter + ". " + body.toStringPrec(precedence);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        Map<String, Type> newEnv = new HashMap<>(env);
        TVar paramType = TVar.fresh();
        newEnv.put(parameter, paramType); // Add parameter to environment with a fresh type variable

        body.type(newEnv, unifier); // Type check the body with the new environment
        Type bodyType = body.getType();

        return new FType(paramType, bodyType); // Type of abstraction is (parameter's type -> body's type)
    }

    @Override
    public Term eval(Map<String, Term> env) {
        return this; // Abstractions are values, they don't reduce themselves
    }

    @Override
    public Term substitute(String varName, Term value) {
        // Rule 1: If the variable to substitute is the same as this abstraction's parameter,
        // then this abstraction binds 'varName', so no substitution occurs within its body.
        if (parameter.equals(varName)) {
            return this;
        }

        // Rule 2: If the variable to substitute is different from this abstraction's parameter,
        // we must check for variable capture.
        // If the 'value' being substituted contains 'parameter' as a free variable,
        // then 'parameter' must be renamed to avoid capture.
        if (value.getFreeVariables().contains(parameter)) {
            // Generate a fresh variable name
            String freshParameterName = parameter + "_" + freshVarCounter.incrementAndGet();
            Variable freshParameterTerm = new Variable(freshParameterName);

            // Recursively substitute the old parameter with the fresh parameter in the body
            Term renamedBody = body.substitute(parameter, freshParameterTerm);

            // Now, perform the original substitution (varName with value) on the renamed body
            // This ensures that the fresh parameter is not accidentally replaced if it happened to clash with varName
            Term substitutedBody = renamedBody.substitute(varName, value);

            // Return a new Abstraction with the fresh parameter and the substituted body
            return new Abstraction(freshParameterName, substitutedBody);
        } else {
            // No capture risk: proceed with substitution directly on the body
            return new Abstraction(parameter, body.substitute(varName, value));
        }
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = body.getFreeVariables();
        freeVars.remove(parameter); // Remove the parameter as it's bound by this abstraction
        return freeVars;
    }

    /**
     * Translates this lambda calculus term into a combinator calculus term using S-K-I combinators.
     * T[λx.M] needs to be handled by the CombinatorTranslator.
     * @return The equivalent Combinator term.
     */
    @Override
    public Combinator translate() {
        // Delegate the actual S-K-I translation logic to the CombinatorTranslator helper
        return CombinatorTranslator.translateAbstraction(parameter, body);
    }
    /**
     * Converts this Abstraction to an IntermediateAbstraction.
     * This is the first step in the two-phase translation to combinators.
     * @return The equivalent IntermediateAbstraction.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        // Recursively convert the body to an IntermediateTerm
        return new IntermediateAbstraction(parameter, body.toIntermediateTerm());
    }
}
