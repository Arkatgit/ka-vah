//package ca.brock.ca.interpreter;
//
//import ca.brock.cs.lambda.*;
//import java.util.HashMap;
//import java.util.Map;
//
//public class TypeInferencer {
//    private int varCounter = 0;
//    private final Unifier unifier = new Unifier();
//    private final Map<String, Type> typeContext = new HashMap<>();
//
//    // Generate fresh type variables
//    private TVar freshTypeVar() {
//        return new TVar("α" + varCounter++);
//    }
//
//    // Main type inference method
//    public Map<String, Type> infer(Term term) {
//        Type inferredType = inferTerm(term, new HashMap<>());
//        return typeContext;
//    }
//
//    private Type inferTerm(Term term, Map<String, Type> context) {
//        if (term instanceof Variable) {
//            return handleVariable((Variable) term, context);
//        }
//        else if (term instanceof Abstraction) {
//            return handleAbstraction((Abstraction) term, context);
//        }
//        else if (term instanceof Application) {
//            return handleApplication((Application) term, context);
//        }
//        else if (term instanceof ca.brock.cs.lambda.Constant) {
//            return handleConstant((ca.brock.cs.lambda.Constant) term);
//        }
//        else if (term instanceof IntegerLiteral) {
//            return new Constant("Int");
//        }
//        else if (term instanceof BooleanLiteral) {
//            return new Constant("Bool");
//        }
//        else if (term instanceof Addition) {
//            return handleBinaryOp((Addition) term, context, "Int");
//        }
//        else if (term instanceof Subtraction) {
//            return handleBinaryOp((Subtraction) term, context, "Int");
//        }
//        else if (term instanceof Multiplication) {
//            return handleBinaryOp((Multiplication) term, context, "Int");
//        }
//        else if (term instanceof And) {
//            return handleBinaryOp((And) term, context, "Bool");
//        }
//        else if (term instanceof Or) {
//            return handleBinaryOp((Or) term, context, "Bool");
//        }
//        else if (term instanceof Equal) {
//            return handleComparison((Equal) term, context);
//        }
//        else if (term instanceof LEqual) {
//            return handleComparison((LEqual) term, context);
//        }
//        else if (term instanceof Conditional) {
//            return handleConditional((Conditional) term, context);
//        }
//        else if (term instanceof Not) {
//            return handleNot((Not) term, context);
//        }
//        else {
//            throw new RuntimeException("Unsupported term type: " + term.getClass().getSimpleName());
//        }
//    }
//
//
//    private Type handleVariable(Variable var, Map<String, Type> context) {
//        // If x : τ is in the context Γ, then Γ ⊢ x : τ.
//        if (!context.containsKey(var.getName())) {
//            throw new RuntimeException("Unbound variable: " + var.getName());
//        }
//        return context.get(var.getName());
//    }
//
//    private Type handleAbstraction(Abstraction abs, Map<String, Type> context) {
//        //If Γ, x:τ₁ ⊢ e : τ₂, then Γ ⊢ (λx.e) : τ₁ → τ₂
//
//        TVar paramType = freshTypeVar();
//        Map<String, Type> newContext = new HashMap<>(context);
//        newContext.put(abs.getParameter(), paramType);
//        Type bodyType = inferTerm(abs.getBody(), newContext);
//        return new FType(paramType, bodyType);
//    }
//
//    private Type handleApplication(Application app, Map<String, Type> context) {
//       // If Γ ⊢ f : τ₁ → τ₂ and Γ ⊢ x : τ₁, then Γ ⊢ (f x) : τ₂.
//        Type funType = inferTerm(app.getFunction(), context);
//        Type argType = inferTerm(app.getArgument(), context);
//        TVar resultType = freshTypeVar();
//
//        Map<String, Type> substitution = unifier.unify(
//            funType,
//            new FType(argType, resultType)
//        );
//
//        if (substitution == null) {
//            throw new RuntimeException("Type error in application: cannot apply " + funType + " to " + argType);
//        }
//
//        typeContext.putAll(substitution);
//        return Unifier.applySubstitution(resultType, substitution);
//    }
//
//    private Type handleConstant(ca.brock.cs.lambda.Constant constant) {
//        if (constant.getValue().equals("True") || constant.getValue().equals("False")) {
//            return new Constant("Bool");
//        }
//
//        switch (constant.getValue()) {
//            case "+":
//            case "-":
//            case "*":
//                return new FType(new Constant("Int"), new FType(new Constant("Int"), new Constant("Int")));
//            case "and":
//            case "or":
//                return new FType(new Constant("Bool"), new FType(new Constant("Bool"), new Constant("Bool")));
//            case "=":
//            case "<=":
//                TVar a = freshTypeVar();
//                return new FType(a, new FType(a, new Constant("Bool")));
//            case "not":
//                return new FType(new Constant("Bool"), new Constant("Bool"));
//            default:
//                return freshTypeVar(); // Unknown constant
//        }
//    }
//
//    private Type handleBinaryOp(Term term, Map<String, Type> context, String expectedType) {
//        Term left, right;
//
//        if (term instanceof Addition) {
//            Addition add = (Addition) term;
//            left = add.getLeft();
//            right = add.getRight();
//        }
//        else if (term instanceof Subtraction) {
//            Subtraction sub = (Subtraction) term;
//            left = sub.getLeft();
//            right = sub.getRight();
//        }
//        else if (term instanceof Multiplication) {
//            Multiplication mul = (Multiplication) term;
//            left = mul.getLeft();
//            right = mul.getRight();
//        }
//        else if (term instanceof And) {
//            And and = (And) term;
//            left = and.getLeft();
//            right = and.getRight();
//        }
//        else if (term instanceof Or) {
//            Or or = (Or) term;
//            left = or.getLeft();
//            right = or.getRight();
//        }
//        else {
//            throw new IllegalArgumentException("Unsupported binary operation");
//        }
//
//        Type leftType = inferTerm(left, context);
//        Type rightType = inferTerm(right, context);
//        Type expected = new Constant(expectedType);
//
//        Map<String, Type> sub1 = unifier.unify(leftType, expected);
//        Map<String, Type> sub2 = unifier.unify(rightType, expected);
//
//        if (sub1 == null || sub2 == null) {
//            throw new RuntimeException("Operands must be " + expectedType);
//        }
//
//        typeContext.putAll(sub1);
//        typeContext.putAll(sub2);
//        return expected;
//    }
//
//    private Type handleComparison(Term term, Map<String, Type> context) {
//        Term left, right;
//
//        if (term instanceof Equal) {
//            Equal eq = (Equal) term;
//            left = eq.getLeft();
//            right = eq.getRight();
//        }
//        else if (term instanceof LEqual) {
//            LEqual le = (LEqual) term;
//            left = le.getLeft();
//            right = le.getRight();
//        }
//        else {
//            throw new IllegalArgumentException("Unsupported comparison operation");
//        }
//
//        Type leftType = inferTerm(left, context);
//        Type rightType = inferTerm(right, context);
//
//        // Unify both operands with same type
//        Map<String, Type> sub = unifier.unify(leftType, rightType);
//        if (sub == null) {
//            throw new RuntimeException("Comparison operands must have same type");
//        }
//
//        typeContext.putAll(sub);
//        return new Constant("Bool");
//    }
//
//    private Type handleConditional(Conditional cond, Map<String, Type> context) {
//        Type condType = inferTerm(cond.getCondition(), context);
//        Type thenType = inferTerm(cond.getTrueBranch(), context);
//        Type elseType = inferTerm(cond.getFalseBranch(), context);
//
//        // Unify condition with Bool
//        Map<String, Type> sub1 = unifier.unify(condType, new Constant("Bool"));
//        if (sub1 == null) {
//            throw new RuntimeException("Condition must be boolean");
//        }
//
//        // Unify then and else branches
//        Map<String, Type> sub2 = unifier.unify(thenType, elseType);
//        if (sub2 == null) {
//            throw new RuntimeException("Branches must have same type");
//        }
//
//        typeContext.putAll(sub1);
//        typeContext.putAll(sub2);
//        return Unifier.applySubstitution(thenType, sub2);
//    }
//
//    private Type handleNot(Not not, Map<String, Type> context) {
//        Type operandType = inferTerm(not.getOperand(), context);
//        Map<String, Type> sub = unifier.unify(operandType, new Constant("Bool"));
//        if (sub == null) {
//            throw new RuntimeException("NOT operand must be boolean");
//        }
//        typeContext.putAll(sub);
//        return new Constant("Bool");
//    }
//}