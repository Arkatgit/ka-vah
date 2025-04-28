package ca.brock.cs.lambda;

import ca.brock.ca.interpreter.TVar;
import ca.brock.ca.interpreter.Type;

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
        public void type(Map<String, Type> env) {
            type = new TVar("Bool");
        }
    }
