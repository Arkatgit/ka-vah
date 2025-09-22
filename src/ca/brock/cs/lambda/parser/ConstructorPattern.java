package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.AlgebraicDataType;
import ca.brock.cs.lambda.types.FType;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a constructor pattern, which matches a constructor application.
 */
public class ConstructorPattern extends Pattern {
    private final String name;
    private final List<Pattern> patterns;

    public ConstructorPattern(String name, List<Pattern> patterns) {
        this.name = name;
        this.patterns = patterns;
    }

    public String getName() {
        return name;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    @Override
    public Map<String, Term> match(Term term) {
        if (term instanceof Application) {
            Application app = (Application) term;
            Term func = app.getFunction();
            Term arg = app.getArgument();

            if (func instanceof Constructor) {
                Constructor constr = (Constructor) func;
                if (constr.getName().equals(name) && patterns.size() == 1) {
                    return patterns.get(0).match(arg);
                }
            } else if (func instanceof Application) {
                return matchComplexApplication(app, new HashMap<>());
            }
        } else if (term instanceof Constructor) {
            Constructor constr = (Constructor) term;
            if (constr.getName().equals(name) && patterns.isEmpty()) {
                return new HashMap<>();
            }
        }
        return null;
    }

    private Map<String, Term> matchComplexApplication(Application app, Map<String, Term> bindings) {
        Term func = app.getFunction();
        Term arg = app.getArgument();

        if (func instanceof Application) {
            matchComplexApplication((Application) func, bindings);
        }

        int argIndex = getPatterns().size() - (countApplications(app) - 1) - 1;
        if (argIndex >= 0) {
            Map<String, Term> newBindings = getPatterns().get(argIndex).match(arg);
            if (newBindings == null) {
                return null;
            }
            bindings.putAll(newBindings);
        }

        if (func instanceof Constructor) {
            Constructor constr = (Constructor) func;
            if (constr.getName().equals(name) && countApplications(app) == getPatterns().size()) {
                return bindings;
            }
        }
        return bindings;
    }

    private int countApplications(Term term) {
        if (term instanceof Application) {
            return 1 + countApplications(((Application) term).getFunction());
        } else {
            return 0;
        }
    }


    @Override
    public Set<String> getBoundVariables() {
        Set<String> boundVars = new HashSet<>();
        for (Pattern p : patterns) {
            boundVars.addAll(p.getBoundVariables());
        }
        return boundVars;
    }

    @Override
//    public Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type constructorType = env.get(name);
//        if (constructorType == null) {
//            throw new TypeError("Constructor '" + name + "' not found in environment.");
//        }
//
//        // Get a fresh instance to avoid variable capture
//        Type freshConstructorType = constructorType.deepCloneAndFresh(unifier);
//
//        // Rest of the method remains the same...
//        List<Type> patternTypes = new ArrayList<>();
//        for (Pattern p : patterns) {
//            patternTypes.add(p.computeType(env, unifier));
//        }
//
//        Type currentType = freshConstructorType;
//        for (Type argType : patternTypes) {
//            if (!(currentType instanceof FType)) {
//                throw new TypeError("Expected function type for constructor pattern arguments.");
//            }
//
//            FType ft = (FType) currentType;
//            Map<TVar, Type> sub = unifier.unify(ft.getInput(), argType);
//            if (sub == null) {
//                throw new TypeError("Type unification failed for constructor pattern.");
//            }
//            currentType = unifier.applySubstitution(ft.getOutput(), sub);
//        }
//
//        return currentType;
//    }
//    public Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type constructorType = env.get(name);
//        if (constructorType == null) {
//            throw new TypeError("Constructor '" + name + "' not found.");
//        }
//
//        // Create fresh type for this specific pattern usage
//        Type freshConstructorType = constructorType.deepCloneAndFresh(unifier);
//        Type currentType = freshConstructorType;
//
//        for (int i = 0; i < patterns.size(); i++) {
//            Pattern pattern = patterns.get(i);
//
//            if (!(currentType instanceof FType)) {
//                throw new TypeError("Expected function type for constructor argument");
//            }
//
//            FType ftype = (FType) currentType;
//            Type expectedArgType = ftype.getInput();
//
//            // Compute pattern type
//            Type patternType = pattern.computeType(env, unifier);
//
//            // Unify expected vs actual argument type
//            Map<TVar, Type> sub = unifier.unify(expectedArgType, patternType);
//            if (sub == null) {
//                throw new TypeError("Constructor argument " + i + " type mismatch.");
//            }
//
//            // Apply substitution and continue
//            currentType = unifier.applySubstitution(ftype.getOutput(), sub);
//            unifier.getEnv().putAll(sub);
//        }
//
//        return currentType;
//    }

//    public Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type constructorType = env.get(name);
//        if (constructorType == null) {
//            throw new TypeError("Constructor '" + name + "' not found.");
//        }
//
//        Type freshConstructorType = constructorType.deepCloneAndFresh(unifier);
//        Type currentType = freshConstructorType;
//
//        // Process each pattern, unifying as we go
//        for (int i = 0; i < patterns.size(); i++) {
//            Pattern pattern = patterns.get(i);
//
//            if (!(currentType instanceof FType)) {
//                throw new TypeError("Expected function type for constructor argument");
//            }
//
//            FType ftype = (FType) currentType;
//            Type expectedArgType = ftype.getInput();
//
//            // Compute the pattern type - this will bind variables to the environment
//            Type patternType = pattern.computeType(env, unifier);
//
//            // Unify the expected argument type with the actual pattern type
//            Map<TVar, Type> sub = unifier.unify(expectedArgType, patternType);
//            if (sub == null) {
//                throw new TypeError("Constructor argument " + i + " type mismatch. " +
//                    "Expected: " + expectedArgType + " Got: " + patternType);
//            }
//
//            // Apply the substitution to continue
//            currentType = unifier.applySubstitution(ftype.getOutput(), sub);
//        }
//
//        return currentType;
//    }
//
//    public Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type constructorType = env.get(name);
//        if (constructorType == null) {
//            throw new TypeError("Constructor '" + name + "' not found in environment.");
//        }
//
//        // Get fresh instance
//        Type freshConstructorType = constructorType.deepCloneAndFresh(unifier);
//
//        // Compute pattern types first
//        List<Type> patternTypes = new ArrayList<>();
//        for (Pattern p : patterns) {
//            patternTypes.add(p.computeType(env, unifier));
//        }
//
//        // Now process the constructor type step by step
//        Type currentType = freshConstructorType;
//
//        for (int i = 0; i < patterns.size(); i++) {
//            if (!(currentType instanceof FType)) {
//                throw new TypeError("Expected function type for constructor argument");
//            }
//
//            FType ftype = (FType) currentType;
//            Type expectedArgType = ftype.getInput();
//            Type actualArgType = patternTypes.get(i);
//
//            // CRITICAL: Apply current substitutions before unification
//            expectedArgType = unifier.applySubstitution(expectedArgType, unifier.getEnv());
//            actualArgType = unifier.applySubstitution(actualArgType, unifier.getEnv());
//
//            Map<TVar, Type> sub = unifier.unify(expectedArgType, actualArgType);
//            if (sub == null) {
//                throw new TypeError("Constructor argument " + i + " type mismatch.");
//            }
//
//            // Apply the substitution to continue
//            currentType = unifier.applySubstitution(ftype.getOutput(), sub);
//        }
//
//        return currentType;
//    }

//    public Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type constructorType = env.get(name);
//        if (constructorType == null) {
//            throw new TypeError("Constructor '" + name + "' not found in environment.");
//        }
//
//        // Get properly freshened constructor type
//        Type freshConstructorType = constructorType.deepCloneAndFresh(unifier);
//
//        // Compute pattern types
//        List<Type> patternTypes = new ArrayList<>();
//        for (Pattern p : patterns) {
//            patternTypes.add(p.computeType(env, unifier));
//        }
//
//        // Step through the constructor type and unify with pattern types
//        Type currentType = freshConstructorType;
//
//        for (int i = 0; i < patterns.size(); i++) {
//            if (!(currentType instanceof FType)) {
//                throw new TypeError("Expected function type for constructor argument");
//            }
//
//            FType ftype = (FType) currentType;
//            Type expectedArgType = ftype.getInput();
//            Type actualArgType = patternTypes.get(i);
//
//            // Apply current substitutions
//            expectedArgType = unifier.applySubstitution(expectedArgType, unifier.getEnv());
//            actualArgType = unifier.applySubstitution(actualArgType, unifier.getEnv());
//
//            Map<TVar, Type> sub = unifier.unify(expectedArgType, actualArgType);
//            if (sub == null) {
//                throw new TypeError("Constructor argument " + i + " type mismatch.");
//            }
//
//            // Apply substitution and continue to next argument
//            currentType = unifier.applySubstitution(ftype.getOutput(), sub);
//        }
//
//        return currentType;
//    }
//    public Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type constructorType = env.get(name);
//        if (constructorType == null) {
//            throw new TypeError("Constructor '" + name + "' not found in environment.");
//        }
//
//        // Get fresh constructor type with preserved relationships
//        Type freshConstructorType = freshenWithConsistentMapping(constructorType);
//
//        // Compute pattern types
//        List<Type> patternTypes = new ArrayList<>();
//        for (Pattern p : patterns) {
//            patternTypes.add(p.computeType(env, unifier));
//        }
//
//        // Step through constructor type and unify
//        Type currentType = freshConstructorType;
//        List<Type> constructorArgTypes = new ArrayList<>();
//
//        for (int i = 0; i < patterns.size(); i++) {
//            if (!(currentType instanceof FType)) {
//                throw new TypeError("Expected function type for constructor argument");
//            }
//
//            FType ftype = (FType) currentType;
//            constructorArgTypes.add(ftype.getInput());
//
//            Type expectedArgType = ftype.getInput();
//            Type actualArgType = patternTypes.get(i);
//
//            Map<TVar, Type> sub = unifier.unify(expectedArgType, actualArgType);
//            if (sub == null) {
//                throw new TypeError("Constructor argument " + i + " type mismatch.");
//            }
//
//            currentType = ftype.getOutput();
//        }
//
//        // CRITICAL: After unifying all arguments, ensure constructor type parameters are consistent
//        ensureConstructorParameterConsistency(constructorArgTypes, unifier);
//
//        return currentType;
//    }
//
//    private void ensureConstructorParameterConsistency(List<Type> constructorArgTypes, Unifier unifier) {
//        if (constructorArgTypes.size() < 2) return;
//
//        // For constructors like C a → D a → E a, ensure all 'a' parameters are the same
//        Type firstParam = constructorArgTypes.get(0);
//        Set<TVar> firstParamVars = firstParam.getFreeTypeVariables();
//
//        for (int i = 1; i < constructorArgTypes.size(); i++) {
//            Type currentParam = constructorArgTypes.get(i);
//            Set<TVar> currentParamVars = currentParam.getFreeTypeVariables();
//
//            // If both parameters have type variables, try to unify them
//            if (!firstParamVars.isEmpty() && !currentParamVars.isEmpty()) {
//                for (TVar firstVar : firstParamVars) {
//                    for (TVar currentVar : currentParamVars) {
//                        Map<TVar, Type> sub = unifier.unify(firstVar, currentVar);
//                        if (sub != null) {
//                            unifier.getEnv().putAll(sub);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private Type freshenWithConsistentMapping(Type type) {
//        Map<TVar, TVar> freshMap = new HashMap<>();
//        return applyConsistentFreshening(type, freshMap);
//    }
//
//    private Type applyConsistentFreshening(Type type, Map<TVar, TVar> freshMap) {
//        if (type instanceof TVar) {
//            TVar original = (TVar) type;
//            if (!freshMap.containsKey(original)) {
//                freshMap.put(original, TVar.fresh());
//            }
//            return freshMap.get(original);
//        } else if (type instanceof FType) {
//            FType ftype = (FType) type;
//            return new FType(
//                applyConsistentFreshening(ftype.getInput(), freshMap),
//                applyConsistentFreshening(ftype.getOutput(), freshMap)
//            );
//        } else if (type instanceof AlgebraicDataType) {
//            AlgebraicDataType adt = (AlgebraicDataType) type;
//            List<Type> freshParams = new ArrayList<>();
//            for (Type param : adt.getParameters()) {
//                freshParams.add(applyConsistentFreshening(param, freshMap));
//            }
//            return new AlgebraicDataType(adt.getName(), freshParams, adt.getConstructors());
//        }
//        return type;
//    }
    public Type computeType(Map<String, Type> env, Unifier unifier) {
        Type constructorType = env.get(name);
        if (constructorType == null) {
            throw new TypeError("Constructor '" + name + "' not found in environment.");
        }

        // Get fresh constructor type with preserved relationships
        Type freshConstructorType = freshenWithConsistentMapping(constructorType);

        // Compute pattern types
        List<Type> patternTypes = new ArrayList<>();
        for (Pattern p : patterns) {
            patternTypes.add(p.computeType(env, unifier));
        }

        // Step through constructor type and unify with patterns
        Type currentType = freshConstructorType;

        for (int i = 0; i < patterns.size(); i++) {
            if (!(currentType instanceof FType)) {
                throw new TypeError("Expected function type for constructor argument");
            }

            FType ftype = (FType) currentType;
            Type expectedArgType = ftype.getInput();
            Type actualArgType = patternTypes.get(i);

            Map<TVar, Type> sub = unifier.unify(expectedArgType, actualArgType);
            if (sub == null) {
                throw new TypeError("Constructor argument " + i + " type mismatch.");
            }

            currentType = ftype.getOutput();
        }

        // CRITICAL: After processing all arguments, ensure polymorphic consistency
        ensurePolymorphicConsistency(freshConstructorType, patternTypes, unifier);

        return currentType;
    }

    private void ensurePolymorphicConsistency(Type constructorType, List<Type> patternTypes, Unifier unifier) {
        // Extract all type parameters from the original constructor type
        Set<TVar> originalParams = constructorType.getFreeTypeVariables();

        if (originalParams.size() <= 1) return; // No need for polymorphic consistency check

        // For each original type parameter, find all its occurrences in pattern types
        for (TVar originalParam : originalParams) {
            Set<TVar> correspondingVars = new HashSet<>();

            // Find all type variables that correspond to this original parameter
            for (Type patternType : patternTypes) {
                findCorrespondingVariables(patternType, originalParam, correspondingVars, unifier);
            }

            // Ensure all corresponding variables are unified
            if (correspondingVars.size() > 1) {
                TVar first = correspondingVars.iterator().next();
                for (TVar other : correspondingVars) {
                    if (!first.equals(other)) {
                        Map<TVar, Type> sub = unifier.unify(first, other);
                        if (sub != null) {
                            unifier.getEnv().putAll(sub);
                        }
                    }
                }
            }
        }
    }

    private void findCorrespondingVariables(Type type, TVar targetParam, Set<TVar> result, Unifier unifier) {
        if (type instanceof TVar) {
            // This might be a fresh variable corresponding to the original parameter
            result.add((TVar) type);
        } else if (type instanceof FType) {
            FType ftype = (FType) type;
            findCorrespondingVariables(ftype.getInput(), targetParam, result, unifier);
            findCorrespondingVariables(ftype.getOutput(), targetParam, result, unifier);
        } else if (type instanceof AlgebraicDataType) {
            AlgebraicDataType adt = (AlgebraicDataType) type;
            for (Type param : adt.getParameters()) {
                findCorrespondingVariables(param, targetParam, result, unifier);
            }
        }
    }

    private Type freshenWithConsistentMapping(Type type) {
        Map<TVar, TVar> freshMap = new HashMap<>();
        return applyConsistentFreshening(type, freshMap);
    }

    private Type applyConsistentFreshening(Type type, Map<TVar, TVar> freshMap) {
        if (type instanceof TVar) {
            TVar original = (TVar) type;
            if (!freshMap.containsKey(original)) {
                freshMap.put(original, TVar.fresh());
            }
            return freshMap.get(original);
        } else if (type instanceof FType) {
            FType ftype = (FType) type;
            return new FType(
                applyConsistentFreshening(ftype.getInput(), freshMap),
                applyConsistentFreshening(ftype.getOutput(), freshMap)
            );
        } else if (type instanceof AlgebraicDataType) {
            AlgebraicDataType adt = (AlgebraicDataType) type;
            List<Type> freshParams = new ArrayList<>();
            for (Type param : adt.getParameters()) {
                freshParams.add(applyConsistentFreshening(param, freshMap));
            }
            return new AlgebraicDataType(adt.getName(), freshParams, adt.getConstructors());
        }
        return type;
    }

    @Override
    public String toString() {
        if (patterns.isEmpty()) {
            return name;
        }
        String args = patterns.stream()
            .map(Pattern::toString)
            .collect(Collectors.joining(" "));
        return name + " " + args;
    }
}
