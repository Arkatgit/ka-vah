package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.FType;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.TypeError;
import ca.brock.ca.interpreter.Unifier;

import java.util.Map;

public class Application extends Term {
    private Term function;
    private Term argument;

    private static final int precedence = 20;

    public Application(Term function, Term argument) {
        this.function = function;
        this.argument = argument;
    }

    public Term getFunction() {
        return function;
    }

    public Term getArgument(){
        return argument;
    }
    @Override
    public String toStringPrec(int prec) {
        // Case 1: Handle (* t) -> Application(Application(Constant("flip"), Constant("*")), r)
        if (function instanceof Application) {
            Application innerApp = (Application) function;

            if (innerApp.function instanceof Constant) {
                Constant firstConst = (Constant) innerApp.function;

                // Check if it's "flip op" where op is an infix operator
                if ("flip".equals(firstConst.toStringPrec(0))) {
                    if (innerApp.argument instanceof Constant) {
                        Constant opConst = (Constant) innerApp.argument;

                        if (opConst.canBeUsedAsSection()) {
                            // Format as (* t)
                            return "(" + opConst.toStringPrec(0) + " " + argument.toStringPrec(precedence + 1) + ")";
                        }
                    }
                }
            }
        }

        // Case 2: Handle (t *) -> Application(Constant("*"), l)
        if (function instanceof Constant) {
            Constant opConst = (Constant) function;

            if (opConst.isInfixOperator()) {
                // Format as (t *)
                return "(" + argument.toStringPrec(precedence) + " " + opConst.toStringPrec(0) + ")";
            }
        }

        // Case 3: Handle (*) t1 t2 -> Application(Application(Constant("*"), l), r)
        if (function instanceof Application) {
            Application innerApp = (Application) function;

            if (innerApp.function instanceof Constant) {
                Constant opConst = (Constant) innerApp.function;

                if (opConst.isInfixOperator()) {
                    // Format as t1 * t2 (infix notation)
                    return innerApp.argument.toStringPrec(precedence) + " " + opConst.toStringPrec(0) + " " + argument.toStringPrec(precedence + 1);
                }
            }
        }

        // Case 4: Handle standalone (*) -> Constant("*")
        if (function instanceof Constant) {
            Constant opConst = (Constant) function;

            if (opConst.isInfixOperator()) {
                // Format as (*)
                return "(" + opConst.toStringPrec(0) + ")";
            }
        }

        // Default function application behavior
        String result = function.toStringPrec(precedence) + " " + argument.toStringPrec(precedence + 1);

        // Ensure parentheses if precedence requires it
        if (prec > precedence) {
            result = "(" + result + ")";
        }

        return result;
    }
    @Override
    public void type(Map<String, Type> env) {
        function.type(env);
        argument.type(env);

        Unifier unifier = new Unifier();
        TVar resultType = TVar.fresh();  // Changed from new TVar()
        Map<String, Type> sub = unifier.unify(
            function.getType(),
            new FType(argument.getType(), resultType)
        );

        if (sub == null) throw new TypeError("Function application type mismatch");
        type = Unifier.applySubstitution(resultType, sub);
    }

}