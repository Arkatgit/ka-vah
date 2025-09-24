package ca.brock.cs.lambda.types;

import ca.brock.cs.lambda.logging.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Unifier {
    private Map<TVar, Type> environment = new HashMap<>();
    private Map<String, DefinedValue> symbolMap = new HashMap<>();
    private int nextId = 0;

    public Map<TVar, Type> getEnv() {
        return environment;
    }

    public  Map<String, DefinedValue> getSymbolMap() {
        return symbolMap;
    }

    /**
     * Sets the internal environment of the unifier.
     * @param newEnv The new environment map to use.
     */
    public void setEnv(Map<TVar, Type> newEnv) {
        this.environment = newEnv;
    }

    public void setSymbolMap( Map<String, DefinedValue> newSymbolMap){
        this.symbolMap = newSymbolMap;
    }

    /**
     * Returns a new, empty substitution map.
     * @return A new HashMap.
     */
    public Map<String, Type> newSubstitution() {
        return new HashMap<>();
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

    // Apply current substitution to both types first
    type1 = applySubstitution(type1, sub);
    type2 = applySubstitution(type2, sub);

    AppLogger.info("  unifyInternal: After applying current substitution: t1 = " + type1 + ", t2 = " + type2);

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
        return unifyInternal(
            applySubstitution(ft1.getOutput(), sub1),
            applySubstitution(ft2.getOutput(), sub1),
            sub1
        );
    } else if (type1 instanceof AlgebraicDataType && type2 instanceof AlgebraicDataType) {
        AlgebraicDataType adt1 = (AlgebraicDataType) type1;
        AlgebraicDataType adt2 = (AlgebraicDataType) type2;

        // Check if they are the same algebraic data type
        if (!adt1.getName().equals(adt2.getName())) {
            AppLogger.info("  unifyInternal: Different ADT names: " + adt1.getName() + " vs " + adt2.getName());
            return null;
        }

        // Check if they have the same number of parameters
        if (adt1.getParameters().size() != adt2.getParameters().size()) {
            AppLogger.info("  unifyInternal: Different number of ADT parameters: " +
                adt1.getParameters().size() + " vs " + adt2.getParameters().size());
            return null;
        }

        // Unify each parameter recursively
        Map<TVar, Type> currentSub = sub;
        for (int i = 0; i < adt1.getParameters().size(); i++) {
            Type param1 = adt1.getParameters().get(i);
            Type param2 = adt2.getParameters().get(i);

            currentSub = unifyInternal(param1, param2, currentSub);
            if (currentSub == null) {
                AppLogger.info("  unifyInternal: ADT parameter unification failed at index " + i);
                return null;
            }
        }
        AppLogger.info("  unifyInternal: ADT unification successful: " + currentSub);
        return currentSub;
    } else if (type1 instanceof ca.brock.cs.lambda.types.Constant &&
        type2 instanceof ca.brock.cs.lambda.types.Constant) {
        // Constant types (Int, Bool, etc.)
        ca.brock.cs.lambda.types.Constant const1 = (ca.brock.cs.lambda.types.Constant) type1;
        ca.brock.cs.lambda.types.Constant const2 = (ca.brock.cs.lambda.types.Constant) type2;

        if (const1.getName().equals(const2.getName())) {
            return sub;
        } else {
            AppLogger.info("  unifyInternal: Constant type mismatch: " + const1.getName() + " vs " + const2.getName());
            return null;
        }
    } else if (type1 instanceof ProdType && type2 instanceof ProdType) {
        // Product types
        ProdType prod1 = (ProdType) type1;
        ProdType prod2 = (ProdType) type2;

        Map<TVar, Type> sub1 = unifyInternal(prod1.getLeft(), prod2.getLeft(), sub);
        if (sub1 == null) {
            return null;
        }
        return unifyInternal(
            applySubstitution(prod1.getRight(), sub1),
            applySubstitution(prod2.getRight(), sub1),
            sub1
        );
    } else {
        // Type mismatch - different type constructors
        AppLogger.info("  unifyInternal: Type constructor mismatch: " +
            type1.getClass().getSimpleName() + " vs " + type2.getClass().getSimpleName());
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
    AppLogger.info("DEBUG: occursCheck: " + var + " in " + type);

    // Apply current substitution to the type
    Type resolvedType = applySubstitution(type, sub);
    AppLogger.info("DEBUG: After substitution: " + resolvedType);

    if (resolvedType instanceof TVar) {
        boolean result = var.equals(resolvedType);
        AppLogger.info("DEBUG: occursCheck TVar result: " + result);
        return result;
    } else if (resolvedType instanceof FType) {
        FType ft = (FType) resolvedType;
        boolean inInput = occursCheck(var, ft.getInput(), sub);
        boolean inOutput = occursCheck(var, ft.getOutput(), sub);
        boolean result = inInput || inOutput;
        AppLogger.info("DEBUG: occursCheck FType result: " + result);
        return result;
    } else if (resolvedType instanceof AlgebraicDataType) {
        AlgebraicDataType adt = (AlgebraicDataType) resolvedType;
        boolean result = false;
        for (Type param : adt.getParameters()) {
            if (occursCheck(var, param, sub)) {
                result = true;
                break;
            }
        }
        AppLogger.info("DEBUG: occursCheck ADT result: " + result);
        return result;
    } else if (resolvedType instanceof TApp) {
        TApp tapp = (TApp) resolvedType;
        boolean inTarget = occursCheck(var, tapp.getTarget(), sub);
        boolean inArgument = occursCheck(var, tapp.getArgument(), sub);
        boolean result = inTarget || inArgument;
        AppLogger.info("DEBUG: occursCheck TApp result: " + result);
        return result;
    }

    AppLogger.info("DEBUG: occursCheck: no occurrence found");
    return false;
}

//    public Type applySubstitution(Type type, Map<TVar, Type> sub) {
//        if (type instanceof TVar) {
//            return sub.getOrDefault(type, type);
//        } else if (type instanceof FType) {
//            FType ft = (FType) type;
//            return new FType(applySubstitution(ft.getInput(), sub), applySubstitution(ft.getOutput(), sub));
//        }
//        return type;
//    }
public Type applySubstitution(Type type, Map<TVar, Type> sub) {
    if (type instanceof TVar) {
        Type substituted = sub.get(type);
        if (substituted != null) {
            // Recursively apply substitution to handle nested substitutions
            return applySubstitution(substituted, sub);
        }
        return type;
    } else if (type instanceof FType) {
        FType ft = (FType) type;
        return new FType(applySubstitution(ft.getInput(), sub), applySubstitution(ft.getOutput(), sub));
    } else if (type instanceof AlgebraicDataType) {
        AlgebraicDataType adt = (AlgebraicDataType) type;
        List<Type> newParameters = new ArrayList<>();
        for (Type param : adt.getParameters()) {
            newParameters.add(applySubstitution(param, sub));
        }
        // Return a new ADT with substituted parameters but same constructors
        return new AlgebraicDataType(adt.getName(), newParameters, adt.getConstructors());
    } else if (type instanceof ProdType) {
        ProdType prod = (ProdType) type;
        return new ProdType(applySubstitution(prod.getLeft(), sub), applySubstitution(prod.getRight(), sub));
    } else if (type instanceof ca.brock.cs.lambda.types.Constant) {
        // Constants don't contain type variables, so no substitution needed
        return type;
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
        //return new TVar("a" + nextId++);
        TVar freshVar = new TVar("a" + nextId++);
        // Add constraint to prevent fresh variables from binding to primitives
      //  constraints.put(freshVar, new AlgebraicDataType("any-adt", List.of(TVar.fresh()), null));
        return freshVar;
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