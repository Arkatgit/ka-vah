package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.*;
import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A parser for a simple functional language.
 * A program is defined as a set of declarations and must contain a
 * function named "main" which serves as the entry point.
 */
public class ProgParser {

    private static final String[] SYMBOLS = {
        "(", ")", "True", "False", "and", "or", "=", "<=", "not",
        "+", "-", "*","/", ".", "\u03BB", "rec", "if", "then", "else",
        "data", "|", "->", "Int", "Bool", ":", ";",
        "match", "with", "end"
    };

    private static final Terminals progOperators = Terminals.operators(SYMBOLS);

    /**
     * A helper class to hold the result of parsing a program.
     */
    public static class ParsedProgram {
        public final Map<String, DefinedValue> symbolMap;
        public final FunctionDefinition mainFunction;

        public ParsedProgram(Map<String, DefinedValue> symbolMap, FunctionDefinition mainFunction) {
            this.symbolMap = symbolMap;
            this.mainFunction = mainFunction;
        }
    }

    /**
     * The main declaration parser, which can parse a data type, a function type signature, or a function body.
     */
    private static Parser<Void> declarationParser(
        Map<String, DefinedValue> symbolMap,
        Map<String, Type> signatureMap) {
        return Parsers.or(
            dataDeclParser(symbolMap),
            functionTypeSignatureDeclarationParser(signatureMap),
            functionBodyDeclarationParser(symbolMap, signatureMap)
        ).followedBy(progOperators.token(";")).optional();
    }

//    private static Parser<Void> optionalSemicolon() {
//        return progOperators.token(";").optional().retn(null);
//    }
//
//    private static Parser<Void> declarationParser(
//        Map<String, DefinedValue> symbolMap,
//        Map<String, Type> signatureMap) {
//        return Parsers.sequence(
//            Parsers.or(
//                dataDeclParser(symbolMap),
//                functionTypeSignatureDeclarationParser(signatureMap),
//                functionBodyDeclarationParser(symbolMap, signatureMap)
//            ),
//            optionalSemicolon(),  // Optional semicolon after any declaration
//            (decl, semi) -> null
//        );
//    }

    /**
     * Parses a data type declaration and adds the constructors to the symbol map.
     */
//    private static Parser<Void> dataDeclParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            progOperators.token("data"),
//            Terminals.Identifier.PARSER,
//            Terminals.Identifier.PARSER.many(),
//            progOperators.token("="),
//            constructorParser().sepBy1(progOperators.token("|")),
//            (dataToken, typeName, typeParams, equalsToken, constructorsData) -> {
//                List<TVar> tVars = typeParams.stream()
//                    .map(TVar::new)
//                    .collect(Collectors.toList());
//
//                String adtName = typeName;
//                List<Type> adtParameters = new ArrayList<>(tVars);
//
//                // Create a temporary ADT instance to use for constructor types
//                // The final ADT will be created after the constructors are ready.
//                AlgebraicDataType tempAdt = new AlgebraicDataType(adtName, adtParameters, null);
//
//                List<Constructor> constructors = new ArrayList<>();
//                for (ConstructorData cd : constructorsData) {
//
//                    System.out.println("DEBUG: Constructor '" + cd.name + "' parsed types: " + cd.types);
//
//                    Type constructorType;
//                    if (cd.types.isEmpty()) {
//                        // Nullary constructor (e.g., 'emptylist'), its type is the ADT itself
//                        constructorType = tempAdt;
//                    } else {
//                        // N-ary constructor (e.g., 'cons'), build a chain of function types
//                        // The last type is the return type, which is the ADT
//                        Type resultType = tempAdt;
//                        for (int i = cd.types.size() - 1; i >= 0; i--) {
//                            resultType = new FType(cd.types.get(i), resultType);
//                        }
//                        constructorType = resultType;
//                    }
//                    System.out.println("DEBUG: Constructor '" + cd.name + "' final type: " + constructorType);
//                    Constructor newConstructor = new Constructor(cd.name, constructorType);
//                    constructors.add(newConstructor);
//                    symbolMap.put(newConstructor.getName(), newConstructor);
//
//                }
//
//                // Now create the final, complete AlgebraicDataType object with all constructors
//                AlgebraicDataType finalAdt = new AlgebraicDataType(adtName, adtParameters, constructors);
//                symbolMap.put(adtName, finalAdt); // The ADT itself is also a defined value
//
//                return null;
//            }
//        );
//    }
//    private static Parser<Void> dataDeclParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            progOperators.token("data"),
//            Terminals.Identifier.PARSER,
//            Terminals.Identifier.PARSER.many(),
//            progOperators.token("="),
//            constructorParser().sepBy1(progOperators.token("|")),
//            (dataToken, typeName, typeParams, equalsToken, constructorsData) -> {
//                List<TVar> tVars = typeParams.stream()
//                    .map(TVar::new)
//                    .collect(Collectors.toList());
//
//                String adtName = typeName;
//                List<Type> adtParameters = new ArrayList<>(tVars);
//
//                // Create the ADT first
//                AlgebraicDataType adt = new AlgebraicDataType(adtName, adtParameters, null);
//
//                List<Constructor> constructors = new ArrayList<>();
//                for (ConstructorData cd : constructorsData) {
//                    Type constructorType;
//                    if (cd.types.isEmpty()) {
//                        // Nullary constructor
//                        constructorType = adt;
//                    } else {
//                        // Build function type from arguments to ADT
//                        Type resultType = adt;
//                        for (int i = cd.types.size() - 1; i >= 0; i--) {
//                            resultType = new FType(cd.types.get(i), resultType);
//                        }
//                        constructorType = resultType;
//                    }
//                    Constructor newConstructor = new Constructor(cd.name, constructorType);
//                    constructors.add(newConstructor);
//                    symbolMap.put(newConstructor.getName(), newConstructor);
//                }
//
//                // Update ADT with constructors
//                AlgebraicDataType finalAdt = new AlgebraicDataType(adtName, adtParameters, constructors);
//                symbolMap.put(adtName, finalAdt);
//
//                return null;
//            }
//        );
//    }

    private static Parser<Void> dataDeclParser(Map<String, DefinedValue> symbolMap) {
        return Parsers.sequence(
            progOperators.token("data"),
            Terminals.Identifier.PARSER,
            Terminals.Identifier.PARSER.many(),
            progOperators.token("="),
            constructorParser().sepBy1(progOperators.token("|")),
            (dataToken, typeName, typeParams, equalsToken, constructorsData) -> {
                List<TVar> tVars = typeParams.stream()
                    .map(TVar::new)
                    .collect(Collectors.toList());

                String adtName = typeName;

                // Create type parameter mapping for substitution
                Map<TVar, Type> typeParamMap = new HashMap<>();
                for (TVar tVar : tVars) {
                    typeParamMap.put(tVar, tVar); // Map each parameter to itself initially
                }

                List<Constructor> constructors = new ArrayList<>();
                for (ConstructorData cd : constructorsData) {
                    Type constructorType;
                    if (cd.types.isEmpty()) {
                        // Nullary constructor - use the ADT type with parameters
                        constructorType = new AlgebraicDataType(adtName, new ArrayList<>(tVars), null);
                    } else {
                        // Build function type with proper parameter substitution
                        Type resultType = new AlgebraicDataType(adtName, new ArrayList<>(tVars), null);
                        for (int i = cd.types.size() - 1; i >= 0; i--) {
                            // Apply type parameter substitution to argument types
                            Type argType = applyTypeParameters(cd.types.get(i), typeParamMap);
                            resultType = new FType(argType, resultType);
                        }
                        constructorType = resultType;
                    }
                    Constructor newConstructor = new Constructor(cd.name, constructorType);
                    constructors.add(newConstructor);
                    symbolMap.put(newConstructor.getName(), newConstructor);
                }

                // Create the complete ADT
                AlgebraicDataType completeAdt = new AlgebraicDataType(adtName, new ArrayList<>(tVars), constructors);
                symbolMap.put(adtName, completeAdt);

                // REGISTER CONSTRUCTORS
                List<String> constructorNames = constructorsData.stream()
                    .map(cd -> cd.name)
                    .collect(Collectors.toList());
                ConstructorRegistry.registerType(typeName, constructorNames);


                return null;
            }
        );
    }

    // Helper method to apply type parameter substitution
    private static Type applyTypeParameters(Type type, Map<TVar, Type> typeParamMap) {
        if (type instanceof TVar) {
            return typeParamMap.getOrDefault(type, type);
        } else if (type instanceof FType) {
            FType ft = (FType) type;
            return new FType(
                applyTypeParameters(ft.getInput(), typeParamMap),
                applyTypeParameters(ft.getOutput(), typeParamMap)
            );
        } else if (type instanceof AlgebraicDataType) {
            AlgebraicDataType adt = (AlgebraicDataType) type;
            List<Type> newParams = new ArrayList<>();
            for (Type param : adt.getParameters()) {
                newParams.add(applyTypeParameters(param, typeParamMap));
            }
            return new AlgebraicDataType(adt.getName(), newParams, adt.getConstructors());
        }
        return type;
    }

    /**
     * Helper class to temporarily hold parsed constructor data (name and argument types).
     */
    private static class ConstructorData {
        public final String name;
        public final List<Type> types;

        public ConstructorData(String name, List<Type> types) {
            this.name = name;
            this.types = types;
        }
    }

    /**
     * Parses a constructor definition, like `cons a (list a)`, returning the constructor name and its argument types.
     */
    private static Parser<ConstructorData> constructorParser() {
        return Parsers.sequence(
            Terminals.Identifier.PARSER,
            constructorArgTypeParser().many(),  // <-- CORRECT: Use constructorArgTypeParser
            ConstructorData::new
        );
    }

    /**
     * Parses a single constructor argument's type, which can be a simple type like `Int`
     * or a type application like `(list a)`.
     * It does not parse function types (`->`).
     */
//    private static Parser<Type> constructorArgTypeParser() {
//        // Basic atomic types
//        Parser<Type> atom = Parsers.or(
//            Terminals.Identifier.PARSER.map(id -> (Type) new TVar(id)),
//            progOperators.token("Int").map(s -> (Type) new ca.brock.cs.lambda.types.Constant("Int")),
//            progOperators.token("Bool").map(s -> (Type) new ca.brock.cs.lambda.types.Constant("Bool"))
//        );
//
//        return Parsers.or(
//            atom,
//            Parsers.sequence(
//                progOperators.token("("),
//                typeParser(), // Use the full type parser for parenthesized types
//                progOperators.token(")"),
//                (open, type, close) -> type
//            )
//        );
//    }

