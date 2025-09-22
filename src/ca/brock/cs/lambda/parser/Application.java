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
//    protected Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type functionType = function.computeType(env, unifier);
//        Type argumentType = argument.computeType(env, unifier);
//
//        AppLogger.info("Application: " + function + " : " + functionType);
//        AppLogger.info("Argument: " + argument + " : " + argumentType);
//
//        TVar resultType = unifier.fresh();
//
//        // The function should have type (argumentType -> resultType)
//        FType expectedFunctionType = new FType(argumentType, resultType);
//
//        AppLogger.info("Expected function type: " + expectedFunctionType);
//        AppLogger.info("Actual function type: " + functionType);
//
//        // CRITICAL: Before unification, we need to apply any existing substitutions
//        Map<TVar, Type> currentEnv = unifier.getEnv();
//        functionType = unifier.applySubstitution(functionType, currentEnv);
//        argumentType = unifier.applySubstitution(argumentType, currentEnv);
//        expectedFunctionType = (FType) unifier.applySubstitution(expectedFunctionType, currentEnv);
//
//        AppLogger.info("After substitution - Function: " + functionType);
//        AppLogger.info("After substitution - Expected: " + expectedFunctionType);
//
//        Map<TVar, Type> sub = unifier.unify(functionType, expectedFunctionType);
//        if (sub == null) {
//            String errorMsg = String.format(
//                "Type mismatch in application: cannot apply %s to %s\n" +
//                    "Function: %s\nArgument: %s",
//                functionType, argumentType,
//                function.toString(), argument.toString()
//            );
//            throw new TypeError(errorMsg);
//        }
//
//        // Apply the unification result to the environment
//        unifier.setEnv(sub);
//
//        Type finalResult = unifier.applySubstitution(resultType, sub);
//        AppLogger.info("Application result type: " + finalResult);
//        return finalResult;
//    }
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        Type functionType = function.computeType(env, unifier);
        Type argumentType = argument.computeType(env, unifier);

        // Apply current substitutions
        functionType = unifier.applySubstitution(functionType, unifier.getEnv());
        argumentType = unifier.applySubstitution(argumentType, unifier.getEnv());

        TVar resultType = TVar.fresh(); //unifier.fresh();

        // The function should have type (argumentType -> resultType)
        FType expectedFunctionType = new FType(argumentType, resultType);

        System.out.println("Application: ArgumentType " + argumentType + " result type " + resultType);
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



