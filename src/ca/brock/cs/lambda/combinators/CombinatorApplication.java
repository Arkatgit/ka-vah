package ca.brock.cs.lambda.combinators;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CombinatorApplication extends Combinator {
    private Combinator function;
    private Combinator argument;

    private static final int precedence = 20;

    public CombinatorApplication(Combinator function, Combinator argument) {
        this.function = function;
        this.argument = argument;
    }

    public Combinator getFunction() {
        return function;
    }

    public Combinator getArgument() {
        return argument;
    }

    @Override
    public String toStringPrec(int prec) {
        String result = function.toStringPrec(precedence) + " " + argument.toStringPrec(precedence + 1);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }

//    @Override
//    public Combinator eval(Map<String, Combinator> env) {
//        Combinator evaluatedFunction = function.eval(env);
//        Combinator evaluatedArgument = argument.eval(env);
//
//        // Case 1: Direct application of I to its argument (I A)
//        if (evaluatedFunction instanceof ICombinator) {
//            // I A -> A
//            return evaluatedArgument;
//        }
//        // Case 2: The function itself is an application (e.g., (X Y) Z)
//        else if (evaluatedFunction instanceof CombinatorApplication) {
//            Combinator funcFunc = ((CombinatorApplication) evaluatedFunction).getFunction(); // X
//            Combinator funcArg = ((CombinatorApplication) evaluatedFunction).getArgument();   // Y
//
//            // Subcase 2.1: ((K A) B) -> A
//            if (funcFunc instanceof KCombinator) {
//                // Here, evaluatedFunction is (K A), and evaluatedArgument is B.
//                // So, funcFunc is K, and funcArg is A.
//                // The reduction (K A) B -> A applies.
//                return funcArg; // Returns A
//            }
//            // Subcase 2.2: (((S A) B) C) -> (A C) (B C)
//            else if (funcFunc instanceof CombinatorApplication && ((CombinatorApplication) funcFunc).getFunction() instanceof SCombinator) {
//                // Here, evaluatedFunction is ((S A) B), and evaluatedArgument is C.
//                // So, funcFunc is (S A), funcArg is B.
//                // The function of funcFunc is S, and its argument is A.
//                Combinator aCombinator = ((CombinatorApplication) funcFunc).getArgument(); // A
//                Combinator bCombinator = funcArg; // B
//                Combinator cCombinator = evaluatedArgument; // C
//
//                // Result: (A C) (B C)
//                Combinator ac = new CombinatorApplication(aCombinator, cCombinator);
//                Combinator bc = new CombinatorApplication(bCombinator, cCombinator);
//                return new CombinatorApplication(ac, bc).eval(env); // Evaluate the result further
//            }
//            // Subcase 2.3: (((C F) X) Y) -> (F Y) X
//            else if (funcFunc instanceof CombinatorApplication && ((CombinatorApplication) funcFunc).getFunction() instanceof CCombinator) {
//                // Here, evaluatedFunction is ((C F) X), and evaluatedArgument is Y.
//                // So, funcFunc is (C F), funcArg is X.
//                // The function of funcFunc is C, and its argument is F.
//                Combinator fCombinator = ((CombinatorApplication) funcFunc).getArgument(); // F
//                Combinator xCombinator = funcArg; // X
//                Combinator yCombinator = evaluatedArgument; // Y
//
//                // Result: (F Y) X
//                Combinator fy = new CombinatorApplication(fCombinator, yCombinator);
//                return new CombinatorApplication(fy, xCombinator).eval(env); // Evaluate the result further
//            }
//            // Subcase 2.4: (((B F) G) X) -> F (G X)
//            else if (funcFunc instanceof CombinatorApplication && ((CombinatorApplication) funcFunc).getFunction() instanceof BCombinator) {
//                // Here, evaluatedFunction is ((B F) G), and evaluatedArgument is X.
//                // So, funcFunc is (B F), funcArg is G.
//                // The function of funcFunc is B, and its argument is F.
//                Combinator fCombinator = ((CombinatorApplication) funcFunc).getArgument(); // F
//                Combinator gCombinator = funcArg; // G
//                Combinator xCombinator = evaluatedArgument; // X
//
//                // Result: F (G X)
//                Combinator gx = new CombinatorApplication(gCombinator, xCombinator);
//                return new CombinatorApplication(fCombinator, gx).eval(env); // Evaluate the result further
//            }
//            // Subcase 2.5: (Y f) arg -> f (Y f) arg
//            else if (funcFunc instanceof YCombinator) {
//                // Here, evaluatedFunction is (Y f), and evaluatedArgument is arg.
//                // So, funcFunc is Y, and funcArg is f.
//                Combinator yf = new CombinatorApplication(funcFunc, funcArg); // Reconstruct (Y f)
//                Combinator newFunc = new CombinatorApplication(funcArg, yf); // f (Y f)
//                return new CombinatorApplication(newFunc, evaluatedArgument).eval(env); // (f (Y f)) arg, evaluate again
//            }
//
//            // Default for CombinatorApplication function: no specific reduction rule applies yet,
//            // so return the application with evaluated sub-terms.
//            return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
//        }
//        // Case 3: The function is Y (Y f)
//        else if (evaluatedFunction instanceof YCombinator) {
//            // Y f -> f (Y f)
//            // Here, evaluatedFunction is Y, and evaluatedArgument is f.
//            Combinator yAppliedToF = new CombinatorApplication(evaluatedFunction, evaluatedArgument); // Y f
//            Combinator newFunc = new CombinatorApplication(evaluatedArgument, yAppliedToF); // f (Y f)
//            return newFunc.eval(env); // Evaluate the expanded form
//        }
//        // Case 4: The function is a CombinatorConstant (e.g., +, =, not)
//        else if (evaluatedFunction instanceof CombinatorConstant) {
//            CombinatorConstant constFunc = (CombinatorConstant) evaluatedFunction;
//
//            if (constFunc.getValue() instanceof String) {
//                String op = (String) constFunc.getValue();
//
//                // Handle unary operator 'not'
//                if (op.equals("not")) {
//                    if (evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isBoolean()) {
//                        boolean val = (boolean) ((CombinatorConstant) evaluatedArgument).getValue();
//                        return new CombinatorConstant(!val);
//                    }
//                }
//                // Handle binary operators like +, =, and, or.
//                // These require two arguments. The current term is (op A) B.
//                // This means the 'function' *before* evaluation was (op A), and 'argument' is B.
//                // So, 'evaluatedFunction' is (op A) and 'evaluatedArgument' is B.
//                // We need to check if 'evaluatedFunction' is an application of an operator.
//                else if (function instanceof CombinatorApplication) { // Check the original 'function'
//                    Combinator innerOpFuncRaw = ((CombinatorApplication) function).getFunction(); // This is the Combinator, needs cast
//                    Combinator innerOpArg = ((CombinatorApplication) function).getArgument();
//
//                    if (innerOpFuncRaw instanceof CombinatorConstant) { // Ensure it's a CombinatorConstant
//                        CombinatorConstant innerOpFunc = (CombinatorConstant) innerOpFuncRaw; // Cast here
//                        if (innerOpFunc.getValue() instanceof String) {
//                            String innerOp = (String) innerOpFunc.getValue(); // Now getValue() is accessible
//
//                            if (innerOp.equals("+")) {
//                                if (innerOpArg instanceof CombinatorConstant && ((CombinatorConstant) innerOpArg).isInteger() &&
//                                    evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isInteger()) {
//                                    int val1 = (int) ((CombinatorConstant) innerOpArg).getValue();
//                                    int val2 = (int) ((CombinatorConstant) evaluatedArgument).getValue();
//                                    return new CombinatorConstant(val1 + val2);
//                                }
//                            } else if (innerOp.equals("-")) { // Added Subtraction
//                                if (innerOpArg instanceof CombinatorConstant && ((CombinatorConstant) innerOpArg).isInteger() &&
//                                    evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isInteger()) {
//                                    int val1 = (int) ((CombinatorConstant) innerOpArg).getValue();
//                                    int val2 = (int) ((CombinatorConstant) evaluatedArgument).getValue();
//                                    return new CombinatorConstant(val1 - val2);
//                                }
//                            } else if (innerOp.equals("*")) { // Added Multiplication
//                                if (innerOpArg instanceof CombinatorConstant && ((CombinatorConstant) innerOpArg).isInteger() &&
//                                    evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isInteger()) {
//                                    int val1 = (int) ((CombinatorConstant) innerOpArg).getValue();
//                                    int val2 = (int) ((CombinatorConstant) evaluatedArgument).getValue();
//                                    return new CombinatorConstant(val1 * val2);
//                                }
//                            } else if (innerOp.equals("=")) {
//                                if (innerOpArg instanceof CombinatorConstant && evaluatedArgument instanceof CombinatorConstant) {
//                                    Object val1 = ((CombinatorConstant) innerOpArg).getValue();
//                                    Object val2 = ((CombinatorConstant) evaluatedArgument).getValue();
//                                    return new CombinatorConstant(val1.equals(val2));
//                                }
//                            } else if (innerOp.equals("<=")) { // Added LessThanEqual
//                                if (innerOpArg instanceof CombinatorConstant && ((CombinatorConstant) innerOpArg).isInteger() &&
//                                    evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isInteger()) {
//                                    int val1 = (int) ((CombinatorConstant) innerOpArg).getValue();
//                                    int val2 = (int) ((CombinatorConstant) evaluatedArgument).getValue();
//                                    return new CombinatorConstant(val1 <= val2);
//                                }
//                            } else if (innerOp.equals("and")) {
//                                if (innerOpArg instanceof CombinatorConstant && ((CombinatorConstant) innerOpArg).isBoolean() &&
//                                    evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isBoolean()) {
//                                    boolean val1 = (boolean) ((CombinatorConstant) innerOpArg).getValue();
//                                    boolean val2 = (boolean) ((CombinatorConstant) evaluatedArgument).getValue();
//                                    return new CombinatorConstant(val1 && val2);
//                                }
//                            } else if (innerOp.equals("or")) {
//                                if (innerOpArg instanceof CombinatorConstant && ((CombinatorConstant) innerOpArg).isBoolean() &&
//                                    evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isBoolean()) {
//                                    boolean val1 = (boolean) ((CombinatorConstant) innerOpArg).getValue();
//                                    boolean val2 = (boolean) ((CombinatorConstant) evaluatedArgument).getValue();
//                                    return new CombinatorConstant(val1 || val2);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            // If it's a constant that's not an operator, or a partial application of an operator,
//            // or an operator applied to non-constant arguments, return the application itself.
//            return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
//        }
//
//        // Default: no reduction happened, return the application with evaluated sub-terms.
//        // This covers cases like (K A), (S A), (S A B), (Variable X) Y, etc. that are not fully reducible yet.
//        return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
//    }

    @Override
    public Combinator eval(Map<String, Combinator> env) {
        Combinator evaluatedFunction = function.eval(env);
        Combinator evaluatedArgument = argument.eval(env);

        // Case 1: Direct application of I to its argument (I A)
        if (evaluatedFunction instanceof ICombinator) {
            return evaluatedArgument;
        }

        // Case: Match combinator evaluation
        if (evaluatedFunction instanceof MatchCombinator) {
            MatchCombinator match = (MatchCombinator) evaluatedFunction;
            // The argument becomes the new input for the match
            return new MatchCombinator(evaluatedArgument, match.getCases()).eval(env);
        }

        // Case 2: The function itself is an application (e.g., (X Y) Z)
        if (evaluatedFunction instanceof CombinatorApplication) {
            CombinatorApplication innerApp = (CombinatorApplication) evaluatedFunction;
            Combinator funcFunc = innerApp.getFunction();
            Combinator funcArg = innerApp.getArgument();

            // Subcase 2.1: ((K A) B) -> A
            if (funcFunc instanceof KCombinator) {
                return funcArg;
            }

            // Subcase 2.2: (((S A) B) C) -> (A C) (B C)
            if (funcFunc instanceof CombinatorApplication && ((CombinatorApplication) funcFunc).getFunction() instanceof SCombinator) {
                Combinator aCombinator = ((CombinatorApplication) funcFunc).getArgument();
                Combinator bCombinator = funcArg;
                Combinator cCombinator = evaluatedArgument;

                Combinator ac = new CombinatorApplication(aCombinator, cCombinator);
                Combinator bc = new CombinatorApplication(bCombinator, cCombinator);
                return new CombinatorApplication(ac, bc).eval(env);
            }

            // Subcase 2.3: (((C F) X) Y) -> (F Y) X
            if (funcFunc instanceof CombinatorApplication && ((CombinatorApplication) funcFunc).getFunction() instanceof CCombinator) {
                Combinator fCombinator = ((CombinatorApplication) funcFunc).getArgument();
                Combinator xCombinator = funcArg;
                Combinator yCombinator = evaluatedArgument;

                Combinator fy = new CombinatorApplication(fCombinator, yCombinator);
                return new CombinatorApplication(fy, xCombinator).eval(env);
            }

            // Subcase 2.4: (((B F) G) X) -> F (G X)
            if (funcFunc instanceof CombinatorApplication && ((CombinatorApplication) funcFunc).getFunction() instanceof BCombinator) {
                Combinator fCombinator = ((CombinatorApplication) funcFunc).getArgument();
                Combinator gCombinator = funcArg;
                Combinator xCombinator = evaluatedArgument;

                Combinator gx = new CombinatorApplication(gCombinator, xCombinator);
                return new CombinatorApplication(fCombinator, gx).eval(env);
            }

            // Subcase 2.5: (Y f) arg -> f (Y f) arg
            if (funcFunc instanceof YCombinator) {
                Combinator yf = new CombinatorApplication(funcFunc, funcArg);
                Combinator newFunc = new CombinatorApplication(funcArg, yf);
                return new CombinatorApplication(newFunc, evaluatedArgument).eval(env);
            }

            // Handle arithmetic operations: ((op arg1) arg2)
            if (funcFunc instanceof CombinatorConstant) {
                CombinatorConstant opConst = (CombinatorConstant) funcFunc;

                if (opConst.getValue() instanceof String) {
                    String op = (String) opConst.getValue();

                    // Check if we have a binary operator applied to two constants
                    if (isBinaryOperator(op) &&
                        funcArg instanceof CombinatorConstant &&
                        evaluatedArgument instanceof CombinatorConstant) {

                        CombinatorConstant leftConst = (CombinatorConstant) funcArg;
                        CombinatorConstant rightConst = (CombinatorConstant) evaluatedArgument;

                        // Handle arithmetic operations
                        if (isArithmeticOperator(op) && leftConst.isInteger() && rightConst.isInteger()) {
                            int leftVal = (Integer) leftConst.getValue();
                            int rightVal = (Integer) rightConst.getValue();

                            switch (op) {
                                case "+":
                                    return new CombinatorConstant(leftVal + rightVal);
                                case "-":
                                    return new CombinatorConstant(leftVal - rightVal);
                                case "*":
                                    return new CombinatorConstant(leftVal * rightVal);
                                case "/":
                                    if (rightVal == 0) throw new RuntimeException("Division by zero");
                                    return new CombinatorConstant(leftVal / rightVal);
                            }
                        }

                        // Handle comparison operations
                        if (isComparisonOperator(op) && leftConst.isInteger() && rightConst.isInteger()) {
                            int leftVal = (Integer) leftConst.getValue();
                            int rightVal = (Integer) rightConst.getValue();

                            switch (op) {
                                case "=":
                                    return new CombinatorConstant(leftVal == rightVal);
                                case "<=":
                                    return new CombinatorConstant(leftVal <= rightVal);
                            }
                        }

                        // Handle logical operations
                        if (isLogicalOperator(op) && leftConst.isBoolean() && rightConst.isBoolean()) {
                            boolean leftVal = (Boolean) leftConst.getValue();
                            boolean rightVal = (Boolean) rightConst.getValue();

                            switch (op) {
                                case "and":
                                    return new CombinatorConstant(leftVal && rightVal);
                                case "or":
                                    return new CombinatorConstant(leftVal || rightVal);
                            }
                        }
                    }
                }
            }

            // Handle conditionals: (((IF condition) thenBranch) elseBranch)
            if (funcFunc instanceof CombinatorApplication) {
                CombinatorApplication middleApp = (CombinatorApplication) funcFunc;
                Combinator middleFunc = middleApp.getFunction();
                Combinator condition = middleApp.getArgument();

                if (middleFunc instanceof CombinatorConstant) {
                    CombinatorConstant ifConst = (CombinatorConstant) middleFunc;

                    if (ifConst.getValue() instanceof String && ifConst.getValue().equals("IF")) {
                        // We have (((IF condition) thenBranch) elseBranch)
                        // Now we can evaluate the conditional
                        Combinator evaluatedCondition = condition.eval(env);

                        if (evaluatedCondition instanceof CombinatorConstant) {
                            CombinatorConstant conditionConst = (CombinatorConstant) evaluatedCondition;

                            if (conditionConst.isBoolean()) {
                                boolean condValue = (Boolean) conditionConst.getValue();

                                if (condValue) {
                                    // True branch: return thenBranch
                                    return funcArg.eval(env);
                                } else {
                                    // False branch: return elseBranch (evaluatedArgument)
                                    return evaluatedArgument.eval(env);
                                }
                            }
                        }

                        // If condition is not reducible to a boolean yet, return as-is
                        return new CombinatorApplication(
                            new CombinatorApplication(
                                new CombinatorApplication(new CombinatorConstant("IF"), condition),
                                funcArg
                            ),
                            evaluatedArgument
                        );
                    }
                }
            }
            // NEW: Handle W combinator: ((W x) y) -> x y y
            if (funcFunc instanceof WCombinator) {
                // We have (W x) y -> x y y
                Combinator x = funcArg;
                Combinator y = evaluatedArgument;

                Combinator xy = new CombinatorApplication(x, y);
                return new CombinatorApplication(xy, y).eval(env);
            }
        }

        // Case 3: The function is Y (Y f)
        if (evaluatedFunction instanceof YCombinator) {
            Combinator yAppliedToF = new CombinatorApplication(evaluatedFunction, evaluatedArgument);
            Combinator newFunc = new CombinatorApplication(evaluatedArgument, yAppliedToF);
            return newFunc.eval(env);
        }

        // Case 4: Handle partial IF applications
        if (evaluatedFunction instanceof CombinatorConstant) {
            CombinatorConstant constFunc = (CombinatorConstant) evaluatedFunction;

            if (constFunc.getValue() instanceof String && constFunc.getValue().equals("IF")) {
                // We have (IF condition), need to wait for thenBranch
                return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
            }
        }

        // Case 5: Handle ((IF condition) thenBranch) - wait for elseBranch
        if (evaluatedFunction instanceof CombinatorApplication) {
            CombinatorApplication innerApp = (CombinatorApplication) evaluatedFunction;
            Combinator innerFunc = innerApp.getFunction();
            Combinator condition = innerApp.getArgument();

            if (innerFunc instanceof CombinatorConstant) {
                CombinatorConstant ifConst = (CombinatorConstant) innerFunc;

                if (ifConst.getValue() instanceof String && ifConst.getValue().equals("IF")) {
                    // We have ((IF condition) thenBranch), need to wait for elseBranch
                    return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
                }
            }
        }

        // Case 6: Handle unary operations like "not"
        if (evaluatedFunction instanceof CombinatorConstant) {
            CombinatorConstant constFunc = (CombinatorConstant) evaluatedFunction;

            if (constFunc.getValue() instanceof String) {
                String op = (String) constFunc.getValue();

                if (op.equals("not")) {
                    if (evaluatedArgument instanceof CombinatorConstant && ((CombinatorConstant) evaluatedArgument).isBoolean()) {
                        boolean val = (boolean) ((CombinatorConstant) evaluatedArgument).getValue();
                        return new CombinatorConstant(!val);
                    }
                }

                // Handle partial application of binary operators
                if (isBinaryOperator(op) && !(evaluatedArgument instanceof CombinatorApplication)) {
                    // This is a partial application like (+ 3), wait for second argument
                    return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
                }
            }
        }

        // NEW: Handle standalone W combinator: W x -> wait for second argument
        if (evaluatedFunction instanceof WCombinator) {
            // W x -> wait for second argument y to become (W x) y
            return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
        }


        // Default: no reduction happened, return the application with evaluated sub-terms
        return new CombinatorApplication(evaluatedFunction, evaluatedArgument);
    }

    // Helper methods to classify operators
    private boolean isBinaryOperator(String op) {
        return isArithmeticOperator(op) || isComparisonOperator(op) || isLogicalOperator(op);
    }

    private boolean isArithmeticOperator(String op) {
        return op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
    }

    private boolean isComparisonOperator(String op) {
        return op.equals("=") || op.equals("<=");
    }

    private boolean isLogicalOperator(String op) {
        return op.equals("and") || op.equals("or");
    }

    @Override
    public Set<String> getFreeVariables() {
        Set<String> freeVars = new HashSet<>();
        freeVars.addAll(function.getFreeVariables());
        freeVars.addAll(argument.getFreeVariables());
        return freeVars;
    }

    @Override
    public Combinator substitute(String varName, Combinator value) {
        return new CombinatorApplication(function.substitute(varName, value), argument.substitute(varName, value));
    }

    @Override
    public Combinator optimize() {
        // First, optimize the subterms recursively
        Combinator optimizedFunction = function.optimize();
        Combinator optimizedArgument = argument.optimize();

        // Try to apply optimization rules to the optimized subterms
        Combinator result = applyOptimizationRules(optimizedFunction, optimizedArgument);

        // If a rule applied, recursively optimize the result
        if (result != null) {
            return result.optimize(); // Recursively optimize the transformed result
        }

        // If no rule applied, return the application of the optimized subterms
        return new CombinatorApplication(optimizedFunction, optimizedArgument);
    }

    private Combinator applyOptimizationRules(Combinator func, Combinator arg) {
        // Rule 1: B I = I
        if (func instanceof BCombinator && arg instanceof ICombinator) {
            return new ICombinator();
        }

        // Rule 2: C I = C (or C*)
        if (func instanceof CCombinator && arg instanceof ICombinator) {
            return func; // C I = C
        }

        // Rule 3: S x I = W x
        if (func instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) func;
            if (app.getFunction() instanceof SCombinator && arg instanceof ICombinator) {
                return new CombinatorApplication(new WCombinator(), app.getArgument());
            }
        }

        // Rule 4: S K x = I
        if (func instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) func;
            if (app.getFunction() instanceof SCombinator && app.getArgument() instanceof KCombinator) {
                return new ICombinator();
            }
        }

        // Rule 5: B x I = x
        if (func instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) func;
            if (app.getFunction() instanceof BCombinator && arg instanceof ICombinator) {
                return app.getArgument();
            }
        }

        // Rule 6: C K x = I
        if (func instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) func;
            if (app.getFunction() instanceof CCombinator && app.getArgument() instanceof KCombinator) {
                return new ICombinator();
            }
        }

        // Rule 7: C B I = I
        if (func instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) func;
            if (app.getFunction() instanceof CCombinator) {
                if (app.getArgument() instanceof CombinatorApplication) {
                    CombinatorApplication innerApp = (CombinatorApplication) app.getArgument();
                    if (innerApp.getFunction() instanceof BCombinator &&
                        innerApp.getArgument() instanceof ICombinator) {
                        return new ICombinator();
                    }
                }
            }
        }

        return null; // No rule applied
    }
}
