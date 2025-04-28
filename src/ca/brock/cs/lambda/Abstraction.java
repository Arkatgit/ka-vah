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
    public void type(Map<String, Type> env) {
        TVar paramType = TVar.fresh();
        Map<String, Type> newEnv = new HashMap<>(env);
        newEnv.put(parameter, paramType);
        body.type(newEnv);
        type = new FType(paramType, body.getType());
    }

}