package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.HashMap;
import java.util.Map;

public class Recursion extends Term {

    private String name;
    private Term body;

    public Recursion(String n, Term b)
    {
        name = n;
        body = b;
    }

    @Override
    public String toStringPrec(int prec)
    {
        return name + ". " + body.toStringPrec(prec);
    }
    @Override
    protected Type computeType(Map<String, Type> env) {
        Map<String, Type> newEnv = new HashMap<>(env);
        TVar recType = TVar.fresh();
        newEnv.put(name, recType);

        body.type(newEnv);
        Type bodyType = body.getType();

        Map<String, Type> sub = new Unifier().unify(recType, bodyType);
        if (sub == null) {
            throw new RuntimeException("Recursive definition type mismatch");
        }

        return bodyType;
    }

}
