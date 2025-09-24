    package ca.brock.cs.lambda.parser;

    import ca.brock.cs.lambda.combinators.Combinator;
    import ca.brock.cs.lambda.combinators.CombinatorApplication;
    import ca.brock.cs.lambda.combinators.CombinatorConstant;
    import ca.brock.cs.lambda.intermediate.IntermediateApplication;
    import ca.brock.cs.lambda.intermediate.IntermediateConstant;
    import ca.brock.cs.lambda.intermediate.IntermediateLEqual;
    import ca.brock.cs.lambda.intermediate.IntermediateTerm;
    import ca.brock.cs.lambda.types.Constant;
    import ca.brock.cs.lambda.types.TVar;
    import ca.brock.cs.lambda.types.Type;
    import ca.brock.cs.lambda.types.Unifier;

    import java.util.HashSet;
    import java.util.Map;
    import java.util.Set;

    public class LEqual extends Term {
        private Term left;
        private Term right;

        public static final int precedence = 10;

        public LEqual(Term l, Term r)
        {
            left = l;
            right = r;
        }

        public Term getLeft() {
            return left;
        }

        public Term getRight() {
            return right;
        }

        @Override
        public String toStringPrec(int prec)
        {
            return left.toStringPrec(prec) + " <= " + right.toStringPrec(prec);
        }

        @Override
        protected Type computeType(Map<String, Type> env, Unifier unifier) {
            left.type(env, unifier);
            right.type(env, unifier);

            Type leftType = left.getType();
            Type rightType = right.getType();

            Map<TVar, Type> sub = unifier.unify(leftType, rightType);
            if (sub == null) {
                throw new RuntimeException("Comparison operands must have same type");
            }

            return new Constant("Bool");
        }
        @Override
        public Term eval(Map<String, Term> env) {
            Term evaluatedLeft = left.eval(env);
            Term evaluatedRight = right.eval(env);

            if (evaluatedLeft instanceof IntegerLiteral && evaluatedRight instanceof IntegerLiteral) {
                return new BooleanLiteral(((IntegerLiteral) evaluatedLeft).getValue() <= ((IntegerLiteral) evaluatedRight).getValue());
            }
            return new LEqual(evaluatedLeft, evaluatedRight); // Partially evaluated
        }

        @Override
        public Term substitute(String varName, Term value) {
            return new LEqual(left.substitute(varName, value), right.substitute(varName, value));
        }

        @Override
        public Set<String> getFreeVariables() {
            Set<String> freeVars = new HashSet<>();
            freeVars.addAll(left.getFreeVariables());
            freeVars.addAll(right.getFreeVariables());
            return freeVars;
        }

        /**
         * Translates this less-than-or-equal term into a combinator calculus term.
         * T[L <= R] = (<= T[L]) T[R]
         * @return The equivalent Combinator term.
         */
        @Override
        public Combinator translate() {
            // Create a CombinatorConstant for the less-than-or-equal operator "<="
            Combinator leOp = new CombinatorConstant("<=");
            // Translate the left and right operands
            Combinator translatedLeft = left.translate();
            Combinator translatedRight = right.translate();
            // Apply the operator to the translated left and then the result to the translated right
            return new CombinatorApplication(new CombinatorApplication(leOp, translatedLeft), translatedRight);
        }

    //    /**
    //     * Converts this LEqual to an IntermediateLEqual.
    //     * This is the first step in the two-phase translation to combinators.
    //     * @return The equivalent IntermediateLEqual.
    //     */
    //    @Override
    //    public IntermediateTerm toIntermediateTerm() {
    //        // Recursively convert left and right operands
    //        return new IntermediateLEqual(left.toIntermediateTerm(), right.toIntermediateTerm());
    //    }

        /**
         * Converts this LEqual to an IntermediateApplication.
         * T[L <= R] is represented as ((<= L) R) in the intermediate form.
         * @return The equivalent IntermediateApplication.
         */
        @Override
        public IntermediateTerm toIntermediateTerm() {
            // Represent 'L <= R' as an application: ((<= L) R)
            IntermediateConstant leOp = new IntermediateConstant("<=");
            IntermediateTerm translatedLeft = left.toIntermediateTerm();
            IntermediateTerm translatedRight = right.toIntermediateTerm();

            // First application: (<= L)
            IntermediateApplication partialApplication = new IntermediateApplication(leOp, translatedLeft);
            // Second application: ((<= L) R)
            return new IntermediateApplication(partialApplication, translatedRight);
        }

    }
