package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.combinators.Combinator;
import ca.brock.cs.lambda.combinators.CombinatorConstant;
import ca.brock.cs.lambda.intermediate.IntermediateTerm;
import ca.brock.cs.lambda.intermediate.IntermediateConstructor;
import ca.brock.cs.lambda.logging.AppLogger;
import ca.brock.cs.lambda.types.AlgebraicDataType;
import ca.brock.cs.lambda.types.DefinedValue;
import ca.brock.cs.lambda.types.FType;
import ca.brock.cs.lambda.types.TVar;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.TypeError;
import ca.brock.cs.lambda.types.Unifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a constructor of a user-defined data type (e.g., 'emptylist', 'cons').
 * This is a Term subclass, similar to a Constant or Variable.
 */
public class Constructor extends Term implements DefinedValue {
    private final String name;
    private final Type type;

    public Constructor(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType(){
        return type;
    }

    @Override
    public Term getTerm() {
        return null;
    }

    @Override
    public String toStringPrec(int prec) {
        return name;
    }

 //   @Override
//    protected Type computeType(Map<String, Type> env, Unifier unifier) {
//        Type constructorType = env.get(name);
//        if (constructorType == null) {
//            throw new TypeError("Constructor '" + name + "' not found.");
//        }
//
//        System.out.println("Constructor " + name + " original type: " + constructorType);
//        Type instantiatedType = instantiateWithFreshVariables(constructorType, unifier);
//        System.out.println("Constructor " + name + " instantiated type: " + instantiatedType);
//
//        return instantiatedType;
//    }
//    private Type instantiateWithFreshVariables(Type type, Unifier unifier) {
//        if (type instanceof FType) {
//            FType ft = (FType) type;
//            return new FType(
//                instantiateWithFreshVariables(ft.getInput(), unifier),
//                instantiateWithFreshVariables(ft.getOutput(), unifier)
//            );
//        } else if (type instanceof AlgebraicDataType) {
//            AlgebraicDataType adt = (AlgebraicDataType) type;
//            List<Type> freshParams = new ArrayList<>();
//            for (Type param : adt.getParameters()) {
//                freshParams.add(instantiateWithFreshVariables(param, unifier));
//            }
//            return new AlgebraicDataType(adt.getName(), freshParams, adt.getConstructors());
//        } else if (type instanceof TVar) {
//            // Replace type variables with fresh ones, but preserve the same variable name
//            // This helps with debugging and error messages
//            TVar original = (TVar) type;
//            TVar fresh = TVar.fresh();
//            System.out.println("  Replacing " + original.getName() + " with " + fresh.getName());
//            return fresh;
//        }
//        return type;
//    }


//    protected Type computeType(Map<String, Type> env, Unifier unifier) {
//        // This is the core fix. We must return a fresh instance of the type
//        // to avoid type variable capture.
//        return type.deepCloneAndFresh(unifier);
//    }
//    @Override
//    protected Type computeType(Map<String, Type> env, Unifier unifier) {
//        if (type instanceof AlgebraicDataType) {
//            AlgebraicDataType adt = (AlgebraicDataType) type;
//            // For recursive types, we need to handle the type parameters properly
//            List<Type> freshParams = new ArrayList<>();
//            for (Type param : adt.getParameters()) {
//                freshParams.add(param.deepCloneAndFresh(unifier));
//            }
//            return new AlgebraicDataType(adt.getName(), freshParams, adt.getConstructors());
//        }
//        return type.deepCloneAndFresh(unifier);
//    }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        Type constructorType = env.get(name);
        if (constructorType == null) {
            throw new TypeError("Constructor '" + name + "' not found.");
        }

        // SPECIAL HANDLING FOR CONSTRUCTORS: Preserve polymorphic relationships
        Map<TVar, TVar> freshMap = new HashMap<>();
        return freshenWithConsistentMapping(constructorType, freshMap);
    }

    private Type freshenWithConsistentMapping(Type type, Map<TVar, TVar> freshMap) {
        if (type instanceof TVar) {
            TVar original = (TVar) type;
            if (!freshMap.containsKey(original)) {
                freshMap.put(original, TVar.fresh());
            }
            return freshMap.get(original);
        } else if (type instanceof FType) {
            FType ftype = (FType) type;
            return new FType(
                freshenWithConsistentMapping(ftype.getInput(), freshMap),
                freshenWithConsistentMapping(ftype.getOutput(), freshMap)
            );
        } else if (type instanceof AlgebraicDataType) {
            AlgebraicDataType adt = (AlgebraicDataType) type;
            List<Type> freshParams = new ArrayList<>();
            for (Type param : adt.getParameters()) {
                freshParams.add(freshenWithConsistentMapping(param, freshMap));
            }
            return new AlgebraicDataType(adt.getName(), freshParams, adt.getConstructors());
        }
        return type;
    }
    @Override
    public Term eval(Map<String, Term> env) {
        // Constructors evaluate to themselves in the absence of a more complex evaluation model.
        return this;
    }

    @Override
    public Term substitute(String varName, Term value) {
        // Constructors don't contain variables to substitute
        return this;
    }

    @Override
    public Set<String> getFreeVariables() {
        return Collections.emptySet(); // Constructors have no free variables
    }

    @Override
    public Combinator translate() {
        // Translate a constructor to a CombinatorConstant
        return new CombinatorConstant(name);
    }

    @Override
    public IntermediateTerm toIntermediateTerm() {
        // A constructor term is converted into an IntermediateConstructor term.
        return new IntermediateConstructor(name);
    }
}
