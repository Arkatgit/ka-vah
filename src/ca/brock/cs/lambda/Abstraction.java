package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.FType;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;

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
    protected Type computeType(Map<String, Type> env) {
        // Create new environment with parameter binding
        Map<String, Type> newEnv = new HashMap<>(env);
        TVar paramType = TVar.fresh();
        newEnv.put(parameter, paramType);

        // Type the body with the new environment
        body.type(newEnv);
        Type bodyType = body.getType();

        return new FType(paramType, bodyType);
    }

}