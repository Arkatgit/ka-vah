package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.DefinedValue;
import ca.brock.cs.lambda.types.FunctionDefinition;
import java.util.*;

/**
 * Optimizes lambda calculus terms by Common Subexpression Elimination (CSE).
 * Identifies repeated non-trivial subexpressions and replaces them with
 * lambda abstractions (equivalent to local let-bindings).
 */
public class TermOptimizer {
    private int freshVarCounter = 0;

    public Map<String, DefinedValue> optimizeProgram(Map<String, DefinedValue> symbolMap) {
        Map<String, DefinedValue> optimized = new HashMap<>();
        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            if (entry.getValue() instanceof FunctionDefinition) {
                FunctionDefinition funcDef = (FunctionDefinition) entry.getValue();
                if (funcDef.getTerm() != null) {
                    Term optimizedTerm = eliminateCommonSubexpressions(funcDef.getTerm());
                    optimized.put(entry.getKey(), new FunctionDefinition(
                        funcDef.getName(),
                        funcDef.getType(),
                        optimizedTerm
                    ));
                } else {
                    optimized.put(entry.getKey(), entry.getValue());
                }
            } else {
                optimized.put(entry.getKey(), entry.getValue());
            }
        }
        return optimized;
    }

    public Term eliminateCommonSubexpressions(Term term) {
        Term current = term;
        // Optimization rounds
        for (int i = 0; i < 20; i++) {
            Map<String, Integer> counts = new HashMap<>();
            collectFrequencies(current, new ArrayList<>(), counts);

            String bestKey = null;
            int maxBenefit = 0;
            Term bestExample = null;

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                String key = entry.getKey();
                int count = entry.getValue();

                // Find an example of this key to check if it's "too small"
                Term example = findExample(current, key, new ArrayList<>());

                if (count > 1 && example != null && !isTooSmallToOptimize(example)) {
                    int size = calculateSizeFromKey(key);
                    int benefit = size * (count - 1);
                    if (benefit > maxBenefit) {
                        maxBenefit = benefit;
                        bestKey = key;
                        bestExample = example;
                    }
                }
            }

            if (bestKey == null) break;

            Term next = applyCSE(current, bestKey, new ArrayList<>());
            if (next == null || next.equals(current)) break;
            current = next;
        }
        return current;
    }

    /**
     * Prevents optimization of basic units which would increase term size
     * due to the overhead of creating a Lambda and an Application.
     */
    private boolean isTooSmallToOptimize(Term t) {
        if (t instanceof Variable || t instanceof Constant ||
            t instanceof IntegerLiteral || t instanceof BooleanLiteral) {
            return true;
        }
        // Ignore Data Constructors (like 'cons' or 'emptylist')
        if (t instanceof Constructor) {
            return true;
        }
        return false;
    }

    private int calculateSizeFromKey(String key) {
        // Simple heuristic: length of the structural key string
        return key.length();
    }

    private void collectFrequencies(Term term, List<String> binders, Map<String, Integer> counts) {
        if (term == null) return;
        String key = getStructuralKey(term, binders);
        counts.put(key, counts.getOrDefault(key, 0) + 1);

        if (term instanceof Application) {
            collectFrequencies(((Application) term).getFunction(), binders, counts);
            collectFrequencies(((Application) term).getArgument(), binders, counts);
        } else if (term instanceof Abstraction) {
            List<String> next = new ArrayList<>(binders);
            next.add(0, ((Abstraction) term).getParameter());
            collectFrequencies(((Abstraction) term).getBody(), next, counts);
        } else if (term instanceof Addition) {
            collectFrequencies(((Addition) term).getLeft(), binders, counts);
            collectFrequencies(((Addition) term).getRight(), binders, counts);
        } else if (term instanceof Subtraction) {
            collectFrequencies(((Subtraction) term).getLeft(), binders, counts);
            collectFrequencies(((Subtraction) term).getRight(), binders, counts);
        } else if (term instanceof Multiplication) {
            collectFrequencies(((Multiplication) term).getLeft(), binders, counts);
            collectFrequencies(((Multiplication) term).getRight(), binders, counts);
        } else if (term instanceof Division) {
            collectFrequencies(((Division) term).getLeft(), binders, counts);
            collectFrequencies(((Division) term).getRight(), binders, counts);
        } else if (term instanceof Equal) {
            collectFrequencies(((Equal) term).getLeft(), binders, counts);
            collectFrequencies(((Equal) term).getRight(), binders, counts);
        } else if (term instanceof LEqual) {
            collectFrequencies(((LEqual) term).getLeft(), binders, counts);
            collectFrequencies(((LEqual) term).getRight(), binders, counts);
        } else if (term instanceof And) {
            collectFrequencies(((And) term).getLeft(), binders, counts);
            collectFrequencies(((And) term).getRight(), binders, counts);
        } else if (term instanceof Or) {
            collectFrequencies(((Or) term).getLeft(), binders, counts);
            collectFrequencies(((Or) term).getRight(), binders, counts);
        } else if (term instanceof Not) {
            collectFrequencies(((Not) term).getOperand(), binders, counts);
        } else if (term instanceof Conditional) {
            collectFrequencies(((Conditional) term).getCondition(), binders, counts);
            collectFrequencies(((Conditional) term).getTrueBranch(), binders, counts);
            collectFrequencies(((Conditional) term).getFalseBranch(), binders, counts);
        } else if (term instanceof Recursion) {
            List<String> next = new ArrayList<>(binders);
            next.add(0, ((Recursion) term).getName());
            collectFrequencies(((Recursion) term).getBody(), next, counts);
        } else if (term instanceof Match) {
            Match m = (Match) term;
            collectFrequencies(m.getInputTerm(), binders, counts);
            for (Match.Case c : m.getCases()) {
                List<String> next = new ArrayList<>(binders);
                next.addAll(0, c.getPattern().getBoundVariables());
                collectFrequencies(c.getResult(), next, counts);
            }
        }
    }

    private String getStructuralKey(Term term, List<String> binders) {
        if (term instanceof Variable) {
            int index = binders.indexOf(((Variable) term).getName());
            return index >= 0 ? "B" + index : "F:" + ((Variable) term).getName();
        } else if (term instanceof IntegerLiteral) {
            return "I:" + ((IntegerLiteral) term).getValue();
        } else if (term instanceof BooleanLiteral) {
            return "L:" + ((BooleanLiteral) term).getValue();
        } else if (term instanceof Constant) {
            return "C:" + ((Constant) term).getValue();
        } else if (term instanceof Constructor) {
            return "K:" + ((Constructor) term).getName();
        } else if (term instanceof Application) {
            return "A(" + getStructuralKey(((Application) term).getFunction(), binders) + "," + getStructuralKey(((Application) term).getArgument(), binders) + ")";
        } else if (term instanceof Abstraction) {
            List<String> next = new ArrayList<>(binders);
            next.add(0, ((Abstraction) term).getParameter());
            return "λ(" + getStructuralKey(((Abstraction) term).getBody(), next) + ")";
        } else if (term instanceof Addition) {
            return "+(" + getStructuralKey(((Addition) term).getLeft(), binders) + "," + getStructuralKey(((Addition) term).getRight(), binders) + ")";
        } else if (term instanceof Subtraction) {
            return "-(" + getStructuralKey(((Subtraction) term).getLeft(), binders) + "," + getStructuralKey(((Subtraction) term).getRight(), binders) + ")";
        } else if (term instanceof Multiplication) {
            return "*(" + getStructuralKey(((Multiplication) term).getLeft(), binders) + "," + getStructuralKey(((Multiplication) term).getRight(), binders) + ")";
        } else if (term instanceof Division) {
            return "/(" + getStructuralKey(((Division) term).getLeft(), binders) + "," + getStructuralKey(((Division) term).getRight(), binders) + ")";
        } else if (term instanceof Equal) {
            return "=(" + getStructuralKey(((Equal) term).getLeft(), binders) + "," + getStructuralKey(((Equal) term).getRight(), binders) + ")";
        } else if (term instanceof LEqual) {
            return "<=(" + getStructuralKey(((LEqual) term).getLeft(), binders) + "," + getStructuralKey(((LEqual) term).getRight(), binders) + ")";
        } else if (term instanceof And) {
            return "&(" + getStructuralKey(((And) term).getLeft(), binders) + "," + getStructuralKey(((And) term).getRight(), binders) + ")";
        } else if (term instanceof Or) {
            return "|(" + getStructuralKey(((Or) term).getLeft(), binders) + "," + getStructuralKey(((Or) term).getRight(), binders) + ")";
        } else if (term instanceof Not) {
            return "!(" + getStructuralKey(((Not) term).getOperand(), binders) + ")";
        } else if (term instanceof Conditional) {
            Conditional c = (Conditional) term;
            return "if(" + getStructuralKey(c.getCondition(), binders) + "," + getStructuralKey(c.getTrueBranch(), binders) + "," + getStructuralKey(c.getFalseBranch(), binders) + ")";
        } else if (term instanceof Recursion) {
            List<String> next = new ArrayList<>(binders);
            next.add(0, ((Recursion) term).getName());
            return "rec(" + getStructuralKey(((Recursion) term).getBody(), next) + ")";
        } else if (term instanceof Match) {
            Match m = (Match) term;
            StringBuilder sb = new StringBuilder("match(");
            sb.append(getStructuralKey(m.getInputTerm(), binders));
            for (Match.Case c : m.getCases()) {
                sb.append(",case(");
                List<String> next = new ArrayList<>(binders);
                next.addAll(0, c.getPattern().getBoundVariables());
                sb.append(getStructuralKey(c.getResult(), next)).append(")");
            }
            return sb.append(")").toString();
        }
        return "U";
    }

    private Term applyCSE(Term term, String targetKey, List<String> binders) {
        if (term == null) return null;

        if (canExtractHere(term, targetKey, binders)) {
            Term candidate = findExample(term, targetKey, binders);
            if (candidate != null) {
                String freshVar = "y" + (++freshVarCounter);
                Term body = replaceWithVariable(term, targetKey, binders, freshVar);
                // Extract into Application(Abstraction, Argument)
                return new Application(new Abstraction(freshVar, body), candidate);
            }
        }

        // Standard recursion for all term types...
        if (term instanceof Application) {
            Application app = (Application) term;
            Term f = applyCSE(app.getFunction(), targetKey, binders);
            if (f != app.getFunction()) return new Application(f, app.getArgument());
            Term a = applyCSE(app.getArgument(), targetKey, binders);
            if (a != app.getArgument()) return new Application(app.getFunction(), a);
        } else if (term instanceof Abstraction) {
            Abstraction abs = (Abstraction) term;
            List<String> next = new ArrayList<>(binders);
            next.add(0, abs.getParameter());
            Term b = applyCSE(abs.getBody(), targetKey, next);
            if (b != abs.getBody()) return new Abstraction(abs.getParameter(), b);
        } else if (term instanceof Addition) {
            Addition op = (Addition) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new Addition(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new Addition(op.getLeft(), r);
        } else if (term instanceof Subtraction) {
            Subtraction op = (Subtraction) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new Subtraction(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new Subtraction(op.getLeft(), r);
        } else if (term instanceof Multiplication) {
            Multiplication op = (Multiplication) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new Multiplication(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new Multiplication(op.getLeft(), r);
        } else if (term instanceof Division) {
            Division op = (Division) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new Division(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new Division(op.getLeft(), r);
        } else if (term instanceof Equal) {
            Equal op = (Equal) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new Equal(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new Equal(op.getLeft(), r);
        } else if (term instanceof LEqual) {
            LEqual op = (LEqual) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new LEqual(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new LEqual(op.getLeft(), r);
        } else if (term instanceof And) {
            And op = (And) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new And(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new And(op.getLeft(), r);
        } else if (term instanceof Or) {
            Or op = (Or) term;
            Term l = applyCSE(op.getLeft(), targetKey, binders);
            if (l != op.getLeft()) return new Or(l, op.getRight());
            Term r = applyCSE(op.getRight(), targetKey, binders);
            if (r != op.getRight()) return new Or(op.getLeft(), r);
        } else if (term instanceof Not) {
            Term o = applyCSE(((Not) term).getOperand(), targetKey, binders);
            if (o != ((Not) term).getOperand()) return new Not(o);
        } else if (term instanceof Conditional) {
            Conditional c = (Conditional) term;
            Term co = applyCSE(c.getCondition(), targetKey, binders);
            if (co != c.getCondition()) return new Conditional(co, c.getTrueBranch(), c.getFalseBranch());
            Term t = applyCSE(c.getTrueBranch(), targetKey, binders);
            if (t != c.getTrueBranch()) return new Conditional(c.getCondition(), t, c.getFalseBranch());
            Term f = applyCSE(c.getFalseBranch(), targetKey, binders);
            if (f != c.getFalseBranch()) return new Conditional(c.getCondition(), c.getTrueBranch(), f);
        } else if (term instanceof Recursion) {
            List<String> next = new ArrayList<>(binders);
            next.add(0, ((Recursion) term).getName());
            Term b = applyCSE(((Recursion) term).getBody(), targetKey, next);
            if (b != ((Recursion) term).getBody()) return new Recursion(((Recursion) term).getName(), b);
        } else if (term instanceof Match) {
            Match m = (Match) term;
            Term in = applyCSE(m.getInputTerm(), targetKey, binders);
            if (in != m.getInputTerm()) return new Match(in, m.getCases());
            List<Match.Case> newCases = new ArrayList<>();
            boolean changed = false;
            for (Match.Case c : m.getCases()) {
                List<String> next = new ArrayList<>(binders);
                next.addAll(0, c.getPattern().getBoundVariables());
                Term res = applyCSE(c.getResult(), targetKey, next);
                if (res != c.getResult()) {
                    newCases.add(new Match.Case(c.getPattern(), res));
                    changed = true;
                } else {
                    newCases.add(c);
                }
            }
            if (changed) return new Match(m.getInputTerm(), newCases);
        }
        return term;
    }

    private boolean canExtractHere(Term node, String targetKey, List<String> binders) {
        String nodeKey = getStructuralKey(node, binders);
        int count = countOccurrences(nodeKey, targetKey);
        return count > 1 && isKeyScopeSafe(targetKey, binders.size());
    }

    private boolean isKeyScopeSafe(String key, int stackSize) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("B(\\d+)").matcher(key);
        while (m.find()) {
            if (Integer.parseInt(m.group(1)) >= stackSize) return false;
        }
        return true;
    }

    private int countOccurrences(String text, String target) {
        int count = 0, index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }

    private Term findExample(Term node, String targetKey, List<String> binders) {
        if (getStructuralKey(node, binders).equals(targetKey)) return node;
        // Search subterms...
        if (node instanceof Application) {
            Term t = findExample(((Application) node).getFunction(), targetKey, binders);
            return (t != null) ? t : findExample(((Application) node).getArgument(), targetKey, binders);
        } else if (node instanceof Abstraction) {
            List<String> next = new ArrayList<>(binders);
            next.add(0, ((Abstraction) node).getParameter());
            return findExample(((Abstraction) node).getBody(), targetKey, next);
        } else if (node instanceof Addition) {
            Term t = findExample(((Addition) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((Addition) node).getRight(), targetKey, binders);
        } else if (node instanceof Subtraction) {
            Term t = findExample(((Subtraction) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((Subtraction) node).getRight(), targetKey, binders);
        } else if (node instanceof Multiplication) {
            Term t = findExample(((Multiplication) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((Multiplication) node).getRight(), targetKey, binders);
        } else if (node instanceof Division) {
            Term t = findExample(((Division) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((Division) node).getRight(), targetKey, binders);
        } else if (node instanceof Equal) {
            Term t = findExample(((Equal) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((Equal) node).getRight(), targetKey, binders);
        } else if (node instanceof LEqual) {
            Term t = findExample(((LEqual) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((LEqual) node).getRight(), targetKey, binders);
        } else if (node instanceof And) {
            Term t = findExample(((And) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((And) node).getRight(), targetKey, binders);
        } else if (node instanceof Or) {
            Term t = findExample(((Or) node).getLeft(), targetKey, binders);
            return (t != null) ? t : findExample(((Or) node).getRight(), targetKey, binders);
        } else if (node instanceof Not) {
            return findExample(((Not) node).getOperand(), targetKey, binders);
        } else if (node instanceof Conditional) {
            Conditional c = (Conditional) node;
            Term t = findExample(c.getCondition(), targetKey, binders);
            if (t != null) return t;
            t = findExample(c.getTrueBranch(), targetKey, binders);
            return (t != null) ? t : findExample(c.getFalseBranch(), targetKey, binders);
        } else if (node instanceof Recursion) {
            List<String> next = new ArrayList<>(binders);
            next.add(0, ((Recursion) node).getName());
            return findExample(((Recursion) node).getBody(), targetKey, next);
        } else if (node instanceof Match) {
            Match m = (Match) node;
            Term t = findExample(m.getInputTerm(), targetKey, binders);
            if (t != null) return t;
            for (Match.Case c : m.getCases()) {
                List<String> next = new ArrayList<>(binders);
                next.addAll(0, c.getPattern().getBoundVariables());
                t = findExample(c.getResult(), targetKey, next);
                if (t != null) return t;
            }
        }
        return null;
    }

    private Term replaceWithVariable(Term node, String targetKey, List<String> binders, String varName) {
        if (node == null) return null;

        // Check if the current node matches the subexpression we are extracting
        if (getStructuralKey(node, binders).equals(targetKey)) {
            return new Variable(varName);
        }

        if (node instanceof Application) {
            Application app = (Application) node;
            return new Application(
                replaceWithVariable(app.getFunction(), targetKey, binders, varName),
                replaceWithVariable(app.getArgument(), targetKey, binders, varName)
            );
        } else if (node instanceof Abstraction) {
            Abstraction abs = (Abstraction) node;
            List<String> next = new ArrayList<>(binders);
            next.add(0, abs.getParameter());
            return new Abstraction(abs.getParameter(), replaceWithVariable(abs.getBody(), targetKey, next, varName));
        } else if (node instanceof Addition) {
            Addition op = (Addition) node;
            return new Addition(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof Subtraction) {
            Subtraction op = (Subtraction) node;
            return new Subtraction(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof Multiplication) {
            Multiplication op = (Multiplication) node;
            return new Multiplication(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof Division) {
            Division op = (Division) node;
            return new Division(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof Equal) {
            Equal op = (Equal) node;
            return new Equal(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof LEqual) {
            LEqual op = (LEqual) node;
            return new LEqual(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof And) {
            And op = (And) node;
            return new And(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof Or) {
            Or op = (Or) node;
            return new Or(replaceWithVariable(op.getLeft(), targetKey, binders, varName), replaceWithVariable(op.getRight(), targetKey, binders, varName));
        } else if (node instanceof Not) {
            return new Not(replaceWithVariable(((Not) node).getOperand(), targetKey, binders, varName));
        } else if (node instanceof Conditional) {
            Conditional c = (Conditional) node;
            return new Conditional(
                replaceWithVariable(c.getCondition(), targetKey, binders, varName),
                replaceWithVariable(c.getTrueBranch(), targetKey, binders, varName),
                replaceWithVariable(c.getFalseBranch(), targetKey, binders, varName)
            );
        } else if (node instanceof Recursion) {
            Recursion rec = (Recursion) node;
            List<String> next = new ArrayList<>(binders);
            next.add(0, rec.getName());
            return new Recursion(rec.getName(), replaceWithVariable(rec.getBody(), targetKey, next, varName));
        } else if (node instanceof Match) {
            Match match = (Match) node;
            List<Match.Case> newCases = new ArrayList<>();
            for (Match.Case c : match.getCases()) {
                List<String> next = new ArrayList<>(binders);
                next.addAll(0, c.getPattern().getBoundVariables());
                newCases.add(new Match.Case(c.getPattern(), replaceWithVariable(c.getResult(), targetKey, next, varName)));
            }
            return new Match(replaceWithVariable(match.getInputTerm(), targetKey, binders, varName), newCases);
        }

        return node;
    }
}


//package ca.brock.cs.lambda.parser;
//
//import ca.brock.cs.lambda.parser.*;
//import ca.brock.cs.lambda.types.DefinedValue;
//import ca.brock.cs.lambda.types.FunctionDefinition;
//
//import java.util.*;
//
///**
// * Optimizes lambda calculus terms by Common Subexpression Elimination (CSE).
// * Creates a Directed Acyclic Graph (DAG) where identical subterms are shared.
// */
//public class TermOptimizer {
//
//    private int freshVarCounter = 0;
//
//    /**
//     * Optimize an entire program with CSE.
//     */
//    public Map<String, DefinedValue> optimizeProgram(Map<String, DefinedValue> symbolMap) {
//        Map<String, DefinedValue> optimized = new HashMap<>();
//
//        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
//            if (entry.getValue() instanceof FunctionDefinition) {
//                FunctionDefinition funcDef = (FunctionDefinition) entry.getValue();
//                Term optimizedTerm = eliminateCommonSubexpressions(funcDef.getTerm());
//
//                FunctionDefinition optimizedFunc = new FunctionDefinition(
//                    funcDef.getName(),
//                    funcDef.getType(),
//                    optimizedTerm
//                );
//                optimized.put(entry.getKey(), optimizedFunc);
//            } else {
//                optimized.put(entry.getKey(), entry.getValue());
//            }
//        }
//
//        return optimized;
//    }
//
//    /**
//     * Main CSE algorithm.
//     */
//    public Term eliminateCommonSubexpressions(Term term) {
//        // First pass: find all subterms and their frequencies
//        Map<String, SubtermInfo> subtermInfo = new HashMap<>();
//        findCommonSubterms(term, new HashSet<>(), subtermInfo);
//
//        // Second pass: rebuild with extracted common subexpressions
//        return rebuildWithCSE(term, subtermInfo, new HashSet<>(), new HashMap<>());
//    }
//
//    private static class SubtermInfo {
//        Term term;
//        int count;
//        String freshVar;
//        boolean extracted;
//
//        SubtermInfo(Term term) {
//            this.term = term;
//            this.count = 1;
//            this.extracted = false;
//        }
//    }
//
//    private void findCommonSubterms(Term term, Set<String> boundVars,
//        Map<String, SubtermInfo> subtermInfo) {
//        if (term == null) return;
//
//        // Skip trivial terms that aren't worth extracting
//        if (isTrivialTerm(term)) {
//            return;
//        }
//
//        String key = generateTermKey(term, boundVars);
//        SubtermInfo info = subtermInfo.get(key);
//        if (info == null) {
//            info = new SubtermInfo(term);
//            subtermInfo.put(key, info);
//        } else {
//            info.count++;
//        }
//
//        // Recursively process subterms
//        if (term instanceof Application) {
//            Application app = (Application) term;
//            findCommonSubterms(app.getFunction(), boundVars, subtermInfo);
//            findCommonSubterms(app.getArgument(), boundVars, subtermInfo);
//        } else if (term instanceof Abstraction) {
//            Abstraction abs = (Abstraction) term;
//            Set<String> newBoundVars = new HashSet<>(boundVars);
//            newBoundVars.add(abs.getParameter());
//            findCommonSubterms(abs.getBody(), newBoundVars, subtermInfo);
//        } else if (term instanceof Addition) {
//            Addition add = (Addition) term;
//            findCommonSubterms(add.getLeft(), boundVars, subtermInfo);
//            findCommonSubterms(add.getRight(), boundVars, subtermInfo);
//        } else if (term instanceof Multiplication) {
//            Multiplication mul = (Multiplication) term;
//            findCommonSubterms(mul.getLeft(), boundVars, subtermInfo);
//            findCommonSubterms(mul.getRight(), boundVars, subtermInfo);
//        } else if (term instanceof Conditional) {
//            Conditional cond = (Conditional) term;
//            findCommonSubterms(cond.getCondition(), boundVars, subtermInfo);
//            findCommonSubterms(cond.getTrueBranch(), boundVars, subtermInfo);
//            findCommonSubterms(cond.getFalseBranch(), boundVars, subtermInfo);
//        } else if (term instanceof Recursion) {
//            Recursion rec = (Recursion) term;
//            Set<String> newBoundVars = new HashSet<>(boundVars);
//            newBoundVars.add(rec.getName());
//            findCommonSubterms(rec.getBody(), newBoundVars, subtermInfo);
//        } else if (term instanceof Match) {
//            Match match = (Match) term;
//            findCommonSubterms(match.getInputTerm(), boundVars, subtermInfo);
//            for (Match.Case c : match.getCases()) {
//                Set<String> caseBoundVars = new HashSet<>(boundVars);
//                caseBoundVars.addAll(c.getPattern().getBoundVariables());
//                findCommonSubterms(c.getResult(), caseBoundVars, subtermInfo);
//            }
//        }
//    }
//
////    private String generateTermKey(Term term, Set<String> boundVars) {
////        // Generate a key that identifies equivalent terms
////        // This handles alpha-equivalence by normalizing bound variables
////
////        if (term instanceof Variable) {
////            Variable var = (Variable) term;
////            String varName = var.getName();
////            if (boundVars.contains(varName)) {
////                return "bound:" + varName;
////            } else {
////                return "free:" + varName;
////            }
////        } else if (term instanceof IntegerLiteral) {
////            return "int:" + ((IntegerLiteral) term).getValue();
////        } else if (term instanceof BooleanLiteral) {
////            return "bool:" + ((BooleanLiteral) term).getValue();
////        } else if (term instanceof Constant) {
////            return "const:" + ((Constant) term).getValue();
////        } else if (term instanceof Constructor) {
////            return "constr:" + ((Constructor) term).getName();
////        } else if (term instanceof Application) {
////            Application app = (Application) term;
////            return "app:" + generateTermKey(app.getFunction(), boundVars) +
////                "@" + generateTermKey(app.getArgument(), boundVars);
////        } else if (term instanceof Abstraction) {
////            Abstraction abs = (Abstraction) term;
////            Set<String> newBoundVars = new HashSet<>(boundVars);
////            newBoundVars.add(abs.getParameter());
////            return "abs:" + abs.getParameter() + ":" +
////                generateTermKey(abs.getBody(), newBoundVars);
////        } else if (term instanceof Addition) {
////            Addition add = (Addition) term;
////            return "add:" + generateTermKey(add.getLeft(), boundVars) +
////                "+" + generateTermKey(add.getRight(), boundVars);
////        } else if (term instanceof Multiplication) {
////            Multiplication mul = (Multiplication) term;
////            return "mul:" + generateTermKey(mul.getLeft(), boundVars) +
////                "*" + generateTermKey(mul.getRight(), boundVars);
////        } else if (term instanceof Conditional) {
////            Conditional cond = (Conditional) term;
////            return "if:" + generateTermKey(cond.getCondition(), boundVars) +
////                "?" + generateTermKey(cond.getTrueBranch(), boundVars) +
////                ":" + generateTermKey(cond.getFalseBranch(), boundVars);
////        }
////
////        // Fallback for other term types
////        return term.getClass().getSimpleName() + ":" + term.toString();
////    }
//    private String generateTermKey(Term term, Set<String> boundVars) {
//        StringBuilder sb = new StringBuilder();
//        generateTermKeyHelper(term, boundVars, sb);
//        return sb.toString();
//    }
//
//    private void generateTermKeyHelper(Term term, Set<String> boundVars, StringBuilder sb) {
//        if (term instanceof Variable) {
//            Variable var = (Variable) term;
//            String varName = var.getName();
//            if (boundVars.contains(varName)) {
//                sb.append("bound:").append(varName);
//            } else {
//                sb.append("free:").append(varName);
//            }
//        } else if (term instanceof IntegerLiteral) {
//            sb.append("int:").append(((IntegerLiteral) term).getValue());
//        } else if (term instanceof BooleanLiteral) {
//            sb.append("bool:").append(((BooleanLiteral) term).getValue());
//        } else if (term instanceof Constant) {
//            sb.append("const:").append(((Constant) term).getValue());
//        } else if (term instanceof Constructor) {
//            sb.append("constr:").append(((Constructor) term).getName());
//        } else if (term instanceof Application) {
//            sb.append("app(");
//            generateTermKeyHelper(((Application) term).getFunction(), boundVars, sb);
//            sb.append(",");
//            generateTermKeyHelper(((Application) term).getArgument(), boundVars, sb);
//            sb.append(")");
//        } else if (term instanceof Abstraction) {
//            sb.append("abs(");
//            Abstraction abs = (Abstraction) term;
//            sb.append(abs.getParameter()).append(":");
//            Set<String> newBoundVars = new HashSet<>(boundVars);
//            newBoundVars.add(abs.getParameter());
//            generateTermKeyHelper(abs.getBody(), newBoundVars, sb);
//            sb.append(")");
//        } else if (term instanceof Addition) {
//            sb.append("add(");
//            Addition add = (Addition) term;
//            generateTermKeyHelper(add.getLeft(), boundVars, sb);
//            sb.append(",");
//            generateTermKeyHelper(add.getRight(), boundVars, sb);
//            sb.append(")");
//        } else if (term instanceof Multiplication) {
//            sb.append("mul(");
//            Multiplication mul = (Multiplication) term;
//            generateTermKeyHelper(mul.getLeft(), boundVars, sb);
//            sb.append(",");
//            generateTermKeyHelper(mul.getRight(), boundVars, sb);
//            sb.append(")");
//        } else if (term instanceof Subtraction) {
//            sb.append("sub(");
//            Subtraction sub = (Subtraction) term;
//            generateTermKeyHelper(sub.getLeft(), boundVars, sb);
//            sb.append(",");
//            generateTermKeyHelper(sub.getRight(), boundVars, sb);
//            sb.append(")");
//        } else if (term instanceof Division) {
//            sb.append("div(");
//            Division div = (Division) term;
//            generateTermKeyHelper(div.getLeft(), boundVars, sb);
//            sb.append(",");
//            generateTermKeyHelper(div.getRight(), boundVars, sb);
//            sb.append(")");
//        } else if (term instanceof Conditional) {
//            sb.append("if(");
//            Conditional cond = (Conditional) term;
//            generateTermKeyHelper(cond.getCondition(), boundVars, sb);
//            sb.append(",");
//            generateTermKeyHelper(cond.getTrueBranch(), boundVars, sb);
//            sb.append(",");
//            generateTermKeyHelper(cond.getFalseBranch(), boundVars, sb);
//            sb.append(")");
//        } else if (term instanceof Recursion) {
//            sb.append("rec(");
//            Recursion rec = (Recursion) term;
//            sb.append(rec.getName()).append(":");
//            Set<String> newBoundVars = new HashSet<>(boundVars);
//            newBoundVars.add(rec.getName());
//            generateTermKeyHelper(rec.getBody(), newBoundVars, sb);
//            sb.append(")");
//        } else {
//            // Fallback
//            sb.append(term.getClass().getSimpleName()).append(":").append(term.toString());
//        }
//    }
//    private Term rebuildWithCSE(Term term, Map<String, SubtermInfo> subtermInfo,
//        Set<String> boundVars, Map<String, Term> cache) {
//        if (term == null) return null;
//
//        String key = generateTermKey(term, boundVars);
//
//        // Check cache
//        if (cache.containsKey(key)) {
//            return cache.get(key);
//        }
//
//        // Check if this term should be extracted
//        SubtermInfo info = subtermInfo.get(key);
//        if (info != null && info.count > 1 && !isTrivialTerm(term) && !info.extracted) {
//            // This is a common subexpression worth extracting
//            info.extracted = true;
//            info.freshVar = "y" + (++freshVarCounter);
//
//            // Extract by creating a lambda
//            Term extracted = extractSubexpression(term, info.freshVar, subtermInfo, boundVars, cache);
//            cache.put(key, extracted);
//            return extracted;
//        }
//
//        // Rebuild term recursively
//        Term result;
//
//        if (term instanceof Variable || term instanceof IntegerLiteral ||
//            term instanceof BooleanLiteral || term instanceof Constant ||
//            term instanceof Constructor) {
//            result = term;
//        } else if (term instanceof Application) {
//            Application app = (Application) term;
//            Term func = rebuildWithCSE(app.getFunction(), subtermInfo, boundVars, cache);
//            Term arg = rebuildWithCSE(app.getArgument(), subtermInfo, boundVars, cache);
//            result = new Application(func, arg);
//        } else if (term instanceof Abstraction) {
//            Abstraction abs = (Abstraction) term;
//            Set<String> newBoundVars = new HashSet<>(boundVars);
//            newBoundVars.add(abs.getParameter());
//            Term body = rebuildWithCSE(abs.getBody(), subtermInfo, newBoundVars, cache);
//
//            // Check if we need to wrap extracted variables
//            result = wrapExtractedVariables(abs.getParameter(), body, subtermInfo, newBoundVars);
//        } else if (term instanceof Addition) {
//            Addition add = (Addition) term;
//            Term left = rebuildWithCSE(add.getLeft(), subtermInfo, boundVars, cache);
//            Term right = rebuildWithCSE(add.getRight(), subtermInfo, boundVars, cache);
//            result = new Addition(left, right);
//        } else if (term instanceof Multiplication) {
//            Multiplication mul = (Multiplication) term;
//            Term left = rebuildWithCSE(mul.getLeft(), subtermInfo, boundVars, cache);
//            Term right = rebuildWithCSE(mul.getRight(), subtermInfo, boundVars, cache);
//
//            // Check for x * x pattern
//            if (left.toString().equals(right.toString())) {
//                // Extract the duplicate
//                result = extractMultiplicationSquare(left, subtermInfo, boundVars, cache);
//            } else {
//                result = new Multiplication(left, right);
//            }
//        } else if (term instanceof Conditional) {
//            Conditional cond = (Conditional) term;
//            Term condition = rebuildWithCSE(cond.getCondition(), subtermInfo, boundVars, cache);
//            Term trueBranch = rebuildWithCSE(cond.getTrueBranch(), subtermInfo, boundVars, cache);
//            Term falseBranch = rebuildWithCSE(cond.getFalseBranch(), subtermInfo, boundVars, cache);
//            result = new Conditional(condition, trueBranch, falseBranch);
//        } else if (term instanceof Recursion) {
//            Recursion rec = (Recursion) term;
//            Set<String> newBoundVars = new HashSet<>(boundVars);
//            newBoundVars.add(rec.getName());
//            Term body = rebuildWithCSE(rec.getBody(), subtermInfo, newBoundVars, cache);
//            result = new Recursion(rec.getName(), body);
//        } else {
//            // For other term types, rebuild recursively
//            result = rebuildTermRecursive(term, subtermInfo, boundVars, cache);
//        }
//
//        cache.put(key, result);
//        return result;
//    }
//
//    private Term extractSubexpression(Term expr, String freshVar,
//        Map<String, SubtermInfo> subtermInfo,
//        Set<String> boundVars, Map<String, Term> cache) {
//        // Create a variable reference
//        Variable varRef = new Variable(freshVar);
//
//        // For now, just return the variable reference
//        // The actual extraction will be handled by the parent context
//        return varRef;
//    }
//
//    private Term wrapExtractedVariables(String param, Term body,
//        Map<String, SubtermInfo> subtermInfo,
//        Set<String> boundVars) {
//        // Find which subterms were extracted in this scope
//        List<String> extractedVars = new ArrayList<>();
//        for (SubtermInfo info : subtermInfo.values()) {
//            if (info.extracted && info.freshVar != null &&
//                !boundVars.contains(info.freshVar)) {
//                extractedVars.add(info.freshVar);
//            }
//        }
//
//        // Create nested lambdas for extracted variables
//        Term result = body;
//        for (String var : extractedVars) {
//            // Find the original expression for this variable
//            Term originalExpr = null;
//            for (SubtermInfo info : subtermInfo.values()) {
//                if (info.freshVar != null && info.freshVar.equals(var)) {
//                    originalExpr = info.term;
//                    break;
//                }
//            }
//
//            if (originalExpr != null) {
//                // Create: (λvar. result) originalExpr
//                result = new Application(new Abstraction(var, result), originalExpr);
//            }
//        }
//
//        return new Abstraction(param, result);
//    }
//
//    private Term extractMultiplicationSquare(Term operand,
//        Map<String, SubtermInfo> subtermInfo,
//        Set<String> boundVars, Map<String, Term> cache) {
//        // For x * x, extract x as a common subexpression
//        String freshVar = "y" + (++freshVarCounter);
//
//        // Check if operand is already worth extracting
//        String operandKey = generateTermKey(operand, boundVars);
//        SubtermInfo info = subtermInfo.get(operandKey);
//
//        if (info != null && info.count > 1 && !isTrivialTerm(operand)) {
//            // Operand is itself a common subexpression
//            info.extracted = true;
//            info.freshVar = freshVar;
//
//            // Create: (λfreshVar. freshVar * freshVar) operand
//            Abstraction squareFunc = new Abstraction(freshVar,
//                new Multiplication(new Variable(freshVar), new Variable(freshVar)));
//            return new Application(squareFunc, operand);
//        } else {
//            // Operand is not worth extracting, just square it directly
//            return new Multiplication(operand, operand);
//        }
//    }
//
//    private Term rebuildTermRecursive(Term term, Map<String, SubtermInfo> subtermInfo,
//        Set<String> boundVars, Map<String, Term> cache) {
//        // Generic recursive rebuilding for term types not specifically handled
//        if (term instanceof Subtraction) {
//            Subtraction sub = (Subtraction) term;
//            Term left = rebuildWithCSE(sub.getLeft(), subtermInfo, boundVars, cache);
//            Term right = rebuildWithCSE(sub.getRight(), subtermInfo, boundVars, cache);
//            return new Subtraction(left, right);
//        } else if (term instanceof Division) {
//            Division div = (Division) term;
//            Term left = rebuildWithCSE(div.getLeft(), subtermInfo, boundVars, cache);
//            Term right = rebuildWithCSE(div.getRight(), subtermInfo, boundVars, cache);
//            return new Division(left, right);
//        } else if (term instanceof And) {
//            And and = (And) term;
//            Term left = rebuildWithCSE(and.getLeft(), subtermInfo, boundVars, cache);
//            Term right = rebuildWithCSE(and.getRight(), subtermInfo, boundVars, cache);
//            return new And(left, right);
//        } else if (term instanceof Or) {
//            Or or = (Or) term;
//            Term left = rebuildWithCSE(or.getLeft(), subtermInfo, boundVars, cache);
//            Term right = rebuildWithCSE(or.getRight(), subtermInfo, boundVars, cache);
//            return new Or(left, right);
//        } else if (term instanceof Not) {
//            Not not = (Not) term;
//            Term operand = rebuildWithCSE(not.getOperand(), subtermInfo, boundVars, cache);
//            return new Not(operand);
//        } else if (term instanceof Match) {
//            Match match = (Match) term;
//            Term input = rebuildWithCSE(match.getInputTerm(), subtermInfo, boundVars, cache);
//            List<Match.Case> cases = new ArrayList<>();
//            for (Match.Case c : match.getCases()) {
//                Set<String> caseBoundVars = new HashSet<>(boundVars);
//                caseBoundVars.addAll(c.getPattern().getBoundVariables());
//                Term result = rebuildWithCSE(c.getResult(), subtermInfo, caseBoundVars, cache);
//                cases.add(new Match.Case(c.getPattern(), result));
//            }
//            return new Match(input, cases);
//        }
//
//        return term;
//    }
//
//    private boolean isTrivialTerm(Term term) {
//        return term instanceof Variable || term instanceof IntegerLiteral ||
//            term instanceof BooleanLiteral || term instanceof Constant ||
//            term instanceof Constructor;
//    }
//
//    /**
//     * Count nodes in a term.
//     */
//    public int countNodes(Term term) {
//        if (term == null) return 0;
//
//        if (term instanceof Application) {
//            Application app = (Application) term;
//            return 1 + countNodes(app.getFunction()) + countNodes(app.getArgument());
//        } else if (term instanceof Abstraction) {
//            Abstraction abs = (Abstraction) term;
//            return 1 + countNodes(abs.getBody());
//        } else if (term instanceof Addition) {
//            Addition add = (Addition) term;
//            return 1 + countNodes(add.getLeft()) + countNodes(add.getRight());
//        } else if (term instanceof Multiplication) {
//            Multiplication mul = (Multiplication) term;
//            return 1 + countNodes(mul.getLeft()) + countNodes(mul.getRight());
//        } else if (term instanceof Conditional) {
//            Conditional cond = (Conditional) term;
//            return 1 + countNodes(cond.getCondition()) +
//                countNodes(cond.getTrueBranch()) + countNodes(cond.getFalseBranch());
//        } else if (term instanceof Recursion) {
//            Recursion rec = (Recursion) term;
//            return 1 + countNodes(rec.getBody());
//        }
//
//        return 1;
//    }
//
//    /**
//     * Count duplicate subterms.
//     */
//    public int countDuplicates(Term term) {
//        Map<String, Integer> counts = new HashMap<>();
//        countDuplicatesHelper(term, counts, new HashSet<>());
//
//        int duplicates = 0;
//        for (int count : counts.values()) {
//            if (count > 1) {
//                duplicates += count - 1;
//            }
//        }
//        return duplicates;
//    }
//
//    private void countDuplicatesHelper(Term term, Map<String, Integer> counts, Set<String> boundVars) {
//        if (term == null) return;
//
//        String key = generateTermKey(term, boundVars);
//        counts.put(key, counts.getOrDefault(key, 0) + 1);
//
//        if (term instanceof Application) {
//            Application app = (Application) term;
//            countDuplicatesHelper(app.getFunction(), counts, boundVars);
//            countDuplicatesHelper(app.getArgument(), counts, boundVars);
//        } else if (term instanceof Abstraction) {
//            Abstraction abs = (Abstraction) term;
//            Set<String> newBoundVars = new HashSet<>(boundVars);
//            newBoundVars.add(abs.getParameter());
//            countDuplicatesHelper(abs.getBody(), counts, newBoundVars);
//        } else if (term instanceof Addition) {
//            Addition add = (Addition) term;
//            countDuplicatesHelper(add.getLeft(), counts, boundVars);
//            countDuplicatesHelper(add.getRight(), counts, boundVars);
//        } else if (term instanceof Multiplication) {
//            Multiplication mul = (Multiplication) term;
//            countDuplicatesHelper(mul.getLeft(), counts, boundVars);
//            countDuplicatesHelper(mul.getRight(), counts, boundVars);
//        }
//    }
//}