//package ca.brock.cs.lambda.parser;
//
//import ca.brock.cs.lambda.combinators.Combinator;
//import ca.brock.cs.lambda.combinators.CombinatorApplication;
//import ca.brock.cs.lambda.intermediate.IntermediateApplication;
//import ca.brock.cs.lambda.intermediate.IntermediateTerm;
//import ca.brock.cs.lambda.logging.AppLogger;
//import ca.brock.cs.lambda.types.FType;
//import ca.brock.cs.lambda.types.TVar;
//import ca.brock.cs.lambda.types.Type;
//import ca.brock.cs.lambda.types.Unifier;
//
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//public class Application extends Term {
//    private Term function;
//    private Term argument;
//
//    private static final int precedence = 20;
//
//    public Application(Term function, Term argument) {
//        this.function = function;
//        this.argument = argument;
//    }
//
//    public Term getFunction() {
//        return function;
//    }
//
//    public Term getArgument(){
//        return argument;
//    }
//    @Override
//    public String toStringPrec(int prec) {
//        // Case 1: Handle (* t) -> Application(Application(Constant("flip"), Constant("*")), r)
//        if (function instanceof Application) {
//            Application innerApp = (Application) function;
//
//            if (innerApp.function instanceof Constant) {
//                Constant firstConst = (Constant) innerApp.function;
//
//                // Check if it's "flip op" where op is an infix operator
//                if ("flip".equals(firstConst.toStringPrec(0))) {
//                    if (innerApp.argument instanceof Constant) {
//                        Constant opConst = (Constant) innerApp.argument;
//
//                        if (opConst.canBeUsedAsSection()) {
//                            // Format as (* t)
//                            return "(" + opConst.toStringPrec(0) + " " + argument.toStringPrec(precedence + 1) + ")";
//                        }
//                    }
//                }
//            }
//        }
//
//        // Case 2: Handle (t *) -> Application(Constant("*"), l)
//        if (function instanceof Constant) {
//            Constant opConst = (Constant) function;
//
//            if (opConst.isInfixOperator()) {
//                // Format as (t *)
//                return "(" + argument.toStringPrec(precedence) + " " + opConst.toStringPrec(0) + ")";
//            }
//        }
//
//        // Case 3: Handle (*) t1 t2 -> Application(Application(Constant("*"), l), r)
//        if (function instanceof Application) {
//            Application innerApp = (Application) function;
//
//            if (innerApp.function instanceof Constant) {
//                Constant opConst = (Constant) innerApp.function;
//
//                if (opConst.isInfixOperator()) {
//                    // Format as t1 * t2 (infix notation)
//                    return innerApp.argument.toStringPrec(precedence) + " " + opConst.toStringPrec(0) + " " + argument.toStringPrec(precedence + 1);
//                }
//            }
//        }
//
//        // Case 4: Handle standalone (*) -> Constant("*")
//        if (function instanceof Constant) {
//            Constant opConst = (Constant) function;
//
//            if (opConst.isInfixOperator()) {
//                // Format as (*)
//                return "(" + opConst.toStringPrec(0) + ")";
//            }
//        }
//
//        // Default function application behavior
//        String result = function.toStringPrec(precedence) + " " + argument.toStringPrec(precedence + 1);
//
//        // Ensure parentheses if precedence requires it
//        if (prec > precedence) {
//            result = "(" + result + ")";
//        }
//
//        return result;
//    }
//
//    @Override
//    protected Type computeType(Map<String, Type> env, Unifier unifier) {
//        AppLogger.info("--- Application: " + function + " " + argument);
//        AppLogger.info("Environment: " + env);
//
//        AppLogger.info("Typing function: " + function + " with environment: " + env);
//        function.type(env, unifier);
//        Type funType = function.getType();
//        AppLogger.info("Type of function Term node: " + function.getType()); // Check the Term node's type
//
//        AppLogger.info("Typing argument: " + argument + " with environment: " + env);
//        argument.type(env, unifier);
//        Type argType = argument.getType();
//        AppLogger.info("Type of argument Term node: " + argument.getType()); // Check the Term node's type
//
//        TVar resultType = TVar.fresh();
//        AppLogger.info("Fresh result type for application: " + resultType);
//        AppLogger.info("Type of function (before unify): " + funType);
//        AppLogger.info("Type of argument (before unify): " + argType);
//        AppLogger.info("Expected function type: (" + argType + " → " + resultType + ")");
//
//        Map<TVar, Type> substitution = unifier.unify(
//            funType,
//            new FType(argType, resultType)
//        );
//
//        AppLogger.info("Unifier environment after unification: " + unifier.getEnv());
//        AppLogger.info("Substitution result: " + substitution);
//
//        if (substitution == null) {
//            throw new RuntimeException("Type mismatch in application: cannot apply " +
//                funType + " to " + argType);
//        }
//
//        // Apply the substitution to the types of function and argument immediately
//        function.setType(unifier.applySubstitution(function.getType(), substitution)); // Use unifier instance
//        argument.setType(unifier.applySubstitution(argument.getType(), substitution)); // Use unifier instance
//        AppLogger.info("Type of function Term node (after unify): " + function.getType());
//        AppLogger.info("Type of argument Term node (after unify): " + argument.getType());
//
//        Type finalResultType = unifier.applySubstitution(resultType, substitution); // Use unifier instance
//        AppLogger.info("Result type after substitution: " + finalResultType);
//
//        return finalResultType;
//    }
//    @Override
//    public Term eval(Map<String, Term> env) {
//        Term evaluatedFunction = function.eval(env);
//        Term evaluatedArgument = argument.eval(env);
//
//        // Case 1: Function is an abstraction (λx. M) N -> M[x:=N]
//        if (evaluatedFunction instanceof Abstraction) {
//            Abstraction abs = (Abstraction) evaluatedFunction;
//            return abs.getBody().substitute(abs.getParameter(), evaluatedArgument).eval(env);
//        }
//        // Case 2: Function is a partial application of a binary operator (e.g., ((+) 5) 10)
//        else if (evaluatedFunction instanceof Application) {
//            Application innerApp = (Application) evaluatedFunction;
//            Term innerFunc = innerApp.getFunction();
//            Term innerArg = innerApp.getArgument();
//
//            if (innerFunc instanceof Constant) {
//                Constant opConst = (Constant) innerFunc;
//                // Now, opConst is the operator (+, -, *, =, <=, and, or)
//                // innerArg is the first operand
//                // evaluatedArgument is the second operand
//
//                switch (opConst.getValue()) {
//                    case "+":
//                        if (innerArg instanceof IntegerLiteral && evaluatedArgument instanceof IntegerLiteral) {
//                            return new IntegerLiteral(((IntegerLiteral) innerArg).getValue() + ((IntegerLiteral) evaluatedArgument).getValue());
//                        }
//                        break;
//                    case "-":
//                        if (innerArg instanceof IntegerLiteral && evaluatedArgument instanceof IntegerLiteral) {
//                            return new IntegerLiteral(((IntegerLiteral) innerArg).getValue() - ((IntegerLiteral) evaluatedArgument).getValue());
//                        }
//                        break;
//                    case "*":
//                        if (innerArg instanceof IntegerLiteral && evaluatedArgument instanceof IntegerLiteral) {
//                            return new IntegerLiteral(((IntegerLiteral) innerArg).getValue() * ((IntegerLiteral) evaluatedArgument).getValue());
//                        }
//                        break;
//                    case "=":
//                        // Handle equality for both integers and booleans
//                        if (innerArg instanceof IntegerLiteral && evaluatedArgument instanceof IntegerLiteral) {
//                            return new BooleanLiteral(((IntegerLiteral) innerArg).getValue() == ((IntegerLiteral) evaluatedArgument).getValue());
//                        } else if (innerArg instanceof BooleanLiteral && evaluatedArgument instanceof BooleanLiteral) {
//                            return new BooleanLiteral(((BooleanLiteral) innerArg).getValue() == ((BooleanLiteral) evaluatedArgument).getValue());
//                        }
//                        break;
//                    case "<=":
//                        if (innerArg instanceof IntegerLiteral && evaluatedArgument instanceof IntegerLiteral) {
//                            return new BooleanLiteral(((IntegerLiteral) innerArg).getValue() <= ((IntegerLiteral) evaluatedArgument).getValue());
//                        }
//                        break;
//                    case "and":
//                        if (innerArg instanceof BooleanLiteral && evaluatedArgument instanceof BooleanLiteral) {
//                            return new BooleanLiteral(((BooleanLiteral) innerArg).getValue() && ((BooleanLiteral) evaluatedArgument).getValue());
//                        }
//                        break;
//                    case "or":
//                        if (innerArg instanceof BooleanLiteral && evaluatedArgument instanceof BooleanLiteral) {
//                            return new BooleanLiteral(((BooleanLiteral) innerArg).getValue() || ((BooleanLiteral) evaluatedArgument).getValue());
//                        }
//                        break;
//                    // 'not' is unary, so it shouldn't be here in the binary operator section
//                }
//            }
//        }
//        // Case 3: Function is a constant operator (like "+", "not", "flip")
//        else if (evaluatedFunction instanceof Constant) {
//            Constant constFunc = (Constant) evaluatedFunction;
//            switch (constFunc.getValue()) {
//                case "+":
//                case "-":
//                case "*":
//                case "=":
//                case "<=":
//                case "and":
//                case "or":
//                    // These are binary operators, they need a second argument.
//                    // So, return a new Application representing the partial application.
//                    return new Application(evaluatedFunction, evaluatedArgument);
//                case "not":
//                    if (evaluatedArgument instanceof BooleanLiteral) {
//                        return new BooleanLiteral(!((BooleanLiteral) evaluatedArgument).getValue());
//                    }
//                    break;
//                case "flip":
//                    // (flip f) x y = f y x
//                    if (evaluatedArgument instanceof Constant) {
//                        // Creates (λx. λy. f y x)
//                        return new Abstraction("x", new Abstraction("y", new Application(new Application(evaluatedArgument, new Variable("y")), new Variable("x"))));
//                    }
//                    break;
//                default:
//                    // For other constants (like "True", "False", "123", or variables that might be in env),
//                    // or if the argument type doesn't match the operator's expectation,
//                    // just return the application, indicating it's not reducible further at this point.
//                    break;
//            }
//        }
//
//        // Default: no reduction happened, return the application with evaluated sub-terms.
//        // This covers cases where function is a variable, or it's not a reducible application.
//        // Or if the application is not fully evaluated (e.g., function is a variable, or argument is not yet a literal).
//        return new Application(evaluatedFunction, evaluatedArgument);
//    }
//
//
//
//    @Override
//    public Term substitute(String varName, Term value) {
//        return new Application(function.substitute(varName, value), argument.substitute(varName, value));
//    }
//
//    @Override
//    public Set<String> getFreeVariables() {
//        Set<String> freeVars = new HashSet<>();
//        freeVars.addAll(function.getFreeVariables());
//        freeVars.addAll(argument.getFreeVariables());
//        return freeVars;
//    }
//
//    @Override
//    public Combinator translate() {
//        // T[M N] = T[M] T[N]
//        return new CombinatorApplication(function.translate(), argument.translate());
//    }
//
//    /**
//     * Converts this Application to an IntermediateApplication.
//     * This is the first step in the two-phase translation to combinators.
//     * @return The equivalent IntermediateApplication.
//     */
//    @Override
//    public IntermediateTerm toIntermediateTerm() {
//        // Recursively convert the function and argument to IntermediateTerms
//        return new IntermediateApplication(function.toIntermediateTerm(), argument.toIntermediateTerm());
//    }
//
//}