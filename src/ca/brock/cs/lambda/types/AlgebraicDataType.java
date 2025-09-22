package ca.brock.cs.lambda.types;

import ca.brock.cs.lambda.parser.Constructor;
import ca.brock.cs.lambda.parser.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a user-defined algebraic data type, like 'list a'.
 * This class also implements DefinedValue so it can be stored in the symbol table.
 */
public class AlgebraicDataType extends Type implements DefinedValue {
    private final String name;
    private final List<Type> parameters;
    private List<Constructor> constructors;

    public AlgebraicDataType(String name, List<Type> parameters, List<Constructor> constructors) {
        this.name = name;
        this.parameters = new ArrayList<>(parameters);
        this.constructors = constructors;
    }

    public String getName() {
        return name;
    }

    public List<Type> getParameters() {
        return parameters;
    }

    public List<Constructor> getConstructors() {
        return constructors;
    }


    public Type getBaseType() {
        return new AlgebraicDataType(name, new ArrayList<>(), null);
    }


    public List<Type> getArgs() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlgebraicDataType that = (AlgebraicDataType) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters);
    }

    @Override
    public String toString() {
        if (parameters.isEmpty()) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        for (Type param : parameters) {
            sb.append(" ").append(param.toString());
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Because an AlgebraicDataType is a type and not a term, we return null.
     */
    @Override
    public Type getType() {
        return null;
    }

    /**
     * Because an AlgebraicDataType is a type definition and not a term that can be evaluated, we return null.
     */
    @Override
    public Term getTerm() {
        return null;
    }

    /**
     * Applies a type substitution to the parameters of this algebraic data type.
     * This is necessary to handle type variables within the data type definition.
     *
     * @param substitution The substitution map to apply.
     * @return A new AlgebraicDataType with the substitution applied.
     */
    @Override
    public Type apply(Map<TVar, Type> substitution) {
        List<Type> newParameters = new ArrayList<>();
        for (Type param : parameters) {
            newParameters.add(param.apply(substitution));
        }
        return new AlgebraicDataType(name, newParameters, constructors);
    }

    /**
     * Gathers all free type variables from the parameters of the algebraic data type.
     *
     * @return A set of free type variables.
     */
    @Override
    public Set<TVar> getFreeTypeVariables() {
        Set<TVar> freeVars = new HashSet<>();
        for (Type param : parameters) {
            freeVars.addAll(param.getFreeTypeVariables());
        }
        return freeVars;
    }

    /**
     * Creates a deep clone of the type, with all type variables replaced
     * by fresh, unique ones. This is essential to prevent type variable
     * capture when a polymorphic type is used multiple times.
     *
     * @param unifier The unifier to generate fresh type variables.
     * @return A new instance of the type with fresh type variables.
     */
       @Override
//    public Type deepCloneAndFresh(Unifier unifier) {
//        List<Type> freshParameters = new ArrayList<>(this.parameters.size());
//        for (Type param : this.parameters) {
//            freshParameters.add(param.deepCloneAndFresh(unifier));
//        }
//        // The constructors themselves do not need to be cloned, as they are a part of the
//        // data type definition. The types *within* the constructors' definitions
//        // are where cloning happens via the Constructor's deepCloneAndFresh method.
//        return new AlgebraicDataType(this.name, freshParameters, this.constructors);
//    }
//
//    @Override
//    public Type deepCloneAndFresh(Unifier unifier) {
//        List<Type> freshParameters = new ArrayList<>();
//        for (Type param : this.parameters) {
//            freshParameters.add(param.deepCloneAndFresh(unifier));
//        }
//
//        // For recursive types like 'list a', we need special handling
//        // Create a mapping from old parameters to fresh ones
//        Map<TVar, Type> paramMap = new HashMap<>();
//        for (int i = 0; i < this.parameters.size(); i++) {
//            if (this.parameters.get(i) instanceof TVar) {
//                TVar oldParam = (TVar) this.parameters.get(i);
//                TVar freshParam = (TVar) freshParameters.get(i);
//                paramMap.put(oldParam, freshParam);
//            }
//        }
//
//        // Apply this mapping to any recursive references in constructor types
//        List<Constructor> freshConstructors = new ArrayList<>();
//        if (this.constructors != null) {
//            for (Constructor constructor : this.constructors) {
//                Type freshConstructorType = applyParamSubstitution(constructor.getType(), paramMap, unifier);
//                freshConstructors.add(new Constructor(constructor.getName(), freshConstructorType));
//            }
//        }
//
//        return new AlgebraicDataType(this.name, freshParameters, freshConstructors);
//    }
//
//    private Type applyParamSubstitution(Type type, Map<TVar, Type> paramMap, Unifier unifier) {
//        if (type instanceof TVar) {
//            return paramMap.getOrDefault(type, type.deepCloneAndFresh(unifier));
//        } else if (type instanceof FType) {
//            FType ftype = (FType) type;
//            return new FType(
//                applyParamSubstitution(ftype.getInput(), paramMap, unifier),
//                applyParamSubstitution(ftype.getOutput(), paramMap, unifier)
//            );
//        } else if (type instanceof AlgebraicDataType) {
//            // For recursive type references, apply the same substitution
//            AlgebraicDataType adt = (AlgebraicDataType) type;
//            if (adt.getName().equals(this.name)) {
//                // This is a recursive reference to the same type
//                List<Type> freshParams = new ArrayList<>();
//                for (Type param : adt.getParameters()) {
//                    freshParams.add(applyParamSubstitution(param, paramMap, unifier));
//                }
//                return new AlgebraicDataType(adt.getName(), freshParams, null);
//            }
//        }
//        return type.deepCloneAndFresh(unifier);
//    }


       public Type deepCloneAndFresh(Unifier unifier) {
           // For ADTs used in constructors, we need special handling
           if (this.constructors != null && !this.constructors.isEmpty()) {
               // This is a constructor-bearing ADT, preserve parameter relationships
               Map<TVar, TVar> freshMap = new HashMap<>();

               // Create fresh variables for all parameters, preserving relationships
               for (Type param : this.parameters) {
                   if (param instanceof TVar) {
                       TVar tvParam = (TVar) param;
                       freshMap.put(tvParam, TVar.fresh());
                   }
               }

               // Apply the fresh mapping to all parameters
               List<Type> freshParams = new ArrayList<>();
               for (Type param : this.parameters) {
                   freshParams.add(applyFreshMappingToType(param, freshMap));
               }

               // Also freshen the constructors with the same mapping
               List<Constructor> freshConstructors = new ArrayList<>();
               for (Constructor constructor : this.constructors) {
                   Type freshConstructorType = applyFreshMappingToType(constructor.getType(), freshMap);
                   freshConstructors.add(new Constructor(constructor.getName(), freshConstructorType));
               }

               return new AlgebraicDataType(this.name, freshParams, freshConstructors);
           }

           // For non-constructor ADTs, use standard freshening
           List<Type> freshParameters = new ArrayList<>();
           for (Type param : this.parameters) {
               freshParameters.add(param.deepCloneAndFresh(unifier));
           }
           return new AlgebraicDataType(this.name, freshParameters, this.constructors);
       }

    // Helper method for applying fresh variable mapping to types
    private Type applyFreshMappingToType(Type type, Map<TVar, TVar> freshMap) {
        if (type instanceof TVar) {
            TVar original = (TVar) type;
            return freshMap.getOrDefault(original, original);
        } else if (type instanceof FType) {
            FType ftype = (FType) type;
            return new FType(
                applyFreshMappingToType(ftype.getInput(), freshMap),
                applyFreshMappingToType(ftype.getOutput(), freshMap)
            );
        } else if (type instanceof AlgebraicDataType) {
            AlgebraicDataType adt = (AlgebraicDataType) type;
            List<Type> freshParams = new ArrayList<>();
            for (Type param : adt.getParameters()) {
                freshParams.add(applyFreshMappingToType(param, freshMap));
            }
            return new AlgebraicDataType(adt.getName(), freshParams, adt.getConstructors());
        } else if (type instanceof Constant) {
            return type; // Constants don't need freshening
        } else if (type instanceof ProdType) {
            ProdType prod = (ProdType) type;
            return new ProdType(
                applyFreshMappingToType(prod.getLeft(), freshMap),
                applyFreshMappingToType(prod.getRight(), freshMap)
            );
        } else if (type instanceof TApp) {
            TApp tapp = (TApp) type;
            return new TApp(
                applyFreshMappingToType(tapp.getTarget(), freshMap),
                applyFreshMappingToType(tapp.getArgument(), freshMap)
            );
        }
        return type;
    }

}