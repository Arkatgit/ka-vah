package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.Constant;
import ca.brock.cs.lambda.types.Type;
import ca.brock.cs.lambda.types.Unifier;

import java.util.Map;

public class BooleanLiteral extends Term {
        private boolean value;

        public BooleanLiteral(boolean v)
        {
            value = v;
        }

        @Override
        public String toStringPrec(int prec)
        {
           return value? "True" : "False";
        }

    @Override
    protected Type computeType(Map<String, Type> env, Unifier unifier) {
        return new Constant("Bool");
    }
}
