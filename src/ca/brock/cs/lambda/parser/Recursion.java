package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

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

}
