package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.Constant;
import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;
import ca.brock.ca.interpreter.Unifier;

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
        return new ca.brock.ca.interpreter.Constant("Bool");
    }
}
