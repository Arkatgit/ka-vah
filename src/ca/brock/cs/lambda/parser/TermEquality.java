//package ca.brock.cs.lambda.parser;
//
///**
// * Utility class for comparing terms structurally.
// */
//public class TermEquality {
//
//    /**
//     * Compares two terms for structural equality.
//     */
//    public static boolean areEqual(Term t1, Term t2) {
//        if (t1 == t2) return true;
//        if (t1 == null || t2 == null) return false;
//        if (t1.getClass() != t2.getClass()) return false;
//
//        if (t1 instanceof Application) {
//            Application a1 = (Application) t1;
//            Application a2 = (Application) t2;
//            return areEqual(a1.getFunction(), a2.getFunction()) &&
//                areEqual(a1.getArgument(), a2.getArgument());
//        }
//        else if (t1 instanceof Abstraction) {
//            Abstraction ab1 = (Abstraction) t1;
//            Abstraction ab2 = (Abstraction) t2;
//            return ab1.getParameter().equals(ab2.getParameter()) &&
//                areEqual(ab1.getBody(), ab2.getBody());
//        }
//        else if (t1 instanceof Addition) {
//            Addition add1 = (Addition) t1;
//            Addition add2 = (Addition) t2;
//            return areEqual(add1.getLeft(), add2.getLeft()) &&
//                areEqual(add1.getRight(), add2.getRight());
//        }
//        else if (t1 instanceof Subtraction) {
//            Subtraction sub1 = (Subtraction) t1;
//            Subtraction sub2 = (Subtraction) t2;
//            return areEqual(sub1.getLeft(), sub2.getLeft()) &&
//                areEqual(sub1.getRight(), sub2.getRight());
//        }
//        else if (t1 instanceof Multiplication) {
//            Multiplication mul1 = (Multiplication) t1;
//            Multiplication mul2 = (Multiplication) t2;
//            return areEqual(mul1.getLeft(), mul2.getLeft()) &&
//                areEqual(mul1.getRight(), mul2.getRight());
//        }
//        else if (t1 instanceof Division) {
//            Division div1 = (Division) t1;
//            Division div2 = (Division) t2;
//            return areEqual(div1.getLeft(), div2.getLeft()) &&
//                areEqual(div1.getRight(), div2.getRight());
//        }
//        else if (t1 instanceof And) {
//            And and1 = (And) t1;
//            And and2 = (And) t2;
//            return areEqual(and1.getLeft(), and2.getLeft()) &&
//                areEqual(and1.getRight(), and2.getRight());
//        }
//        else if (t1 instanceof Or) {
//            Or or1 = (Or) t1;
//            Or or2 = (Or) t2;
//            return areEqual(or1.getLeft(), or2.getLeft()) &&
//                areEqual(or1.getRight(), or2.getRight());
//        }
//        else if (t1 instanceof Equal) {
//            Equal eq1 = (Equal) t1;
//            Equal eq2 = (Equal) t2;
//            return areEqual(eq1.getLeft(), eq2.getLeft()) &&
//                areEqual(eq1.getRight(), eq2.getRight());
//        }
//        else if (t1 instanceof LEqual) {
//            LEqual le1 = (LEqual) t1;
//            LEqual le2 = (LEqual) t2;
//            return areEqual(le1.getLeft(), le2.getLeft()) &&
//                areEqual(le1.getRight(), le2.getRight());
//        }
//        else if (t1 instanceof Not) {
//            Not not1 = (Not) t1;
//            Not not2 = (Not) t2;
//            return areEqual(not1.getOperand(), not2.getOperand());
//        }
//        else if (t1 instanceof Conditional) {
//            Conditional cond1 = (Conditional) t1;
//            Conditional cond2 = (Conditional) t2;
//            return areEqual(cond1.getCondition(), cond2.getCondition()) &&
//                areEqual(cond1.getTrueBranch(), cond2.getTrueBranch()) &&
//                areEqual(cond1.getFalseBranch(), cond2.getFalseBranch());
//        }
//        else if (t1 instanceof Recursion) {
//            Recursion rec1 = (Recursion) t1;
//            Recursion rec2 = (Recursion) t2;
//            return rec1.getName().equals(rec2.getName()) &&
//                areEqual(rec1.getBody(), rec2.getBody());
//        }
//        else if (t1 instanceof Match) {
//            Match match1 = (Match) t1;
//            Match match2 = (Match) t2;
//
//            if (!areEqual(match1.getInputTerm(), match2.getInputTerm())) {
//                return false;
//            }
//
//            if (match1.getCases().size() != match2.getCases().size()) {
//                return false;
//            }
//
//            for (int i = 0; i < match1.getCases().size(); i++) {
//                Match.Case c1 = match1.getCases().get(i);
//                Match.Case c2 = match2.getCases().get(i);
//
//                // Compare patterns
//                if (!c1.getPattern().toString().equals(c2.getPattern().toString())) {
//                    return false;
//                }
//
//                // Compare results
//                if (!areEqual(c1.getResult(), c2.getResult())) {
//                    return false;
//                }
//            }
//
//            return true;
//        }
//        else if (t1 instanceof Variable) {
//            return ((Variable) t1).getName().equals(((Variable) t2).getName());
//        }
//        else if (t1 instanceof IntegerLiteral) {
//            return ((IntegerLiteral) t1).getValue() == ((IntegerLiteral) t2).getValue();
//        }
//        else if (t1 instanceof BooleanLiteral) {
//            return ((BooleanLiteral) t1).getValue() == ((BooleanLiteral) t2).getValue();
//        }
//        else if (t1 instanceof Constant) {
//            return ((Constant) t1).getValue().equals(((Constant) t2).getValue());
//        }
//        else if (t1 instanceof Constructor) {
//            return ((Constructor) t1).getName().equals(((Constructor) t2).getName());
//        }
//
//        return false;
//    }
//
//    /**
//     * Generates a hash code for a term based on its structure.
//     */
//    public static int hashCode(Term term) {
//        if (term == null) return 0;
//
//        int hash = term.getClass().getName().hashCode();
//
//        if (term instanceof Application) {
//            Application app = (Application) term;
//            hash = 31 * hash + hashCode(app.getFunction());
//            hash = 31 * hash + hashCode(app.getArgument());
//        }
//        else if (term instanceof Abstraction) {
//            Abstraction abs = (Abstraction) term;
//            hash = 31 * hash + abs.getParameter().hashCode();
//            hash = 31 * hash + hashCode(abs.getBody());
//        }
//        else if (term instanceof Addition) {
//            Addition add = (Addition) term;
//            hash = 31 * hash + hashCode(add.getLeft());
//            hash = 31 * hash + hashCode(add.getRight());
//        }
//        else if (term instanceof Subtraction) {
//            Subtraction sub = (Subtraction) term;
//            hash = 31 * hash + hashCode(sub.getLeft());
//            hash = 31 * hash + hashCode(sub.getRight());
//        }
//        else if (term instanceof Multiplication) {
//            Multiplication mul = (Multiplication) term;
//            hash = 31 * hash + hashCode(mul.getLeft());
//            hash = 31 * hash + hashCode(mul.getRight());
//        }
//        else if (term instanceof Division) {
//            Division div = (Division) term;
//            hash = 31 * hash + hashCode(div.getLeft());
//            hash = 31 * hash + hashCode(div.getRight());
//        }
//        else if (term instanceof And) {
//            And and = (And) term;
//            hash = 31 * hash + hashCode(and.getLeft());
//            hash = 31 * hash + hashCode(and.getRight());
//        }
//        else if (term instanceof Or) {
//            Or or = (Or) term;
//            hash = 31 * hash + hashCode(or.getLeft());
//            hash = 31 * hash + hashCode(or.getRight());
//        }
//        else if (term instanceof Equal) {
//            Equal eq = (Equal) term;
//            hash = 31 * hash + hashCode(eq.getLeft());
//            hash = 31 * hash + hashCode(eq.getRight());
//        }
//        else if (term instanceof LEqual) {
//            LEqual le = (LEqual) term;
//            hash = 31 * hash + hashCode(le.getLeft());
//            hash = 31 * hash + hashCode(le.getRight());
//        }
//        else if (term instanceof Not) {
//            Not not = (Not) term;
//            hash = 31 * hash + hashCode(not.getOperand());
//        }
//        else if (term instanceof Conditional) {
//            Conditional cond = (Conditional) term;
//            hash = 31 * hash + hashCode(cond.getCondition());
//            hash = 31 * hash + hashCode(cond.getTrueBranch());
//            hash = 31 * hash + hashCode(cond.getFalseBranch());
//        }
//        else if (term instanceof Recursion) {
//            Recursion rec = (Recursion) term;
//            hash = 31 * hash + rec.getName().hashCode();
//            hash = 31 * hash + hashCode(rec.getBody());
//        }
//        else if (term instanceof Match) {
//            Match match = (Match) term;
//            hash = 31 * hash + hashCode(match.getInputTerm());
//            for (Match.Case c : match.getCases()) {
//                hash = 31 * hash + c.getPattern().toString().hashCode();
//                hash = 31 * hash + hashCode(c.getResult());
//            }
//        }
//        else if (term instanceof Variable) {
//            hash = 31 * hash + ((Variable) term).getName().hashCode();
//        }
//        else if (term instanceof IntegerLiteral) {
//            hash = 31 * hash + ((IntegerLiteral) term).getValue();
//        }
//        else if (term instanceof BooleanLiteral) {
//            hash = 31 * hash + (((BooleanLiteral) term).getValue() ? 1231 : 1237);
//        }
//        else if (term instanceof Constant) {
//            hash = 31 * hash + ((Constant) term).getValue().hashCode();
//        }
//        else if (term instanceof Constructor) {
//            hash = 31 * hash + ((Constructor) term).getName().hashCode();
//        }
//
//        return hash;
//    }
//}