    private static Parser<Type> constructorArgTypeParser() {
        Parser.Reference<Type> typeRef = Parser.newReference();

        Parser<Type> atomType = Parsers.or(
            Terminals.Identifier.PARSER.<Type>map(TVar::new),
            progOperators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")),
            progOperators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool"))
        );

        // Parse type applications like (list a) as AlgebraicDataType
        Parser<Type> typeApplication = Parsers.sequence(
            Terminals.Identifier.PARSER, // Type constructor name (e.g., "list")
            atomType.many1(), // Type arguments (e.g., "a")
            (constructorName, typeArgs) -> {
                // Create an AlgebraicDataType for type applications like (list a)
                return new AlgebraicDataType(constructorName, typeArgs, null);
            }
        );

        // Parse either:
        // 1. A parenthesized type application: (list a) -> AlgebraicDataType
        // 2. A simple atomic type: a, Int, Bool
        Parser<Type> finalParser = Parsers.or(
            typeApplication.between(progOperators.token("("), progOperators.token(")")),
            atomType
        );

        typeRef.set(finalParser);
        return typeRef.lazy();
    }
    /**
     * Parses a function type signature declaration like `id : Int -> Int;`.
     * This adds a signature to a temporary map for later use.
     */
    private static Parser<Void> functionTypeSignatureDeclarationParser(Map<String, Type> signatureMap) {
        return Parsers.sequence(
            Terminals.Identifier.PARSER,
            progOperators.token(":"),
            typeParser(),
            (name, colon, type) -> {
                signatureMap.put(name, type);
                return null;
            }
        );
    }

    /**
     * Parses a function body declaration like `id = \u03BBy.y;`.
     * This retrieves the type from the signature map and creates the final FunctionDefinition.
     */
    private static Parser<Void> functionBodyDeclarationParser(
        Map<String, DefinedValue> symbolMap,
        Map<String, Type> signatureMap) {
        return Parsers.sequence(
            Terminals.Identifier.PARSER,
            progOperators.token("="),
            getTermParser(symbolMap),
            (name, equals, term) -> {
                // Check if a signature for this function exists
                System.out.println("DEBUG: Parsing function " + name + " with term: " + term);
                Type type = signatureMap.remove(name);
                // Creates a FunctionDefinition with the found type or null if no signature was present
                symbolMap.put(name, new FunctionDefinition(name, type, term));
                return null;
            }
        );
    }

