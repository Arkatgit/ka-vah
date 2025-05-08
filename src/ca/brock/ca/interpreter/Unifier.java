package ca.brock.ca.interpreter;

import ca.brock.cs.lambda.AppLogger;

import java.util.HashMap;
import java.util.Map;

public class Unifier {
    private Map<TVar, Type> environment = new HashMap<>();
    private int nextId = 0;

    public Map<TVar, Type> getEnv() {
        return environment;
    }

    public Map<TVar, Type> unify(Type type1, Type type2) { // Changed return type
        Map<TVar, Type> currentSub = new HashMap<>(environment);
        Map<TVar, Type> resultSub = unifyInternal(applySubstitution(type1, currentSub), applySubstitution(type2, currentSub), currentSub);

        if (resultSub != null) {
            environment = resultSub;
            applyTransitiveClosure();
            return environment; // Return the Map<TVar, Type> directly
        }
        return null;
    }

    private Map<TVar, Type> unifyInternal(Type type1, Type type2, Map<TVar, Type> sub) {
        AppLogger.info("--- Unifying: " + type1 + " and " + type2);
        AppLogger.info("  unifyInternal: After applying substitution: t1 = " + type1 + ", t2 = " + type2);

        if (type1.equals(type2)) {
            return sub;
        } else if (type1 instanceof TVar) {
            return unifyVariable((TVar) type1, type2, sub);
        } else if (type2 instanceof TVar) {
            return unifyVariable((TVar) type2, type1, sub);
        } else if (type1 instanceof FType && type2 instanceof FType) {
            FType ft1 = (FType) type1;
            FType ft2 = (FType) type2;
            Map<TVar, Type> sub1 = unifyInternal(ft1.getInput(), ft2.getInput(), sub);
            if (sub1 == null) {
                return null;
            }
            return unifyInternal(ft1.getOutput(), ft2.getOutput(), sub1);
        } else {
            return null;
        }
    }

    private Map<TVar, Type> unifyVariable(TVar var, Type type, Map<TVar, Type> sub) {
        AppLogger.info("  unifyVariable: Unifying variable " + var + " with type " + type);
        if (sub.containsKey(var)) {
            return unifyInternal(sub.get(var), type, sub);
        } else if (occursCheck(var, type, sub)) {
            return null; // Occurs check failed
        } else {
            Map<TVar, Type> newSub = new HashMap<>(sub);
            newSub.put(var, type);
            AppLogger.info("    unifyVariable: Binding " + var + " to " + type + " in environment: " + newSub);
            return newSub;
        }
    }

    private boolean occursCheck(TVar var, Type type, Map<TVar, Type> sub) {
        Type resolvedType = applySubstitution(type, sub);
        if (resolvedType instanceof TVar) {
            return var.equals(resolvedType);
        } else if (resolvedType instanceof FType) {
            FType ft = (FType) resolvedType;
            return occursCheck(var, ft.getInput(), sub) || occursCheck(var, ft.getOutput(), sub);
        }
        return false;
    }

    public Type applySubstitution(Type type, Map<TVar, Type> sub) {
        if (type instanceof TVar) {
            return sub.getOrDefault(type, type);
        } else if (type instanceof FType) {
            FType ft = (FType) type;
            return new FType(applySubstitution(ft.getInput(), sub), applySubstitution(ft.getOutput(), sub));
        }
        return type;
    }

    public Type deepApplySubstitution(Type type, Map<TVar, Type> substitution) {
        AppLogger.info("  deepApplySubstitution: Applying deep substitution on type: " + type + " with substitution map: " + substitution);
        if (type instanceof TVar) {
            Type sub = substitution.get(type);
            if (sub != null) {
                AppLogger.info("    deepApplySubstitution: Found substitution for " + type + " -> " + sub);
                return deepApplySubstitution(sub, substitution); // Recursively apply
            }
            AppLogger.info("    deepApplySubstitution: No substitution found for " + type);
            return type;
        }
        if (type instanceof FType) {
            FType ft = (FType) type;
            Type inputSubstituted = deepApplySubstitution(ft.getInput(), substitution);
            Type outputSubstituted = deepApplySubstitution(ft.getOutput(), substitution);
            Type result = new FType(inputSubstituted, outputSubstituted);
            AppLogger.info("    deepApplySubstitution: Applied to FType, result: " + result);
            return result;
        }
        AppLogger.info("  deepApplySubstitution: No deep substitution needed for type: " + type);
        return type;
    }

    private Map<String, Type> convertToStringKeyMap(Map<TVar, Type> typeVarMap) {
        Map<String, Type> stringMap = new HashMap<>();
        for (Map.Entry<TVar, Type> entry : typeVarMap.entrySet()) {
            stringMap.put(entry.getKey().getName(), entry.getValue());
        }
        return stringMap;
    }

    public TVar fresh() {
        return new TVar("a" + nextId++);
    }

    private void applyTransitiveClosure() {
        boolean changed;
        do {
            changed = false;
            Map<TVar, Type> newEnvironment = new HashMap<>(environment);
            for (Map.Entry<TVar, Type> entry : environment.entrySet()) {
                TVar tv = entry.getKey();
                Type type = entry.getValue();
                Type resolvedType = deepApplySubstitution(type, environment);
                if (!resolvedType.equals(type)) {
                    newEnvironment.put(tv, resolvedType);
                    changed = true;
                }
            }
            environment = newEnvironment;
        } while (changed);
    }
}