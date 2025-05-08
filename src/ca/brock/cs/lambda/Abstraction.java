package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.FType;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.Unifier;

import java.util.HashMap;
import java.util.Map;

public class Abstraction extends Term {
    private String parameter;
    private Term body;

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
        AppLogger.info("--- Abstraction: λ" + parameter + ". " + body);
        AppLogger.info("Initial environment: " + env);

        // Create new environment with parameter binding
        Map<String, Type> newEnv = new HashMap<>(env);
        TVar paramType = TVar.fresh();
        AppLogger.info("Fresh parameter type for " + parameter + ": " + paramType);
        newEnv.put(parameter, paramType);
        AppLogger.info("Environment after binding " + parameter + ": " + newEnv);

        AppLogger.info("Typing body: " + body + " with environment: " + newEnv);
        body.type(newEnv, unifier);
        Type bodyType = body.getType();
        AppLogger.info("Type of body: " + bodyType);
        AppLogger.info("Type of body Term node: " + body.type); // Check the Term node's type

        // Apply current substitutions to paramType and bodyType
        Type substitutedParamType = unifier.applySubstitution(paramType, unifier.getEnv());
        Type substitutedBodyType = unifier.applySubstitution(bodyType, unifier.getEnv());

        Type abstractionType = new FType(substitutedParamType, substitutedBodyType);
        AppLogger.info("Type of abstraction: " + abstractionType);
        return abstractionType;
    }
}