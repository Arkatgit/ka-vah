package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.DefinedValue;
import ca.brock.cs.lambda.types.FunctionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desugars algebraic data constructors and match expressions into ordinary
 * lambda/application terms using Scott encodings.
 *
 * This keeps source-level ADTs for parsing and type checking, but ensures that
 * the intermediate/combinator/x86 stages only see the ordinary core language.
 */
public final class ScottEncoding {
    private static final AtomicInteger freshCounter = new AtomicInteger();

    private ScottEncoding() {}

    public static Map<String, DefinedValue> desugarProgram(Map<String, DefinedValue> symbolMap) {
        Map<String, DefinedValue> result = new HashMap<>();
        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            DefinedValue value = entry.getValue();
            if (value instanceof FunctionDefinition) {
                FunctionDefinition functionDefinition = (FunctionDefinition) value;
                result.put(
                    entry.getKey(),
                    new FunctionDefinition(
                        functionDefinition.getName(),
                        functionDefinition.getType(),
                        desugar(functionDefinition.getTerm())
                    )
                );
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    public static Term desugar(Term term) {
        if (term == null) {
            return null;
        }

        if (term instanceof IntegerLiteral ||
            term instanceof BooleanLiteral ||
            term instanceof Variable ||
            term instanceof Constant) {
            return term;
        }

        if (term instanceof Constructor) {
            Constructor c = (Constructor) term;
            return encodeConstructor(c.getName());
        }

        if (term instanceof Application) {
            Application app = (Application) term;
            return new Application(desugar(app.getFunction()), desugar(app.getArgument()));
        }

        if (term instanceof Abstraction) {
            Abstraction abs = (Abstraction) term;
            return new Abstraction(abs.getParameter(), desugar(abs.getBody()));
        }

        if (term instanceof Recursion) {
            Recursion rec = (Recursion) term;
            return new Recursion(rec.getName(), desugar(rec.getBody()));
        }

        if (term instanceof Match) {
            Match match = (Match) term;
            return encodeMatch(match);
        }

        if (term instanceof Conditional) {
            Conditional c = (Conditional) term;
            return new Conditional(
                desugar(c.getCondition()),
                desugar(c.getTrueBranch()),
                desugar(c.getFalseBranch())
            );
        }

        if (term instanceof Addition) {
            Addition a = (Addition) term;
            return new Addition(desugar(a.getLeft()), desugar(a.getRight()));
        }

        if (term instanceof Subtraction) {
            Subtraction s = (Subtraction) term;
            return new Subtraction(desugar(s.getLeft()), desugar(s.getRight()));
        }

        if (term instanceof Multiplication) {
            Multiplication m = (Multiplication) term;
            return new Multiplication(desugar(m.getLeft()), desugar(m.getRight()));
        }

        if (term instanceof Division) {
            Division d = (Division) term;
            return new Division(desugar(d.getLeft()), desugar(d.getRight()));
        }

        if (term instanceof Equal) {
            Equal e = (Equal) term;
            return new Equal(desugar(e.getLeft()), desugar(e.getRight()));
        }

        if (term instanceof LEqual) {
            LEqual le = (LEqual) term;
            return new LEqual(desugar(le.getLeft()), desugar(le.getRight()));
        }

        if (term instanceof And) {
            And a = (And) term;
            return new And(desugar(a.getLeft()), desugar(a.getRight()));
        }

        if (term instanceof Or) {
            Or o = (Or) term;
            return new Or(desugar(o.getLeft()), desugar(o.getRight()));
        }

        if (term instanceof Not) {
            Not n = (Not) term;
            return new Not(desugar(n.getOperand()));
        }

        throw new IllegalArgumentException(
            "Scott desugaring not implemented for " + term.getClass().getSimpleName()
        );
    }
    public static Term encodeConstructor(String constructorName) {
        ConstructorRegistry.ConstructorInfo info = ConstructorRegistry.getConstructorInfo(constructorName);

        List<String> fieldVars = freshNames("scott_field", info.getArity());
        List<String> caseVars = freshNames("scott_case", info.getConstructorCount());

        Term body = new Variable(caseVars.get(info.getIndex()));
        for (String fieldVar : fieldVars) {
            body = new Application(body, new Variable(fieldVar));
        }

        for (int i = caseVars.size() - 1; i >= 0; i--) {
            body = new Abstraction(caseVars.get(i), body);
        }
        for (int i = fieldVars.size() - 1; i >= 0; i--) {
            body = new Abstraction(fieldVars.get(i), body);
        }

        return body;
    }

    public static Term encodeMatch(Match match) {
        Term scrutinee = desugar(match.getInputTerm());
        String typeName = inferMatchedTypeName(match);
        List<ConstructorRegistry.ConstructorInfo> infos = ConstructorRegistry.getConstructorsForType(typeName);

        if (infos.isEmpty()) {
            throw new IllegalStateException("No constructor metadata registered for type " + typeName);
        }

        Term result = scrutinee;
        for (ConstructorRegistry.ConstructorInfo info : infos) {
            Match.Case matchingCase = findCaseForConstructor(match.getCases(), info.getConstructorName());
            if (matchingCase == null) {
                throw new IllegalStateException("Missing Scott-encoded branch for constructor " + info.getConstructorName());
            }
            result = new Application(result, encodeBranch(matchingCase, info));
        }
        return result;
    }

    private static Term encodeBranch(Match.Case matchCase, ConstructorRegistry.ConstructorInfo info) {
        List<String> params = new ArrayList<>();
        collectPatternVariablesLeftToRight(matchCase.getPattern(), params);

        if (params.size() != info.getArity()) {
            throw new IllegalStateException(
                "Branch arity mismatch for constructor " + info.getConstructorName() +
                    ": expected " + info.getArity() + " binder(s), found " + params.size()
            );
        }

        Term body = desugar(matchCase.getResult());
        for (int i = params.size() - 1; i >= 0; i--) {
            body = new Abstraction(params.get(i), body);
        }
        return body;
    }

    private static Match.Case findCaseForConstructor(List<Match.Case> cases, String constructorName) {
        for (Match.Case matchCase : cases) {
            Pattern pattern = matchCase.getPattern();

            if (pattern instanceof ConstructorPattern) {
                ConstructorPattern constructorPattern = (ConstructorPattern) pattern;

                if (constructorPattern.getName().equals(constructorName)) {
                    return matchCase;
                }
            }
        }
        return null;
    }

    private static void collectPatternVariablesLeftToRight(Pattern pattern, List<String> out) {
        if (pattern instanceof VariablePattern) {
            VariablePattern vp = (VariablePattern) pattern;
            out.add(vp.getName());
            return;
        }

        if (pattern instanceof ConstructorPattern) {
            ConstructorPattern cp = (ConstructorPattern) pattern;
            for (Pattern child : cp.getPatterns()) {
                collectPatternVariablesLeftToRight(child, out);
            }
            return;
        }

        if (pattern instanceof ConstantPattern) {
            return;
        }

        throw new IllegalArgumentException(
            "Unsupported pattern in Scott encoding: " + pattern.getClass().getSimpleName()
        );
    }

    private static String inferMatchedTypeName(Match match) {
        for (Match.Case matchCase : match.getCases()) {
            Pattern pattern = matchCase.getPattern();

            if (pattern instanceof ConstructorPattern) {
                ConstructorPattern cp = (ConstructorPattern) pattern;
                return ConstructorRegistry
                    .getConstructorInfo(cp.getName())
                    .getTypeName();
            }
        }

        throw new IllegalStateException(
            "Cannot infer matched ADT from match expression without constructor patterns"
        );
    }

    private static List<String> freshNames(String prefix, int count) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            names.add(prefix + "_" + freshCounter.incrementAndGet());
        }
        return names;
    }
}