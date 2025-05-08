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
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        AppLogger.info("--- Application: " + function + " " + argument);
        AppLogger.info("Environment: " + env);

        AppLogger.info("Typing function: " + function + " with environment: " + env);
        function.type(env, unifier);
        Type funType = function.getType();
        AppLogger.info("Type of function Term node: " + function.type); // Check the Term node's type

        AppLogger.info("Typing argument: " + argument + " with environment: " + env);
        argument.type(env, unifier);
        Type argType = argument.getType();
        AppLogger.info("Type of argument Term node: " + argument.type); // Check the Term node's type

        TVar resultType = TVar.fresh();
        AppLogger.info("Fresh result type for application: " + resultType);
        AppLogger.info("Type of function (before unify): " + funType);
        AppLogger.info("Type of argument (before unify): " + argType);
        AppLogger.info("Expected function type: (" + argType + " → " + resultType + ")");

        Map<TVar, Type> substitution = unifier.unify(
            funType,
            new FType(argType, resultType)
        );

        AppLogger.info("Unifier environment after unification: " + unifier.getEnv());
        AppLogger.info("Substitution result: " + substitution);

        if (substitution == null) {
            throw new RuntimeException("Type mismatch in application: cannot apply " +
                funType + " to " + argType);
        }

        // Apply the substitution to the types of function and argument immediately
        function.setType(unifier.applySubstitution(function.getType(), substitution)); // Use unifier instance
        argument.setType(unifier.applySubstitution(argument.getType(), substitution)); // Use unifier instance
        AppLogger.info("Type of function Term node (after unify): " + function.type);
        AppLogger.info("Type of argument Term node (after unify): " + argument.type);

        Type finalResultType = unifier.applySubstitution(resultType, substitution); // Use unifier instance
        AppLogger.info("Result type after substitution: " + finalResultType);

        return finalResultType;
    }
}