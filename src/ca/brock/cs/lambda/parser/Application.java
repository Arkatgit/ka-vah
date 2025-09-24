package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorApplication;
import ca.brock.cs.lambda.intermediate.IntermediateApplication;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.logging.AppLogger;
import ca.brock.cs.lambda.types.AlgebraicDataType;
import ca.brock.cs.lambda.types.FType;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Type functionType = function.computeType(env, unifier);
        Type argumentType = argument.computeType(env, unifier);

        // Apply current substitutions
        functionType = unifier.applySubstitution(functionType, unifier.getEnv());
        argumentType = unifier.applySubstitution(argumentType, unifier.getEnv());

        TVar resultType = TVar.fresh(); //unifier.fresh();

        // The function should have type (argumentType -> resultType)
        FType expectedFunctionType = new FType(argumentType, resultType);

        AppLogger.info("Application: ArgumentType " + argumentType + " result type " + resultType);
        Map<TVar, Type> sub = unifier.unify(functionType, expectedFunctionType);
        if (sub == null) {
            throw new TypeError("Type mismatch in application: cannot apply " +
                functionType + " to " + argumentType);
        }

        return unifier.applySubstitution(resultType, unifier.getEnv());
    }


    @Override
    public Term eval(Map<String, Term> env) {
        Term evaluatedFunction = function.eval(env);
        Term evaluatedArgument = argument.eval(env);

        if (evaluatedFunction instanceof Abstraction) {
            Abstraction abstraction = (Abstraction) evaluatedFunction;
            // Beta reduction
            return abstraction.getBody().substitute(abstraction.getParameter(), evaluatedArgument).eval(env);
        }

        // Return a partially applied constructor
        if (evaluatedFunction instanceof Constructor) {
            return new Application(evaluatedFunction, evaluatedArgument);
        }

        // Default: no reduction happened, return the application with evaluated sub-terms.
        // This covers cases where function is a variable, or it's not a reducible application.
        // Or if the application is not fully evaluated (e.g., function is a variable, or argument is not yet a literal).
        return new Application(evaluatedFunction, evaluatedArgument);
    }



    @Override
    public Term substitute(String varName, Term value) {
        return new Application(function.substitute(varName, value), argument.substitute(varName, value));
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.addAll(function.getFreeVariables());
        freeVars.addAll(argument.getFreeVariables());
        return freeVars;
    }

    @Override
    public Combinator translate() {
        // T[M N] = T[M] T[N]
        return new CombinatorApplication(function.translate(), argument.translate());
    }

    /**
     * Converts this Application to an IntermediateApplication.
     * This is the first step in the two-phase translation to combinators.
     * @return The equivalent IntermediateApplication.
     */
    @Override
    public IntermediateTerm toIntermediateTerm() {
        // Recursively convert the function and argument to IntermediateTerms
        return new IntermediateApplication(function.toIntermediateTerm(), argument.toIntermediateTerm());
    }

}