    /**
     * Gets the term parser, which is the core of the expression parsing.
     */
//    private static Parser<Term> getTermParser(Map<String, DefinedValue> symbolMap) {
//        Parser.Reference<Term> termRef = Parser.newReference();
//
//        // New parser for a match expression
//        Parser<Term> matchParser = Parsers.sequence(
//            progOperators.token("match"),
//            termRef.lazy(),
//            progOperators.token("with"),
//            matchCaseParser(termRef).sepBy1(progOperators.token("|")),
//            progOperators.token("end"),
//            (match, input, with, cases, end) -> new Match(input, cases)
//        );
//
//        Parser<Term> atom = Parsers.or(
//            Terminals.Identifier.PARSER.map(id -> {
//                DefinedValue value = symbolMap.get(id);
//                if (value instanceof Term) {
//                    return (Term) value;
//                }
//                return new Variable(id);
//            }),
//            progOperators.token("True").retn(new BooleanLiteral(true)),
//            progOperators.token("False").retn(new BooleanLiteral(false)),
//            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
//            Parsers.sequence(progOperators.token("("), progOperators.token("*"), progOperators.token(")"), (open, op, close) -> new Constant("*")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("+"), progOperators.token(")"), (open, op, close) -> new Constant("+")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("-"), progOperators.token(")"), (open, op, close) -> new Constant("-"))
//        );
//
//        Parser<Term> term = termRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//            .or(atom)
//            .or(Parsers.sequence(progOperators.token("\u03BB"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Abstraction(s2, t)))
//            .or(Parsers.sequence(progOperators.token("if"), termRef.lazy(), progOperators.token("then"), termRef.lazy(), progOperators.token("else"), termRef.lazy(),
//                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)))
//            .or(Parsers.sequence(progOperators.token("rec"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Recursion(s2, t)))
//            .or(matchParser);
//
//        Parser<Term> applications = term.many1().map(ProgParser::makeApplications);
//
//        Parser<Term> parser = new OperatorTable<Term>()
//            .infixr(progOperators.token("or").retn(Or::new), Or.precedence)
//            .infixr(progOperators.token("and").retn(And::new), And.precedence)
//            .prefix(progOperators.token("not").retn(Not::new), Not.precedence)
//            .infixr(progOperators.token("=").retn(Equal::new), Equal.precedence)
//            .infixr(progOperators.token("<=").retn(LEqual::new), LEqual.precedence)
//            .infixr(progOperators.token("+").retn(Addition::new), Addition.precedence)
//            .infixn(progOperators.token("-").retn(Subtraction::new), Subtraction.precedence)
//            .infixr(progOperators.token("*").retn(Multiplication::new), Multiplication.precedence)
//            .infixr(progOperators.token("/").retn(Division::new), Division.precedence)
//            .build(applications);
//
//        termRef.set(parser);
//        return parser;
//    }
    private static Parser<Term> getTermParser(Map<String, DefinedValue> symbolMap) {
        Parser.Reference<Term> termRef = Parser.newReference();

        // New parser for a match expression
        Parser<Term> matchParser = Parsers.sequence(
            progOperators.token("match"),
            termRef.lazy(),
            progOperators.token("with"),
            matchCaseParser(termRef, symbolMap).sepBy1(progOperators.token("|")),
            progOperators.token("end"),
            (match, input, with, cases, end) -> new Match(input, cases)
        );

        Parser<Term> atom = Parsers.or(
            Terminals.Identifier.PARSER.map(id -> {
                DefinedValue value = symbolMap.get(id);
                if (value instanceof Term) {
                    return (Term) value;
                }
                return new Variable(id);
            }),
            progOperators.token("True").retn(new BooleanLiteral(true)),
            progOperators.token("False").retn(new BooleanLiteral(false)),
            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
            Parsers.sequence(progOperators.token("("), progOperators.token("*"), progOperators.token(")"), (open, op, close) -> new Constant("*")),
            Parsers.sequence(progOperators.token("("), progOperators.token("+"), progOperators.token(")"), (open, op, close) -> new Constant("+")),
            Parsers.sequence(progOperators.token("("), progOperators.token("-"), progOperators.token(")"), (open, op, close) -> new Constant("-"))
        );

        // Parse parenthesized expressions
        Parser<Term> parens = termRef.lazy().between(progOperators.token("("), progOperators.token(")"));

        // Parse all simple terms (atoms, parentheses, and other basic constructs)
        Parser<Term> simpleTerm = Parsers.or(
            parens,
            atom,
            Parsers.sequence(progOperators.token("\u03BB"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
                (s1, s2, s3, t) -> new Abstraction(s2, t)),
            Parsers.sequence(progOperators.token("if"), termRef.lazy(), progOperators.token("then"), termRef.lazy(), progOperators.token("else"), termRef.lazy(),
                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)),
            Parsers.sequence(progOperators.token("rec"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
                (s1, s2, s3, t) -> new Recursion(s2, t)),
            matchParser
        );

        // Parse function application (highest precedence)
        Parser<Term> application = simpleTerm.many1().map(terms -> {
            if (terms.isEmpty()) {
                return null;
            }
            Term result = terms.get(0);
            for(int i = 1; i < terms.size(); i++) {
                result = new Application(result, terms.get(i));
            }
            return result;
        });

        // Parse operators (lower precedence than application)
        Parser<Term> parser = new OperatorTable<Term>()
            .infixr(progOperators.token("or").retn(Or::new), Or.precedence)
            .infixr(progOperators.token("and").retn(And::new), And.precedence)
            .prefix(progOperators.token("not").retn(Not::new), Not.precedence)
            .infixr(progOperators.token("=").retn(Equal::new), Equal.precedence)
            .infixr(progOperators.token("<=").retn(LEqual::new), LEqual.precedence)
            .infixr(progOperators.token("+").retn(Addition::new), Addition.precedence)
            .infixn(progOperators.token("-").retn(Subtraction::new), Subtraction.precedence)
            .infixr(progOperators.token("*").retn(Multiplication::new), Multiplication.precedence)
            .infixr(progOperators.token("/").retn(Division::new), Division.precedence)
            .build(application);

        termRef.set(parser);
        return parser;
    }

    /**
     * Parses a match case, e.g., `cons x y -> z`.
     */
//    private static Parser<Match.Case> matchCaseParser(Parser.Reference<Term> termRef) {
//        return Parsers.sequence(
//            patternParser(),
//            progOperators.token("->"),
//            termRef.lazy(),
//            (pattern, arrow, resultTerm) -> new Match.Case(pattern, resultTerm)
//        );
//    }
    private static Parser<Match.Case> matchCaseParser(Parser.Reference<Term> termRef, Map<String, DefinedValue> symbolMap) {
        return Parsers.sequence(
            patternParser(symbolMap),  // Pass symbolMap here
            progOperators.token("->"),
            termRef.lazy(),
            (pattern, arrow, resultTerm) -> new Match.Case(pattern, resultTerm)
        );
    }

    /**
     * Parses a pattern, which can be a variable or a constructor.
     */
//    private static Parser<Pattern> patternParser() {
//        Parser.Reference<Pattern> patternRef = Parser.newReference();
//
//        Parser<Pattern> variablePattern = Terminals.Identifier.PARSER.map(VariablePattern::new);
//
//        Parser<Pattern> constructorPattern = Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            patternRef.lazy().many(),  // Back to .many()
//            (name, patterns) -> new ConstructorPattern(name, patterns)
//        );
//
//        Parser<Pattern> finalPatternParser = Parsers.or(
//            constructorPattern,
//            variablePattern
//        );
//
//        patternRef.set(finalPatternParser);
//        return patternRef.lazy();
//    }
    /**
     * Parses a pattern, which can be:
     * - Constants: True, False, integer literals
     * - Variables: identifiers
     * - Constructors: constructorName followed by patterns
     * - Parenthesized patterns: (pattern)
     */
//    private static Parser<Pattern> patternParser() {
//        Parser.Reference<Pattern> patternRef = Parser.newReference();
//
//        // 1. Constants patterns
//        Parser<Pattern> truePattern = progOperators.token("True").retn(new ConstantPattern(new BooleanLiteral(true)));
//        Parser<Pattern> falsePattern = progOperators.token("False").retn(new ConstantPattern(new BooleanLiteral(false)));
//        Parser<Pattern> intPattern = Terminals.IntegerLiteral.PARSER.map(
//            s -> new ConstantPattern(new IntegerLiteral(Integer.parseInt(s)))
//        );
//        Parser<Pattern> constantPattern = Parsers.or(truePattern, falsePattern, intPattern);
//
//        // 2. Variable patterns (identifiers that aren't constants)
//        Parser<Pattern> variablePattern = Terminals.Identifier.PARSER.map(VariablePattern::new);
//
//        // 3. Parenthesized patterns
//        Parser<Pattern> parenPattern = patternRef.lazy().between(
//            progOperators.token("("), progOperators.token(")")
//        );
//
//        // 4. Atomic patterns (patterns that don't have sub-patterns)
//        Parser<Pattern> atomicPattern = Parsers.or(constantPattern, variablePattern, parenPattern);
//
//        // 5. Constructor patterns: constructorName followed by one or more atomic patterns
//        Parser<Pattern> constructorPattern = Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            atomicPattern.atLeast(1),  // Constructor must have at least one argument
//            (name, patterns) -> {
//                System.out.println("DEBUG: Parsing constructor pattern: " + name + " with " + patterns.size() + " sub-patterns");
//               return new ConstructorPattern(name, patterns);
//            }
//        );
//
//        // 6. The full pattern grammar
//        Parser<Pattern> fullPattern = Parsers.or(
//            constructorPattern,  // Most specific first: constructor with arguments
//            constantPattern,     // Then constants
//            variablePattern,     // Then variables
//            parenPattern         // Then parenthesized patterns
//        );
//
//        patternRef.set(fullPattern);
//        return fullPattern;
//    }

    private static Parser<Pattern> patternParser(Map<String, DefinedValue> symbolMap) {
        Parser.Reference<Pattern> patternRef = Parser.newReference();

        // 1. Constants patterns
        Parser<Pattern> truePattern = progOperators.token("True").retn(new ConstantPattern(new BooleanLiteral(true)));
        Parser<Pattern> falsePattern = progOperators.token("False").retn(new ConstantPattern(new BooleanLiteral(false)));
        Parser<Pattern> intPattern = Terminals.IntegerLiteral.PARSER.map(
            s -> new ConstantPattern(new IntegerLiteral(Integer.parseInt(s)))
        );
        Parser<Pattern> constantPattern = Parsers.or(truePattern, falsePattern, intPattern);

        // 2. Variable patterns (identifiers that aren't constructors)
        Parser<Pattern> variablePattern = Terminals.Identifier.PARSER.map(id -> {
            // Check if this identifier is a known constructor
            if (symbolMap.containsKey(id) && symbolMap.get(id) instanceof Constructor) {
                // This is a nullary constructor (no arguments)
                return new ConstructorPattern(id, Collections.emptyList());
            }
            return new VariablePattern(id);
        });

        // 3. Parenthesized patterns
        Parser<Pattern> parenPattern = patternRef.lazy().between(
            progOperators.token("("), progOperators.token(")")
        );

        // 4. Atomic patterns
        Parser<Pattern> atomicPattern = Parsers.or(constantPattern, variablePattern, parenPattern);

        // 5. Constructor patterns with arguments
        Parser<Pattern> constructorPattern = Parsers.sequence(
            Terminals.Identifier.PARSER,
            atomicPattern.atLeast(1),
            (name, patterns) -> {
                // Verify this is actually a constructor
                if (!symbolMap.containsKey(name) || !(symbolMap.get(name) instanceof Constructor)) {
                    throw new ParserException("'" + name + "' is not a known constructor");
                }
                System.out.println("DEBUG: Parsing constructor pattern: " + name + " with " + patterns.size() + " arguments");
                return new ConstructorPattern(name, patterns);
            }
        );

        // 6. The full pattern grammar - constructor patterns first!
        Parser<Pattern> fullPattern = Parsers.or(
            constructorPattern,  // Constructor with arguments first
            constantPattern,     // Then constants
            variablePattern,     // Then variables (including nullary constructors)
            parenPattern         // Then parenthesized patterns
        );

        patternRef.set(fullPattern);
        return patternRef.lazy();
    }


    /**
     * Parses a type, handling type applications like "list a".
     */
    private static Parser<Type> typeParser() {
        Parser.Reference<Type> typeRef = Parser.newReference();

        Parser<Type> termType = Parsers.or(
            Terminals.Identifier.PARSER.<Type>map(TVar::new)
                .or(progOperators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
                .or(progOperators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool"))),
            typeRef.lazy().between(progOperators.token("("), progOperators.token(")"))
        );

        // Modify typeApplication to use AlgebraicDataType instead of TApp
        Parser<Type> typeApplication = termType.many1().map(types -> {
            if (types.size() == 1) return types.get(0);

            // For type applications like "list a", create AlgebraicDataType
            if (types.get(0) instanceof TVar && types.size() == 2) {
                TVar constructor = (TVar) types.get(0);
                Type argument = types.get(1);
                return new AlgebraicDataType(constructor.getName(), List.of(argument), null);
            }

            // Fallback for more complex cases (shouldn't occur in simple type signatures)
            Type result = new TApp(types.get(0), types.get(1));
            for (int i = 2; i < types.size(); i++) {
                result = new TApp(result, types.get(i));
            }
            return result;
        });

        Parser<Type> funcType = new OperatorTable<Type>()
            .infixr(progOperators.token("->").retn(FType::new), 10)
            .build(typeApplication);

        typeRef.set(funcType);
        return typeRef.lazy();
    }

//    private static Parser<Type> typeParser() {
//        Parser.Reference<Type> typeRef = Parser.newReference();
//
//        Parser<Type> termType = Parsers.or(
//            Terminals.Identifier.PARSER.<Type>map(TVar::new)
//                .or(progOperators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
//                .or(progOperators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool"))),
//            typeRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//        );
//
//        Parser<Type> typeApplication = termType.many1().map(types -> {
//            if (types.size() == 1) return types.get(0);
//            Type result = new TApp(types.get(0), types.get(1));
//            for (int i = 2; i < types.size(); i++) {
//                result = new TApp(result, types.get(i));
//            }
//            return result;
//        });
//
//        Parser<Type> funcType = new OperatorTable<Type>()
//            .infixr(progOperators.token("->").retn(FType::new), 10)
//            .build(typeApplication);
//
//        typeRef.set(funcType);
//        return typeRef.lazy();
//    }

    /**
     * Parses the entire program, which is a series of declarations, and validates
     * that a "main" function is defined.
     */
    public static Parser<ParsedProgram> programParser(Map<String, DefinedValue> symbolMap) {
        Map<String, Type> signatureMap = new HashMap<>();
        return declarationParser(symbolMap, signatureMap).many().map(declarations -> {
            if (!signatureMap.isEmpty()) {
                String missingSignatures = String.join(", ", signatureMap.keySet());
                throw new ParserException("Signatures declared without definitions for: " + missingSignatures);
            }
            DefinedValue mainValue = symbolMap.get("main");
            if (mainValue == null || !(mainValue instanceof FunctionDefinition)) {
                throw new ParserException("A 'main' function is required but not found or is not a function.");
            }
            return new ParsedProgram(symbolMap, (FunctionDefinition) mainValue);
        });
    }

    /**
     * The public parse method that runs the main program parser.
     */
    public static ParsedProgram parse(CharSequence source, Map<String, DefinedValue> symbolMap) {
        return programParser(symbolMap)
            .from(progOperators.tokenizer().cast().or(
                        Terminals.Identifier.TOKENIZER)
                    .or(Terminals.IntegerLiteral.TOKENIZER),
                Scanners.WHITESPACES.skipMany())
            .parse(source);
    }

    private static Term makeApplications(List<Term> l) {
        if (l.isEmpty()) {
            return null;
        }
        Term result = l.get(0);
        for(int i = 1; i < l.size(); i++) {
            result = new Application(result, l.get(i));
        }
        return result;
    }



    /**
     * Helper function to parse a program, set up the environment, and check all function types.
     * @param programString The program code to parse and check.
     */
    private static void checkAndPrintTypes(String programString) {
        Map<String, DefinedValue> symbolMap = new HashMap<>();
        ParsedProgram program = parse(programString, symbolMap);

        // First, populate the environment with all constructors and function signatures
        Map<String, Type> baseEnv = new HashMap<>();
        System.out.println("Symbol map contents:");
        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            System.out.println("  " + entry.getKey() + " : " +
                (entry.getValue() instanceof Constructor ?
                    ((Constructor)entry.getValue()).getType() :
                    entry.getValue().getClass().getSimpleName()));
            DefinedValue def = entry.getValue();

            if (def instanceof Constructor) {
                baseEnv.put(entry.getKey(), def.getType());
            } else if (def instanceof FunctionDefinition) {
                FunctionDefinition funcDef = (FunctionDefinition) def;
                if (funcDef.getType() != null) {
                    baseEnv.put(entry.getKey(), funcDef.getType());
                }
          }
//            else if (def instanceof AlgebraicDataType) {
//                 add ADT names to the environment
//                AlgebraicDataType adt = (AlgebraicDataType) def;
//                baseEnv.put(entry.getKey(), adt);
//            }

        }

        // Now, iterate and type check each function definition
        for (DefinedValue definedValue : symbolMap.values()) {
            if (definedValue instanceof FunctionDefinition) {
                FunctionDefinition funcDef = (FunctionDefinition) definedValue;
                System.out.println("\nChecking function: " + funcDef.getName());

                Type declaredType = funcDef.getType();

                if (funcDef.getTerm() == null) {
                    System.out.println("  - No term defined for function");
                    continue;
                }

                try {
                    // Create fresh unifier and environment for each function
                    Unifier functionUnifier = new Unifier();
                    Map<String, Type> functionEnv = new HashMap<>(baseEnv);
                    functionUnifier.setSymbolMap(symbolMap);

                    // Compute the type
                    Type inferredType = funcDef.getTerm().computeType(functionEnv, functionUnifier);
                   // funcDef.getTerm().type(functionEnv, functionUnifier);
                    //Type inferredType = funcDef.getTerm().getType();
                    System.out.println("  - Inferred type: " + inferredType);

                    if (declaredType != null) {
                        System.out.println("  - Declared type: " + declaredType);

                        // Try to unify declared and inferred types
                        Map<TVar, Type> unificationResult = functionUnifier.unify(declaredType, inferredType);
                        if (unificationResult != null) {
                            System.out.println("  - Declared and inferred types unified successfully.");
                        } else {
                            System.out.println("  - Type Mismatch! Could not unify declared and inferred types.");
                        }
                    } else {
                        System.out.println("  - No type signature provided.");
                    }

                } catch (Exception e) {
                    System.out.println("  - Failed to compute type: " + e.getMessage());
                    throw new TypeError(e.getMessage());
                    // Continue with other functions even if this one fails
                }
            }
        }
    }

    public static void main(String[] args) {
        Map<String, DefinedValue> symbolMap = new HashMap<>();

        String[] testPrograms = {

            // Test 1
            "fact : Int -> Int;\n" +
                "fact = rec f. λn.\n" +
                "    if n <= 1\n" +
                "    then 1\n" +
                "    else n * (f (n - 1));\n\n" +
                "main : Int; " +
                "main = fact 5;",

            // Test 2: Basic list operations with type error (should fail)
            "data list a = emptylist | cons a (list a);\n" +
                "length : list a -> Int;\n" +
                "length = λxs. match xs with\n" +
                "    emptylist -> 0\n" +  // Type error: should return Int, not Bool
                "    | cons x xs_tail -> 1 + (length xs_tail)\n" +
                "end;\n" +
                "main = length (cons 1 (cons 2 emptylist));\n",

            // Test 3: Correct list length function
            "data list a = emptylist | cons a (list a);\n" +
                "length : list a -> Int;\n" +
                "length = λxs. match xs with\n" +
                "    emptylist -> 0\n" +  // Correct: returns Int
                "    | cons x xs_tail -> 1 + (length xs_tail)\n" +
                "end;\n" +
                "main = length (cons 1 (cons 2 emptylist));\n",

            // Test 4: Basic arithmetic
            "add : Int -> Int -> Int;\n" +
                "add = λy. λx. x + y;\n" +
                "main = add 5 3;",

            "compose : (b -> c) -> (a -> b) -> (a -> c);\n" +
                "compose = λf. λg. λx. f (g x);\n" +
                "double : Int -> Int;\n" +
                "double = λx. x * 2;\n" +
                "isEven : Int -> Bool;\n" +
                "isEven = λy. if y/ 2 = 0 then True else False  ;\n" +
                "main = compose isEven double 5;",

            // Test 6: Tree data type with pattern matching
            "data Tree a = Leaf a | Node (Tree a) a (Tree a);\n" +
                "isNode : Tree Int -> Bool;\n" +
                "isNode = λt. match t with Leaf x -> False | Node l y r -> True end;\n" +
                "main = isNode (Node (Leaf 1) 2 (Leaf 3));",

            // Test 7: Recursive function with correct types
            "data list a = emptylist | cons a (list a);\n" +
                "sum : list Int -> Int;\n" +
                "sum = λxs. match xs with\n" +
                "    emptylist -> 0\n" +
                "    | cons x xs_tail -> x + (sum xs_tail)\n" +
                "end;\n" +
                "main = sum (cons 1 (cons 2 (cons 3 emptylist)));",  // Should return 6

            // Test 8: Higher-order functions
            "data list a = emptylist | cons a (list a);\n" +
                "map : (a -> b) -> list a -> list b;\n" +
                "map = λf. λxs. match xs with\n" +
                "    emptylist -> emptylist\n" +
                "    | cons x xs_tail -> cons (f x) (map f xs_tail)\n" +
                "end;\n" +
                "double : Int -> Int;\n" +
                "double = λx. x * 2;\n" +
                "main = map double (cons 1 (cons 2 emptylist));",  // Should return [2, 4]

            // Test 9: Polymorphic identity function
            "id : a -> a;\n" +
                "id = λx. x;\n" +
                "main = id 42;",  // Should work with any type

            // Test 10: Type error in pattern matching (should fail)
            "data list a = emptylist | cons a (list a);\n" +
                "badFunction : list Int -> Int;\n" +
                "badFunction = λxs. match xs with\n" +
               "    emptylist -> 0\n" +
                "    | cons x xs_tail -> x + badFunction xs_tail\n" +  // x should be Int
                "    | cons x xs_tail -> x \n" +  // Type error: can't use 'and' with Int
                "end;\n" +
                "main = badFunction (cons 1 emptylist);",


            // Test 11: Mutual recursion
            "data list a = emptylist | cons a (list a);\n" +
                "evenLength : list a -> Bool;\n" +
                "evenLength = λxs. match xs with\n" +
                "    emptylist -> True\n" +
                "    | cons x xs_tail -> oddLength xs_tail\n" +
                "end;\n" +
                "oddLength : list a -> Bool;\n" +
                "oddLength = λxs. match xs with\n" +
                "    emptylist -> False\n" +
                "    | cons x xs_tail -> evenLength xs_tail\n" +
                "end;\n" +
                "main = evenLength (cons 1 (cons 2 emptylist));",  // Should return True

            // Test 12: Complex type application
            "data Box a = MBox a;\n" +
                "applyToBox : (a -> b) -> Box a -> Box b;\n" +
                "applyToBox = λf. λb. match b with MBox x -> MBox (f x) end;\n" +
                "increment : Int -> Int;\n" +
                "increment = λx. x + 1;\n" +
                "main = applyToBox increment (MBox 5);",  // Should return Box 6

            // Test 13: Complex nested pattern matching with different constructors
            "data Option a = Nothing | Just a;\n" +
                "data Pair a b = Pair a b;\n" +
                "unwrap : Option (Pair Int Bool) -> Int;\n" +
                "unwrap = λopt. match opt with\n" +
                "    Nothing -> 0\n" +
                "    | Just (Pair x y) -> if y then x else -x\n" +
                "end;\n" +
                "main = unwrap (Just (Pair 5 True));",

            // Test 14: Higher-order function with complex type
            "compose3 : (c -> d) -> (b -> c) -> (a -> b) -> (a -> d);\n" +
                "compose3 = λf. λg. λh. λx. f (g (h x));\n" +
                "inc : Int -> Int;\n" +
                "inc = λx. x + 1;\n" +
                "triple : Int -> Int;\n" +
                "triple = λx. x * 3;\n" +
                "main = compose3 inc triple inc 5;",

            // Test 15: Multiple type parameters
            "data Either a b = Left a | Right b;\n" +
                "swap : (Either a b) -> (Either b a);\n" +  // Removed extra parentheses
                "swap = λe. match e with\n" +
                "    Left x -> Right x\n" +
                "    | Right y -> Left y\n" +
                "end;\n" +
                "main = swap (Left 42);",

        };

        for (int i = 0; i < testPrograms.length; i++) {
            try {
                System.out.println("=== Test " + (i + 1) + " ===");
                checkAndPrintTypes(testPrograms[i]);
                System.out.println("Test " + (i + 1) + " passed!\n");
            } catch (Exception e) {
                System.err.println("Test " + (i + 1) + " failed with error: " + e.getMessage() + "\n");
            }
        }
    }


//    public static void main(String[] args) {
//        Map<String, DefinedValue> symbolMap = new HashMap<>();
//
//        // A test program that defines a `list` data type, a `length` function,
//        // and a `main` function that uses it.
//        String [] testPrograms = {
//            "data list a = emptylist | cons a (list a);\n" +
//                "length : list a -> Int;\n" +
//                "length = λxs. match xs with\n" +
//                "    emptylist -> True\n" +
//                "    | cons x xs_tail -> 1 + (length xs_tail)\n" +
//                "end;\n" +
//                "main = length (cons 1 (cons 2 emptylist));\n",
//
//            "add : Int -> Int -> Int;\n" +
//                "add = λy. λx. x + y;\n" +
//                "main = add 5 3;",
//
//            // Test Case 5: Nested ADT with pattern matching
//            "data Tree a = Leaf a | Node (Tree a) a (Tree a);\n" +
//                "isNode : Tree Int -> Bool;\n" +
//                "isNode = λt. match t with Leaf x -> False | Node l y r -> True end;\n" +
//                "main = isNode (Node (Leaf 1) 2 (Leaf 3));",
//
//           };
//        for (String testProgram : testPrograms) {
//            try {
//                checkAndPrintTypes(testProgram);
//                System.out.println("\nTest passed!");
//            } catch (Exception e) {
//                System.err.println("\nTest failed with an error: " + e.getMessage());
//            }
//        }
//    }

}

//package ca.brock.cs.lambda.parser;
//
//import ca.brock.cs.lambda.types.*;
//import org.jparsec.OperatorTable;
//import org.jparsec.Parser;
//import org.jparsec.Parsers;
//import org.jparsec.Scanners;
//import org.jparsec.Terminals;
//
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * A parser for a simple functional language that enforces strict rules for function
// * signatures and definitions.
// * A signature is optional, but if present, must be immediately followed by a definition.
// * A definition can exist on its own without a signature.
// */
//public class ProgParser {
//
//    /**
//     * Removed "x" from the SYMBOLS array so it is correctly parsed as an Identifier.
//     * Including it here would treat it as a reserved keyword.
//     */
//    private static final String[] SYMBOLS = {
//        "(", ")", "True", "False", "and", "or", "=", "<=", "not",
//        "+", "-", "*", ".", "\u03BB", "rec", "if", "then", "else",
//        "data", "|", "->", "Int", "Bool", ":", ";",
//        "match", "with", "end"
//    };
//
//    private static final Terminals progOperators = Terminals.operators(SYMBOLS);
//
//    /**
//     * A helper class to hold the result of parsing a program.
//     */
//    public static class ParsedProgram {
//        public final Map<String, DefinedValue> symbolMap;
//        public final Term finalTerm;
//
//        public ParsedProgram(Map<String, DefinedValue> symbolMap, Term finalTerm) {
//            this.symbolMap = symbolMap;
//            this.finalTerm = finalTerm;
//        }
//    }
//
//    /**
//     * The main declaration parser, which can parse a data type, a function type signature, or a function body.
//     */
//    private static Parser<Void> declarationParser(
//        Map<String, DefinedValue> symbolMap,
//        Map<String, Type> signatureMap) {
//        return Parsers.or(
//            dataDeclParser(symbolMap),
//            functionTypeSignatureDeclarationParser(signatureMap),
//            functionBodyDeclarationParser(symbolMap, signatureMap)
//        ).followedBy(progOperators.token(";"));
//    }
//
//    /**
//     * Parses a data type declaration and adds the constructors to the symbol map.
//     */
//    private static Parser<Void> dataDeclParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            progOperators.token("data"),
//            Terminals.Identifier.PARSER,
//            Terminals.Identifier.PARSER.many(),
//            progOperators.token("="),
//            constructorParser().sepBy1(progOperators.token("|")),
//            (dataToken, typeName, typeParams, equalsToken, constructors) -> {
//                List<TVar> tVars = typeParams.stream()
//                    .map(TVar::new)
//                    .collect(Collectors.toList());
//                AlgebraicDataType adt = new AlgebraicDataType(typeName, new ArrayList<>(tVars), constructors);
//                for (Constructor constructor : constructors) {
//                    symbolMap.put(constructor.getName(), constructor);
//                }
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Parses a function type signature declaration like `id : Int -> Int;`.
//     * This adds a signature to a temporary map for later use.
//     */
//    private static Parser<Void> functionTypeSignatureDeclarationParser(Map<String, Type> signatureMap) {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            progOperators.token(":"),
//            typeParser(),
//            (name, colon, type) -> {
//                signatureMap.put(name, type);
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Parses a function body declaration like `id = \u03BBy.y;`.
//     * This retrieves the type from the signature map and creates the final FunctionDefinition.
//     */
//    private static Parser<Void> functionBodyDeclarationParser(
//        Map<String, DefinedValue> symbolMap,
//        Map<String, Type> signatureMap) {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            progOperators.token("="),
//            getTermParser(symbolMap),
//            (name, equals, term) -> {
//                // Check if a signature for this function exists
//                Type type = signatureMap.remove(name);
//                // Creates a FunctionDefinition with the found type or null if no signature was present
//                symbolMap.put(name, new FunctionDefinition(name, type, term));
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Gets the term parser, which is the core of the expression parsing.
//     */
//    private static Parser<Term> getTermParser(Map<String, DefinedValue> symbolMap) {
//        Parser.Reference<Term> termRef = Parser.newReference();
//
//        // New parser for a match expression
//        Parser<Term> matchParser = Parsers.sequence(
//            progOperators.token("match"),
//            termRef.lazy(),
//            progOperators.token("with"),
//            matchCaseParser(termRef).sepBy1(progOperators.token("|")), // Correctly separates cases by '|'
//            progOperators.token("end"),
//            (match, input, with, cases, end) -> new Match(input, cases)
//        );
//
//        Parser<Term> atom = Parsers.or(
//            Terminals.Identifier.PARSER.map(id -> {
//                DefinedValue value = symbolMap.get(id);
//                if (value instanceof Term) {
//                    return (Term) value;
//                }
//                return new Variable(id);
//            }),
//            progOperators.token("True").retn(new BooleanLiteral(true)),
//            progOperators.token("False").retn(new BooleanLiteral(false)),
//            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
//            Parsers.sequence(progOperators.token("("), progOperators.token("*"), progOperators.token(")"), (open, op, close) -> new Constant("*")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("+"), progOperators.token(")"), (open, op, close) -> new Constant("+")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("-"), progOperators.token(")"), (open, op, close) -> new Constant("-"))
//        );
//
//        Parser<Term> term = termRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//            .or(atom)
//            .or(Parsers.sequence(progOperators.token("\u03BB"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Abstraction(s2, t)))
//            .or(Parsers.sequence(progOperators.token("if"), termRef.lazy(), progOperators.token("then"), termRef.lazy(), progOperators.token("else"), termRef.lazy(),
//                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)))
//            .or(Parsers.sequence(progOperators.token("rec"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Recursion(s2, t)))
//            .or(matchParser); // Add the new match parser
//
//        Parser<Term> applications = term.many1().map(ProgParser::makeApplications);
//
//        Parser<Term> parser = new OperatorTable<Term>()
//            .infixr(progOperators.token("or").retn(Or::new), Or.precedence)
//            .infixr(progOperators.token("and").retn(And::new), And.precedence)
//            .prefix(progOperators.token("not").retn(Not::new), Not.precedence)
//            .infixr(progOperators.token("=").retn(Equal::new), Equal.precedence)
//            .infixr(progOperators.token("<=").retn(LEqual::new), LEqual.precedence)
//            .infixr(progOperators.token("+").retn(Addition::new), Addition.precedence)
//            .infixn(progOperators.token("-").retn(Subtraction::new), Subtraction.precedence)
//            .infixr(progOperators.token("*").retn(Multiplication::new), Multiplication.precedence)
//            .build(applications);
//
//        termRef.set(parser);
//        return parser;
//    }
//
//    /**
//     * Parses a match case, e.g., `cons x y -> z`.
//     */
//    private static Parser<Match.Case> matchCaseParser(Parser.Reference<Term> termRef) {
//        return Parsers.sequence(
//            patternParser(),
//            progOperators.token("->"),
//            termRef.lazy(),
//            (pattern, arrow, resultTerm) -> new Match.Case(pattern, resultTerm)
//        );
//    }
//
//    /**
//     * Parses a pattern, which can be a variable or a constructor.
//     */
//    private static Parser<Pattern> patternParser() {
//        Parser.Reference<Pattern> patternRef = Parser.newReference();
//
//        // A variable pattern is an identifier on its own.
//        Parser<Pattern> variablePattern = Terminals.Identifier.PARSER.map(VariablePattern::new);
//
//        // A constructor pattern is an identifier followed by zero or more patterns.
//        Parser<Pattern> constructorPattern = Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            patternRef.lazy().many(), // Use lazy() here to break the recursion
//            (name, patterns) -> new ConstructorPattern(name, patterns)
//        );
//
//        // A pattern is either a constructor pattern or a variable pattern
//        Parser<Pattern> finalPatternParser = Parsers.or(
//            constructorPattern,
//            variablePattern
//        );
//
//        patternRef.set(finalPatternParser);
//        return patternRef.lazy();
//    }
//
//
//    /**
//     * Parses a constructor and its types, e.g., "cons a (list a)".
//     */
//    private static Parser<Constructor> constructorParser() {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            typeParser().many(),
//            (name, types) -> {
//                // The type of a constructor with multiple arguments is a function type.
//                // E.g., cons a (list a) has type a -> (list a) -> (list a).
//                if (types.isEmpty()) {
//                    return new Constructor(name, null); // For nullary constructors
//                }
//                // The return type is the last type in the list, which should be the user-defined type itself.
//                Type returnType = types.get(types.size() - 1);
//                Type current = returnType;
//                // Build the function type from right to left
//                for (int i = types.size() - 2; i >= 0; i--) {
//                    current = new ProdType(types.get(i), current);
//                }
//                return new Constructor(name, current);
//            }
//        );
//    }
//
//    /**
//     * Parses a type, handling type applications like "list a".
//     */
//    private static Parser<Type> typeParser() {
//        Parser.Reference<Type> typeRef = Parser.newReference();
//
//        // A term type is a base type or a parenthesized type.
//        Parser<Type> termType = Parsers.or(
//            Terminals.Identifier.PARSER.<Type>map(TVar::new)
//                .or(progOperators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
//                .or(progOperators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool"))),
//            typeRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//        );
//
//        // Type application: `T1 T2`
//        Parser<Type> typeApplication = termType.many1().map(types -> {
//            if (types.size() == 1) return types.get(0);
//            Type result = new TApp(types.get(0), types.get(1));
//            for (int i = 2; i < types.size(); i++) {
//                result = new TApp(result, types.get(i));
//            }
//            return result;
//        });
//
//        // Function and product types
//        Parser<Type> funcType = new OperatorTable<Type>()
//            .infixr(progOperators.token("->").retn(FType::new), 10)
//            .build(typeApplication);
//
//        typeRef.set(funcType);
//        return typeRef.lazy();
//    }
//
//    /**
//     * Parses the entire program, including declarations and a final term.
//     */
//    private static Parser<ParsedProgram> programParser(Map<String, DefinedValue> symbolMap) {
//        Map<String, Type> signatureMap = new HashMap<>();
//        return Parsers.sequence(
//            declarationParser(symbolMap, signatureMap).many(),
//            getTermParser(symbolMap).optional(),
//            (declarations, optionalTerm) -> {
//                // Final check to ensure no signatures were declared without a definition.
//                if (!signatureMap.isEmpty()) {
//                    String missingSignatures = String.join(", ", signatureMap.keySet());
//                    throw new ParserException("Signatures declared without definitions for: " + missingSignatures);
//                }
//                Term mainTerm = optionalTerm == null ? null : optionalTerm;
//                return new ParsedProgram(symbolMap, mainTerm);
//            }
//        );
//    }
//
//    /**
//     * The public parse method that runs the main program parser.
//     */
//    public static ParsedProgram parse(CharSequence source, Map<String, DefinedValue> symbolMap) {
//        return programParser(symbolMap)
//            .from(progOperators.tokenizer().cast().or(
//                        Terminals.Identifier.TOKENIZER)
//                    .or(Terminals.IntegerLiteral.TOKENIZER),
//                Scanners.WHITESPACES.skipMany())
//            .parse(source);
//    }
//
//    private static Term makeApplications(List<Term> l) {
//        if (l.isEmpty()) {
//            return null;
//        }
//        Term result = l.get(0);
//        for(int i = 1; i < l.size(); i++) {
//            result = new Application(result, l.get(i));
//        }
//        return result;
//    }
//
//    public static void main(String[] args) {
//        Map<String, DefinedValue> symbolMap = new HashMap<>();
//        // Test case with a match expression, corrected to include the '|' separator
//        String matchTest = "data list a = emptylist | cons a (list a); mylist = cons 1 (cons 2 emptylist); match mylist with emptylist -> 0 | cons x y -> x end";
//
//        try {
//            System.out.println("--- Parsing Match Expression ---");
//            ParsedProgram result = parse(matchTest, symbolMap);
//            System.out.println("Parsing successful!");
//            System.out.println("Final Term: " + result.finalTerm);
//        } catch (ParserException e) {
//            System.err.println("Parsing failed (unexpected): " + e.getMessage());
//        }
//    }
//}
//

//package ca.brock.cs.lambda.parser;
//
//import ca.brock.cs.lambda.types.*;
//import org.jparsec.OperatorTable;
//import org.jparsec.Parser;
//import org.jparsec.Parsers;
//import org.jparsec.Scanners;
//import org.jparsec.Terminals;
//
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.HashMap;
//
///**
// * A parser for a simple functional language that enforces strict rules for function
// * signatures and definitions.
// * A signature is optional, but if present, must be immediately followed by a definition.
// * A definition can exist on its own without a signature.
// */
//public class ProgParser {
//
//    private static final String[] SYMBOLS = {
//        "(", ")", "True", "False", "and", "or", "=", "<=", "not",
//        "+", "-", "*", ".", "\u03BB", "rec", "if", "then", "else",
//        "data", "|", "->", "Int", "Bool", ":", ";"
//    };
//
//    private static final Terminals progOperators = Terminals.operators(SYMBOLS);
//
//    /**
//     * A helper class to hold the result of parsing a program.
//     */
//    public static class ParsedProgram {
//        public final Map<String, DefinedValue> symbolMap;
//        public final Term finalTerm;
//
//        public ParsedProgram(Map<String, DefinedValue> symbolMap, Term finalTerm) {
//            this.symbolMap = symbolMap;
//            this.finalTerm = finalTerm;
//        }
//    }
//
//    /**
//     * The main declaration parser, which can parse a data type, a function type signature, or a function body.
//     */
//    private static Parser<Void> declarationParser(
//        Map<String, DefinedValue> symbolMap,
//        Map<String, Type> signatureMap) {
//        return Parsers.or(
//            dataDeclParser(symbolMap),
//            functionTypeSignatureDeclarationParser(signatureMap),
//            functionBodyDeclarationParser(symbolMap, signatureMap)
//        ).followedBy(progOperators.token(";"));
//    }
//
//    /**
//     * Parses a data type declaration and adds the constructors to the symbol map.
//     */
//    private static Parser<Void> dataDeclParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            progOperators.token("data"),
//            Terminals.Identifier.PARSER,
//            Terminals.Identifier.PARSER.many(),
//            progOperators.token("="),
//            constructorParser().sepBy1(progOperators.token("|")),
//            (dataToken, typeName, typeParams, equalsToken, constructors) -> {
//                List<TVar> tVars = typeParams.stream()
//                    .map(TVar::new)
//                    .collect(Collectors.toList());
//                AlgebraicDataType adt = new AlgebraicDataType(typeName, new ArrayList<>(tVars), constructors);
//                for (Constructor constructor : constructors) {
//                    symbolMap.put(constructor.getName(), constructor);
//                }
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Parses a function type signature declaration like `id : Int -> Int;`.
//     * This adds a signature to a temporary map for later use.
//     */
//    private static Parser<Void> functionTypeSignatureDeclarationParser(Map<String, Type> signatureMap) {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            progOperators.token(":"),
//            typeParser(),
//            (name, colon, type) -> {
//                signatureMap.put(name, type);
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Parses a function body declaration like `id = \u03BBy.y;`.
//     * This retrieves the type from the signature map and creates the final FunctionDefinition.
//     */
//    private static Parser<Void> functionBodyDeclarationParser(
//        Map<String, DefinedValue> symbolMap,
//        Map<String, Type> signatureMap) {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            progOperators.token("="),
//            getTermParser(symbolMap),
//            (name, equals, term) -> {
//                // Check if a signature for this function exists
//                Type type = signatureMap.remove(name);
//                // Creates a FunctionDefinition with the found type or null if no signature was present
//                symbolMap.put(name, new FunctionDefinition(name, type, term));
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Gets the term parser, which is the core of the expression parsing.
//     */
//    private static Parser<Term> getTermParser(Map<String, DefinedValue> symbolMap) {
//        Parser.Reference<Term> termRef = Parser.newReference();
//
//        Parser<Term> atom = Parsers.or(
//            Terminals.Identifier.PARSER.map(id -> {
//                DefinedValue value = symbolMap.get(id);
//                if (value instanceof Term) {
//                    return (Term) value;
//                }
//                return new Variable(id);
//            }),
//            progOperators.token("True").retn(new BooleanLiteral(true)),
//            progOperators.token("False").retn(new BooleanLiteral(false)),
//            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
//            Parsers.sequence(progOperators.token("("), progOperators.token("*"), progOperators.token(")"), (open, op, close) -> new Constant("*")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("+"), progOperators.token(")"), (open, op, close) -> new Constant("+")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("-"), progOperators.token(")"), (open, op, close) -> new Constant("-"))
//        );
//
//        Parser<Term> term = termRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//            .or(atom)
//            .or(Parsers.sequence(progOperators.token("\u03BB"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Abstraction(s2, t)))
//            .or(Parsers.sequence(progOperators.token("if"), termRef.lazy(), progOperators.token("then"), termRef.lazy(), progOperators.token("else"), termRef.lazy(),
//                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)))
//            .or(Parsers.sequence(progOperators.token("rec"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Recursion(s2, t)));
//
//        Parser<Term> applications = term.many1().map(ProgParser::makeApplications);
//
//        Parser<Term> parser = new OperatorTable<Term>()
//            .infixr(progOperators.token("or").retn(Or::new), Or.precedence)
//            .infixr(progOperators.token("and").retn(And::new), And.precedence)
//            .prefix(progOperators.token("not").retn(Not::new), Not.precedence)
//            .infixr(progOperators.token("=").retn(Equal::new), Equal.precedence)
//            .infixr(progOperators.token("<=").retn(LEqual::new), LEqual.precedence)
//            .infixr(progOperators.token("+").retn(Addition::new), Addition.precedence)
//            .infixn(progOperators.token("-").retn(Subtraction::new), Subtraction.precedence)
//            .infixr(progOperators.token("*").retn(Multiplication::new), Multiplication.precedence)
//            .build(applications);
//
//        termRef.set(parser);
//        return parser;
//    }
//
//    /**
//     * Parses a constructor and its types, e.g., "cons a (list a)".
//     */
//    private static Parser<Constructor> constructorParser() {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            typeParser().many(),
//            (name, types) -> {
//                if (types.isEmpty()) {
//                    return new Constructor(name, null);
//                }
//                Type current = types.get(types.size() - 1);
//                for (int i = types.size() - 2; i >= 0; i--) {
//                    current = new ProdType(types.get(i), current);
//                }
//                return new Constructor(name, current);
//            }
//        );
//    }
//
//    /**
//     * Parses a type, handling type applications like "list a".
//     */
//    private static Parser<Type> typeParser() {
//        Parser.Reference<Type> typeRef = Parser.newReference();
//
//        // A term type is a base type or a parenthesized type.
//        Parser<Type> termType = Parsers.or(
//            Terminals.Identifier.PARSER.<Type>map(TVar::new)
//                .or(progOperators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
//                .or(progOperators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool"))),
//            typeRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//        );
//
//        // Type application: `T1 T2`
//        Parser<Type> typeApplication = termType.many1().map(types -> {
//            if (types.size() == 1) return types.get(0);
//            Type result = new TApp(types.get(0), types.get(1));
//            for (int i = 2; i < types.size(); i++) {
//                result = new TApp(result, types.get(i));
//            }
//            return result;
//        });
//
//        // Function and product types
//        Parser<Type> funcType = new OperatorTable<Type>()
//            .infixr(progOperators.token("->").retn(FType::new), 10)
//            .build(typeApplication);
//
//        typeRef.set(funcType);
//        return typeRef.lazy();
//    }
//
//    /**
//     * Parses the entire program, including declarations and a final term.
//     */
//    private static Parser<ParsedProgram> programParser(Map<String, DefinedValue> symbolMap) {
//        Map<String, Type> signatureMap = new HashMap<>();
//        return Parsers.sequence(
//            declarationParser(symbolMap, signatureMap).many(),
//            getTermParser(symbolMap).optional(),
//            (declarations, optionalTerm) -> {
//                // Final check to ensure no signatures were declared without a definition.
//                if (!signatureMap.isEmpty()) {
//                    String missingSignatures = String.join(", ", signatureMap.keySet());
//                    throw new ParserException("Signatures declared without definitions for: " + missingSignatures);
//                }
//                Term mainTerm = optionalTerm == null ? null : optionalTerm;
//                return new ParsedProgram(symbolMap, mainTerm);
//            }
//        );
//    }
//
//    /**
//     * The public parse method that runs the main program parser.
//     */
//    public static ParsedProgram parse(CharSequence source, Map<String, DefinedValue> symbolMap) {
//        return programParser(symbolMap)
//            .from(progOperators.tokenizer().cast().or(
//                        Terminals.Identifier.TOKENIZER)
//                    .or(Terminals.IntegerLiteral.TOKENIZER),
//                Scanners.WHITESPACES.skipMany())
//            .parse(source);
//    }
//
//    private static Term makeApplications(List<Term> l) {
//        if (l.isEmpty()) {
//            return null;
//        }
//        Term result = l.get(0);
//        for(int i = 1; i < l.size(); i++) {
//            result = new Application(result, l.get(i));
//        }
//        return result;
//    }
//
//    public static void main(String[] args) {
//        Map<String, DefinedValue> symbolMap = new HashMap<>();
//        try {
//            String testString = "data list a = emptylist | cons a (list a); id : Int -> Int; id = λy.y; test = cons 1 (cons 2 emptylist); test";
//
//
//            ParsedProgram result = parse(testString, symbolMap);
//
//            System.out.println("--- Parsed Program Details ---");
//            System.out.println("Symbol Map:");
//            for (Map.Entry<String, DefinedValue> entry : result.symbolMap.entrySet()) {
//                String name = entry.getKey();
//                DefinedValue value = entry.getValue();
//
//                System.out.printf("  - %s: ", name);
//                if (value instanceof FunctionDefinition) {
//                    FunctionDefinition funcDef = (FunctionDefinition) value;
//                    System.out.printf("Function (Type: %s, Term: %s)\n", funcDef.getType(), funcDef.getTerm());
//                } else if (value instanceof AlgebraicDataType) {
//                    AlgebraicDataType adt = (AlgebraicDataType) value;
//                    System.out.printf("Algebraic Data Type (Name: %s, TVars: %s, Constructors: %s)\n", adt.getName(), adt.getParameters(), adt.getConstructors());
//                } else if (value instanceof Constructor) {
//                    Constructor constructor = (Constructor) value;
//                    String typeString = (constructor.getType() != null) ? constructor.getType().toString() : "No type defined";
//                    System.out.printf("Constructor (Type: %s)\n", typeString);
//
//                } else {
//                    System.out.printf("Other Defined Value (Type: %s, Value: %s)\n", value.getClass().getSimpleName(), value);
//                }
//            }
//
//            System.out.println("\nFinal Term:");
//            if (result.finalTerm != null) {
//                System.out.printf("  - %s\n", result.finalTerm);
//            } else {
//                System.out.println("  - No final term found in the program.");
//            }
//            System.out.println("------------------------------");
//
//        } catch (ParserException e) {
//            System.err.println("Parsing failed: " + e.getMessage());
//        }
//    }
//
//}

//
//package ca.brock.cs.lambda.parser;
//
//
//import ca.brock.cs.lambda.types.*;
//import org.jparsec.OperatorTable;
//import org.jparsec.Parser;
//import org.jparsec.Parsers;
//import org.jparsec.Scanners;
//import org.jparsec.Terminals;
//import org.jparsec.error.ParserException;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.HashMap;
//
///**
// * A parser for a simple functional language.
// * This version correctly handles both declarations and a final expression,
// * and enforces that a function signature, if present, is immediately followed
// * by a body.
// */
//public class ProgParser {
//
//    // Corrected SYMBOLS array - 'x' has been removed so it can be used as an identifier.
//    private static final String[] SYMBOLS = {
//        "(", ")", "True", "False", "and", "or", "=", "<=", "not",
//        "+", "-", "*", ".", "\u03BB", "rec", "if", "then", "else",
//        "data", "|", "->", "Int", "Bool", ":", ";"
//    };
//
//    private static final Terminals progOperators = Terminals.operators(SYMBOLS);
//
//    /**
//     * A helper class to hold the result of parsing a program.
//     */
//    public static class ParsedProgram {
//        public final Map<String, DefinedValue> symbolMap;
//        public final Term finalTerm;
//
//        public ParsedProgram(Map<String, DefinedValue> symbolMap, Term finalTerm) {
//            this.symbolMap = symbolMap;
//            this.finalTerm = finalTerm;
//        }
//    }
//
//    /**
//     * The main declaration parser, which can parse a data type, a function type signature, or a function body.
//     */
//    private static Parser<Void> declarationParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.or(
//            dataDeclParser(symbolMap),
//            functionTypeSignatureDeclarationParser(symbolMap),
//            functionBodyDeclarationParser(symbolMap)
//        ).followedBy(progOperators.token(";"));
//    }
//
//    /**
//     * Parses a data type declaration and adds the constructors to the symbol map.
//     */
//    private static Parser<Void> dataDeclParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            progOperators.token("data"),
//            Terminals.Identifier.PARSER,
//            Terminals.Identifier.PARSER.many(),
//            progOperators.token("="),
//            constructorParser().sepBy1(progOperators.token("|")),
//            (dataToken, typeName, typeParams, equalsToken, constructors) -> {
//                List<TVar> tVars = typeParams.stream()
//                    .map(TVar::new)
//                    .collect(Collectors.toList());
//
//                AlgebraicDataType adt = new AlgebraicDataType(typeName, new ArrayList<>(tVars), constructors);
//
//                for (Constructor constructor : constructors) {
//                    symbolMap.put(constructor.getName(), constructor);
//                }
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Parses a function type signature declaration like `id : Int -> Int;`.
//     */
//    private static Parser<Void> functionTypeSignatureDeclarationParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            progOperators.token(":"),
//            typeParser(),
//            (name, colon, type) -> {
//                // Creates a FunctionDefinition with a null term
//                symbolMap.put(name, new FunctionDefinition(name, type, null));
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Parses a function body declaration like `id = \u03BBy.y;`.
//     */
//    private static Parser<Void> functionBodyDeclarationParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            progOperators.token("="),
//            getTermParser(symbolMap),
//            (name, equals, term) -> {
//                // Looks for an existing FunctionDefinition and updates its term
//                DefinedValue existing = symbolMap.get(name);
//                if (existing instanceof FunctionDefinition) {
//                    FunctionDefinition funcDef = (FunctionDefinition) existing;
//                    funcDef.setTerm(term);
//                } else {
//                    // Or creates a new one if it doesn't exist
//                    symbolMap.put(name, new FunctionDefinition(name, null, term));
//                }
//                return null;
//            }
//        );
//    }
//
//    /**
//     * Gets the term parser, which is the core of the expression parsing.
//     */
//    private static Parser<Term> getTermParser(Map<String, DefinedValue> symbolMap) {
//        Parser.Reference<Term> termRef = Parser.newReference();
//
//        Parser<Term> atom = Parsers.or(
//            Terminals.Identifier.PARSER.map(id -> {
//                DefinedValue value = symbolMap.get(id);
//                if (value instanceof Term) {
//                    return (Term) value;
//                }
//                return new Variable(id);
//            }),
//            progOperators.token("True").retn(new BooleanLiteral(true)),
//            progOperators.token("False").retn(new BooleanLiteral(false)),
//            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
//            Parsers.sequence(progOperators.token("("), progOperators.token("*"), progOperators.token(")"), (open, op, close) -> new Constant("*")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("+"), progOperators.token(")"), (open, op, close) -> new Constant("+")),
//            Parsers.sequence(progOperators.token("("), progOperators.token("-"), progOperators.token(")"), (open, op, close) -> new Constant("-"))
//        );
//
//        Parser<Term> term = termRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//            .or(atom)
//            .or(Parsers.sequence(progOperators.token("\u03BB"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Abstraction(s2, t)))
//            .or(Parsers.sequence(progOperators.token("if"), termRef.lazy(), progOperators.token("then"), termRef.lazy(), progOperators.token("else"), termRef.lazy(),
//                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)))
//            .or(Parsers.sequence(progOperators.token("rec"), Terminals.Identifier.PARSER, progOperators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Recursion(s2, t)));
//
//        Parser<Term> applications = term.many1().map(ProgParser::makeApplications);
//
//        Parser<Term> parser = new OperatorTable<Term>()
//            .infixr(progOperators.token("or").retn(Or::new), Or.precedence)
//            .infixr(progOperators.token("and").retn(And::new), And.precedence)
//            .prefix(progOperators.token("not").retn(Not::new), Not.precedence)
//            .infixr(progOperators.token("=").retn(Equal::new), Equal.precedence)
//            .infixr(progOperators.token("<=").retn(LEqual::new), LEqual.precedence)
//            .infixr(progOperators.token("+").retn(Addition::new), Addition.precedence)
//            .infixn(progOperators.token("-").retn(Subtraction::new), Subtraction.precedence)
//            .infixr(progOperators.token("*").retn(Multiplication::new), Multiplication.precedence)
//            .build(applications);
//
//        termRef.set(parser);
//        return parser;
//    }
//
//    /**
//     * Parses a constructor and its types, e.g., "cons a (list a)".
//     */
//    private static Parser<Constructor> constructorParser() {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            typeParser().many(),
//            (name, types) -> {
//                if (types.isEmpty()) {
//                    return new Constructor(name, null);
//                }
//                Type current = types.get(types.size() - 1);
//                for (int i = types.size() - 2; i >= 0; i--) {
//                    current = new ProdType(types.get(i), current);
//                }
//                return new Constructor(name, current);
//            }
//        );
//    }
//
//    /**
//     * Parses a type, handling type applications like "list a".
//     */
//    private static Parser<Type> typeParser() {
//        Parser.Reference<Type> typeRef = Parser.newReference();
//
//        // A term type is a base type or a parenthesized type.
//        Parser<Type> termType = Parsers.or(
//            Terminals.Identifier.PARSER.<Type>map(TVar::new)
//                .or(progOperators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
//                .or(progOperators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool"))),
//            typeRef.lazy().between(progOperators.token("("), progOperators.token(")"))
//        );
//
//        // Type application: `T1 T2`
//        Parser<Type> typeApplication = termType.many1().map(types -> {
//            if (types.size() == 1) return types.get(0);
//            Type result = new TApp(types.get(0), types.get(1));
//            for (int i = 2; i < types.size(); i++) {
//                result = new TApp(result, types.get(i));
//            }
//            return result;
//        });
//
//        // Function and product types
//        Parser<Type> funcType = new OperatorTable<Type>()
//            .infixr(progOperators.token("->").retn(FType::new), 10)
//            .build(typeApplication);
//
//        typeRef.set(funcType);
//        return typeRef.lazy();
//    }
//
//    /**
//     * Parses the entire program, including declarations and a final term.
//     */
//    private static Parser<ParsedProgram> programParser(Map<String, DefinedValue> symbolMap) {
//        return Parsers.sequence(
//            declarationParser(symbolMap).many(),
//            getTermParser(symbolMap).optional(),
//            (declarations, optionalTerm) -> {
//                Term mainTerm = optionalTerm == null ? null : optionalTerm;
//                return new ParsedProgram(symbolMap, mainTerm);
//            }
//        );
//    }
//
//    /**
//     * The public parse method that runs the main program parser.
//     */
//    public static ParsedProgram parse(CharSequence source, Map<String, DefinedValue> symbolMap) {
//        return programParser(symbolMap)
//            .from(progOperators.tokenizer().cast().or(
//                        Terminals.Identifier.TOKENIZER)
//                    .or(Terminals.IntegerLiteral.TOKENIZER),
//                Scanners.WHITESPACES.skipMany())
//            .parse(source);
//    }
//
//    private static Term makeApplications(List<Term> l) {
//        if (l.isEmpty()) {
//            return null;
//        }
//        Term result = l.get(0);
//        for(int i = 1; i < l.size(); i++) {
//            result = new Application(result, l.get(i));
//        }
//        return result;
//    }
//    public static void main(String[] args) {
//        Map<String, DefinedValue> symbolMap = new HashMap<>();
//
//        // Updated test string to reflect the correct syntax
//        String testString = "data list a = emptylist | cons a (list a); id : Int -> Int; id = \u03BBy.y; test = cons 1 (cons 2 emptylist);";
//
//        try {
//            ParsedProgram result = parse(testString, symbolMap);
//
//            System.out.println("--- Parsed Program Details ---");
//            System.out.println("Symbol Map:");
//            for (Map.Entry<String, DefinedValue> entry : result.symbolMap.entrySet()) {
//                String name = entry.getKey();
//                DefinedValue value = entry.getValue();
//
//                System.out.printf("  - %s: ", name);
//                if (value instanceof FunctionDefinition) {
//                    FunctionDefinition funcDef = (FunctionDefinition) value;
//                    System.out.printf("Function (Type: %s, Term: %s)\n", funcDef.getType(), funcDef.getTerm());
//                } else if (value instanceof AlgebraicDataType) {
//                    AlgebraicDataType adt = (AlgebraicDataType) value;
//                    System.out.printf("Algebraic Data Type (Name: %s, TVars: %s, Constructors: %s)\n", adt.getName(), adt.getParameters(), adt.getConstructors());
//                } else if (value instanceof Constructor) {
//                    Constructor constructor = (Constructor) value;
//                    String typeString = (constructor.getType() != null) ? constructor.getType().toString() : "No type defined";
//                    System.out.printf("Constructor (Type: %s)\n", typeString);
//
//                } else {
//                    System.out.printf("Other Defined Value (Type: %s, Value: %s)\n", value.getClass().getSimpleName(), value);
//                }
//            }
//
//            System.out.println("\nFinal Term:");
//            if (result.finalTerm != null) {
//                System.out.printf("  - %s\n", result.finalTerm);
//            } else {
//                System.out.println("  - No final term found in the program.");
//            }
//            System.out.println("------------------------------");
//
//        } catch (ParserException e) {
//            System.err.println("Parsing failed: " + e.getMessage());
//        }
//    }
//}
//
//

//package ca.brock.cs.lambda.parser;
//
//import ca.brock.cs.lambda.types.*;
//import org.jparsec.OperatorTable;
//import org.jparsec.Parser;
//import org.jparsec.Parsers;
//import org.jparsec.Scanners;
//import org.jparsec.Terminals;
//import org.jparsec.Token; // Import Token
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class ProgParser {
//
//    // Add "Int" and "Bool" to the SYMBOLS array so the parser recognizes them.
//    private static final String[] SYMBOLS = { "(", ")", "True", "False", "and", "or", "=", "<=", "not", "+", "-", "*", ".", "\u03BB", "rec", "if", "then", "else", "data", "|", "->", "x", "Int", "Bool"};
//
//    private static final Terminals progOperators = Terminals.operators(SYMBOLS);
//
//    // Map to store constructor names to their corresponding constructor objects
//    private final Map<String, Constructor> constructorMap = new HashMap<>();
//
//    public ProgParser() {
//        // We can initialize the constructorMap with built-in or default constructors here if needed.
//    }
//
//    public Parser<Term> programParser() {
//        // A program consists of zero or more data declarations followed by a single term.
//        Parser<Term> termParser = getTermParser(progOperators);
//        return Parsers.sequence(
//            dataDeclParser(progOperators).many(), // Parse all data declarations
//            termParser, // Parse the main term
//            (declarations, mainTerm) -> mainTerm
//        );
//    }
//
//    // This parser populates the constructorMap as a side effect
//    private Parser<Void> dataDeclParser(Terminals operators) {
//        return Parsers.sequence(
//            operators.token("data"),
//            Terminals.Identifier.PARSER, // Data type name
//            Terminals.Identifier.PARSER.many(), // Type parameters (e.g., 'a' in 'list a')
//            operators.token("="),
//            constructorParser(operators).sepBy1(operators.token("|")), // Constructors
//            (dataToken, typeName, typeParams, equalsToken, constructors) -> {
//                processDataDeclaration(dataToken, typeName, typeParams, equalsToken, constructors);
//                return null;
//            }
//        );
//    }
//
//    // Helper method to process the data declaration and populate the constructor map
//    // The parameters for the tokens have been changed from Void to Token.
//    private Void processDataDeclaration(Token dataToken, String typeName, List<String> typeParams, Token equalsToken, List<Constructor> constructors) {
//        // Map the type parameters to TVar objects
//        List<TVar> tVars = typeParams.stream()
//            .map(TVar::new)
//            .collect(Collectors.toList());
//
//        // Create the algebraic data type
//        AlgebraicDataType adt = new AlgebraicDataType(typeName, new ArrayList<>(tVars), constructors);
//
//        // Populate the constructor map with the new constructors
//        for (Constructor constructor : constructors) {
//            constructorMap.put(constructor.getName(), constructor);
//        }
//        return null;
//    }
//
//
//    private Parser<Term> getTermParser(Terminals operators) {
//        Parser.Reference<Term> termRef = Parser.newReference();
//
//        Parser<Term> atom = Parsers.or(
//            // Check if the identifier is a known constructor first
//            Terminals.Identifier.PARSER.map(id -> constructorMap.containsKey(id) ? constructorMap.get(id) : new Variable(id)),
//            operators.token("True").retn(new BooleanLiteral(true)),
//            operators.token("False").retn(new BooleanLiteral(false)),
//            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
//            Parsers.sequence(operators.token("("), operators.token("*"), operators.token(")"), (open, op, close) -> new Constant("*")),
//            Parsers.sequence(operators.token("("), operators.token("+"), operators.token(")"), (open, op, close) -> new Constant("+")),
//            Parsers.sequence(operators.token("("), operators.token("-"), operators.token(")"), (open, op, close) -> new Constant("-"))
//        );
//
//        Parser<Term> term = termRef.lazy().between(operators.token("("), operators.token(")"))
//            .or(atom)
//            .or(Parsers.sequence(operators.token("\u03BB"), Terminals.Identifier.PARSER, operators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Abstraction(s2, t)))
//            .or(Parsers.sequence(operators.token("if"), termRef.lazy(), operators.token("then"), termRef.lazy(), operators.token("else"), termRef.lazy(),
//                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)))
//            .or(Parsers.sequence(operators.token("rec"), Terminals.Identifier.PARSER, operators.token("."), termRef.lazy(),
//                (s1, s2, s3, t) -> new Recursion(s2, t)))
//            .or(Parsers.sequence(operators.token("("), operators.token("*"), termRef.lazy(), operators.token(")"),
//                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("*")), r)))
//            .or(Parsers.sequence(operators.token("("), operators.token("+"), termRef.lazy(), operators.token(")"),
//                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("+")), r)))
//            .or(Parsers.sequence(operators.token("("), operators.token("-"), termRef.lazy(), operators.token(")"),
//                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("-")), r)))
//            .or(Parsers.sequence(operators.token("("), termRef.lazy(), operators.token("*"), operators.token(")"),
//                (open, l, op, close) -> new Application(new Constant("*"), l)))
//            .or(Parsers.sequence(operators.token("("), termRef.lazy(), operators.token("+"), operators.token(")"),
//                (open, l, op, close) -> new Application(new Constant("+"), l)))
//            .or(Parsers.sequence(operators.token("("), termRef.lazy(), operators.token("-"), operators.token(")"),
//                (open, l, op, close) -> new Application(new Constant("-"), l)));
//
//        Parser<Term> typeTerm = term.many1().map(this::makeApplications);
//
//        Parser<Term> parser = new OperatorTable<Term>()
//            .infixr(operators.token("or").retn(Or::new), Or.precedence)
//            .infixr(operators.token("and").retn(And::new), And.precedence)
//            .prefix(operators.token("not").retn(Not::new), Not.precedence)
//            .infixr(operators.token("=").retn(Equal::new), Equal.precedence)
//            .infixr(operators.token("<=").retn(LEqual::new), LEqual.precedence)
//            .infixr(operators.token("+").retn(Addition::new), Addition.precedence)
//            .infixn(operators.token("-").retn(Subtraction::new), Subtraction.precedence)
//            .infixr(operators.token("*").retn(Multiplication::new), Multiplication.precedence)
//            .build(typeTerm);
//
//        termRef.set(parser);
//        return parser;
//    }
//
//    private Parser<Constructor> constructorParser(Terminals operators) {
//        return Parsers.sequence(
//            Terminals.Identifier.PARSER,
//            typeParser(operators).many(),
//            (name, types) -> new Constructor(name, types.isEmpty() ? null : types.get(0))
//        );
//    }
//
//    // This method has been corrected to handle the type ambiguity with the Constant class.
//    private Parser<Type> typeParser(Terminals operators) {
//        Parser.Reference<Type> typeRef = Parser.newReference();
//
//        // To not confuse the parser.Constant with the types.Constant class.
//        // We now use the fully qualified name to be explicit.
//        Parser<Type> atomType = Terminals.Identifier.PARSER.<Type>map(TVar::new)
//            .or(operators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
//            .or(operators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool")));
//
//        Parser<Type> type = typeRef.lazy().between(operators.token("("), operators.token(")"))
//            .or(atomType);
//
//        Parser<Type> funcType = new OperatorTable<Type>()
//            .infixr(operators.token("->").retn(FType::new), 10)
//            .infixr(operators.token("x").retn(ProdType::new), 20)
//            .build(type);
//
//        typeRef.set(funcType);
//        return typeRef.lazy();
//    }
//
//    public Term parse(CharSequence source) {
//        return programParser()
//            .from(progOperators.tokenizer().cast().or(
//                        Terminals.Identifier.TOKENIZER)
//                    .or(Terminals.IntegerLiteral.TOKENIZER),
//                Scanners.WHITESPACES.skipMany())
//            .parse(source);
//    }
//
//    private Term makeApplications(List<Term> l) {
//        Term result = l.get(0);
//        for(int i = 1; i < l.size(); i++) {
//            result = new Application(result, l.get(i));
//        }
//        return result;
//    }
//
//    public static void main(String[] args) {
//        ProgParser parser = new ProgParser();
//
//        Term consTerm = parser.parse("data list a = emptylist | cons a (list a)" + "cons 1 (cons 2 emptylist)");
//        System.out.println( consTerm);
//    }
//}

/*
package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.types.*;
import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Token; // Import Token

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProgParser {

    // Add "Int" and "Bool" to the SYMBOLS array so the parser recognizes them.
    private static final String[] SYMBOLS = { "(", ")", "True", "False", "and", "or", "=", "<=", "not", "+", "-", "*", ".", "\u03BB", "rec", "if", "then", "else", "data", "|", "->", "x", "Int", "Bool"};

    private static final Terminals progOperators = Terminals.operators(SYMBOLS);

    // Map to store constructor names to their corresponding constructor objects
    private final Map<String, Constructor> constructorMap = new HashMap<>();

    public ProgParser() {
        // We can initialize the constructorMap with built-in or default constructors here if needed.
    }

    public Parser<Term> programParser() {
        // A program consists of zero or more data declarations followed by a single term.
        Parser<Term> termParser = getTermParser(progOperators);
        return Parsers.sequence(
            dataDeclParser(progOperators).many(), // Parse all data declarations
            termParser, // Parse the main term
            (declarations, mainTerm) -> mainTerm
        );
    }

    // This parser populates the constructorMap as a side effect
    private Parser<Void> dataDeclParser(Terminals operators) {
        return Parsers.sequence(
            operators.token("data"),
            Terminals.Identifier.PARSER, // Data type name
            Terminals.Identifier.PARSER.many(), // Type parameters (e.g., 'a' in 'list a')
            operators.token("="),
            constructorParser(operators).sepBy1(operators.token("|")), // Constructors
            (dataToken, typeName, typeParams, equalsToken, constructors) -> {
                processDataDeclaration(dataToken, typeName, typeParams, equalsToken, constructors);
                return null;
            }
        );
    }

    // Helper method to process the data declaration and populate the constructor map
    // The parameters for the tokens have been changed from Void to Token.
    private Void processDataDeclaration(Token dataToken, String typeName, List<String> typeParams, Token equalsToken, List<Constructor> constructors) {
        // Map the type parameters to TVar objects
        List<TVar> tVars = typeParams.stream()
            .map(TVar::new)
            .collect(Collectors.toList());

        // Create the algebraic data type
        AlgebraicDataType adt = new AlgebraicDataType(typeName, new ArrayList<>(tVars), constructors);

        // Populate the constructor map with the new constructors
        for (Constructor constructor : constructors) {
            constructorMap.put(constructor.getName(), constructor);
        }
        return null;
    }


    private Parser<Term> getTermParser(Terminals operators) {
        Parser.Reference<Term> termRef = Parser.newReference();

        Parser<Term> atom = Parsers.or(
            // Check if the identifier is a known constructor first
            Terminals.Identifier.PARSER.map(id -> constructorMap.containsKey(id) ? constructorMap.get(id) : new Variable(id)),
            operators.token("True").retn(new BooleanLiteral(true)),
            operators.token("False").retn(new BooleanLiteral(false)),
            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
            Parsers.sequence(operators.token("("), operators.token("*"), operators.token(")"), (open, op, close) -> new Constant("*")),
            Parsers.sequence(operators.token("("), operators.token("+"), operators.token(")"), (open, op, close) -> new Constant("+")),
            Parsers.sequence(operators.token("("), operators.token("-"), operators.token(")"), (open, op, close) -> new Constant("-"))
        );

        Parser<Term> term = termRef.lazy().between(operators.token("("), operators.token(")"))
            .or(atom)
            .or(Parsers.sequence(operators.token("\u03BB"), Terminals.Identifier.PARSER, operators.token("."), termRef.lazy(),
                (s1, s2, s3, t) -> new Abstraction(s2, t)))
            .or(Parsers.sequence(operators.token("if"), termRef.lazy(), operators.token("then"), termRef.lazy(), operators.token("else"), termRef.lazy(),
                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)))
            .or(Parsers.sequence(operators.token("rec"), Terminals.Identifier.PARSER, operators.token("."), termRef.lazy(),
                (s1, s2, s3, t) -> new Recursion(s2, t)))
            .or(Parsers.sequence(operators.token("("), operators.token("*"), termRef.lazy(), operators.token(")"),
                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("*")), r)))
            .or(Parsers.sequence(operators.token("("), operators.token("+"), termRef.lazy(), operators.token(")"),
                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("+")), r)))
            .or(Parsers.sequence(operators.token("("), operators.token("-"), termRef.lazy(), operators.token(")"),
                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("-")), r)))
            .or(Parsers.sequence(operators.token("("), termRef.lazy(), operators.token("*"), operators.token(")"),
                (open, l, op, close) -> new Application(new Constant("*"), l)))
            .or(Parsers.sequence(operators.token("("), termRef.lazy(), operators.token("+"), operators.token(")"),
                (open, l, op, close) -> new Application(new Constant("+"), l)))
            .or(Parsers.sequence(operators.token("("), termRef.lazy(), operators.token("-"), operators.token(")"),
                (open, l, op, close) -> new Application(new Constant("-"), l)));

        Parser<Term> typeTerm = term.many1().map(this::makeApplications);

        Parser<Term> parser = new OperatorTable<Term>()
            .infixr(operators.token("or").retn(Or::new), Or.precedence)
            .infixr(operators.token("and").retn(And::new), And.precedence)
            .prefix(operators.token("not").retn(Not::new), Not.precedence)
            .infixr(operators.token("=").retn(Equal::new), Equal.precedence)
            .infixr(operators.token("<=").retn(LEqual::new), LEqual.precedence)
            .infixr(operators.token("+").retn(Addition::new), Addition.precedence)
            .infixn(operators.token("-").retn(Subtraction::new), Subtraction.precedence)
            .infixr(operators.token("*").retn(Multiplication::new), Multiplication.precedence)
            .build(typeTerm);

        termRef.set(parser);
        return parser;
    }

    private Parser<Constructor> constructorParser(Terminals operators) {
        return Parsers.sequence(
            Terminals.Identifier.PARSER,
            typeParser(operators).many(),
            (name, types) -> new Constructor(name, types.isEmpty() ? null : types.get(0))
        );
    }

    // This method has been corrected to handle the type ambiguity with the Constant class.
    private Parser<Type> typeParser(Terminals operators) {
        Parser.Reference<Type> typeRef = Parser.newReference();

        // The compiler was likely confusing the parser.Constant with the types.Constant class.
        // We now use the fully qualified name to be explicit.
        Parser<Type> atomType = Terminals.Identifier.PARSER.<Type>map(TVar::new)
            .or(operators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
            .or(operators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool")));

        Parser<Type> type = typeRef.lazy().between(operators.token("("), operators.token(")"))
            .or(atomType);

        Parser<Type> funcType = new OperatorTable<Type>()
            .infixr(operators.token("->").retn(FType::new), 10)
            .infixr(operators.token("x").retn(ProdType::new), 20)
            .build(type);

        typeRef.set(funcType);
        return typeRef.lazy();
    }

    public Term parse(CharSequence source) {
        return programParser()
            .from(progOperators.tokenizer().cast().or(
                        Terminals.Identifier.TOKENIZER)
                    .or(Terminals.IntegerLiteral.TOKENIZER),
                Scanners.WHITESPACES.skipMany())
            .parse(source);
    }

    private Term makeApplications(List<Term> l) {
        Term result = l.get(0);
        for(int i = 1; i < l.size(); i++) {
            result = new Application(result, l.get(i));
        }
        return result;
    }

    public static void main(String[] args) {
        ProgParser parser = new ProgParser();

        Term consTerm = parser.parse("data list a = emptylist | cons a (list a); cons 1 (cons 2 emptylist)");
        System.out.println("cons 1 (cons 2 emptylist): " + consTerm);
    }
}

 */