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
    public void type(Map<String, Type> env) {
        // Create a fresh type variable for the recursive binding
        TVar recType = TVar.fresh();

        // Extend the environment with the recursive binding
        Map<String, Type> newEnv = new HashMap<>(env);
        newEnv.put(name, recType);

        // Type check the body with the extended environment
        body.type(newEnv);

        // Unify the recursive type with the body's type
        Unifier unifier = new Unifier();
        Map<String, Type> sub = unifier.unify(recType, body.getType());

        if (sub == null) {
            throw new TypeError("Recursive definition type mismatch for " + name +
                "\nExpected: " + recType +
                "\nActual: " + body.getType());
        }

        // Apply the substitution to get the final type
        type = Unifier.applySubstitution(recType, sub);
    }

}
