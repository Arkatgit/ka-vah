package ca.brock.cs.lambda.types;

import ca.brock.cs.lambda.parser.Term;

/**
 * A common interface for all top-level definitions in the language,
 * such as data constructors and function definitions.
 */
public interface DefinedValue {
    String getName();
    Type getType();
    Term getTerm();
}
