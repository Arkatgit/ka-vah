package ca.brock.cs.lambda.parser;

import ca.brock.cs.lambda.logging.AppLogger;
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
        "+", "-", "*", "/", ".", "\u03BB", "rec", "if", "then", "else",
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
     *  declaration  = dataDecl | typeSigDecl | functionBodyDecl
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


    /**
     * Parses a data type declaration and adds the constructors to the symbol map.
     * dataDecl  = "data", IDENTIFIER, { IDENTIFIER }, "=", constructor, { "|", constructor }, ";"
     */
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
            constructorArgTypeParser().many(),
            ConstructorData::new
        );
    }

    /**
     * Parses a single constructor argument's type, which can be a simple type like `Int`
     * or a type application like `(list a)`.
     * It does not parse function types (`->`).
     */
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
     * typeSigDecl    = IDENTIFIER, ":", type, ";"
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
     * functionBodyDecl = IDENTIFIER, "=", term, ";"
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
                AppLogger.info("DEBUG: Parsing function " + name + " with term: " + term);
                Type type = signatureMap.remove(name);
                // Creates a FunctionDefinition with the found type or null if no signature was present
                symbolMap.put(name, new FunctionDefinition(name, type, term));
                return null;
            }
        );
    }

    /**
     * Gets the term parser, which is the core of the expression parsing.
     * term           = application
     * application    = { expression }+
     * expression     = "(", term, ")"
     *                | atom
     *                | lambda
     *                | conditional
     *                | recursion
     *                | matchExpression
     *
     * lambda         = "λ", IDENTIFIER, ".", term
     * conditional    = "if", term, "then", term, "else", term
     * recursion      = "rec", IDENTIFIER, ".", term
     * matchExpression = "match", term, "with", matchCase, { "|", matchCase }, "end"
     *
     * atom           = IDENTIFIER
     *                | BOOLEAN
     *                | INTEGER
     *                | "(", operator, ")"
     */
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
            for (int i = 1; i < terms.size(); i++) {
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
     * <p>
     * Parses a pattern, which can be:
     * - Constants: True, False, integer literals
     * - Variables: identifiers
     * - Constructors: constructorName followed by patterns
     * - Parenthesized patterns: (pattern)
     */
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
                AppLogger.info("DEBUG: Parsing constructor pattern: " + name + " with " + patterns.size() + " arguments");
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
     * type           = funcType
     * funcType       = typeApplication, { "->", typeApplication }
     * typeApplication = termType, { termType }
     * termType       = IDENTIFIER | "Int" | "Bool" | "(", type, ")"
     */
    private static Parser<Type> typeParser() {
        Parser.Reference<Type> typeRef = Parser.newReference();

        Parser<Type> termType = Parsers.or(
            Terminals.Identifier.PARSER.<Type>map(TVar::new)
                .or(progOperators.token("Int").retn(new ca.brock.cs.lambda.types.Constant("Int")))
                .or(progOperators.token("Bool").retn(new ca.brock.cs.lambda.types.Constant("Bool"))),
            typeRef.lazy().between(progOperators.token("("), progOperators.token(")"))
        );

        // Type application with dual convention approach
        Parser<Type> typeApplication = termType.many1().map(types -> {
            if (types.size() == 1) return types.get(0);

            if (types.get(0) instanceof TVar) {
                TVar first = (TVar) types.get(0);
                String name = first.getName();

                // Convention 1: Uppercase = multi-parameter type constructor (any number of args)
                if (Character.isUpperCase(name.charAt(0))) {
                    return new AlgebraicDataType(name, types.subList(1, types.size()), null);
                }

                // Convention 2: Lowercase with exactly 2 elements = unary type constructor like "list a"
                if (types.size() == 2) {
                    return new AlgebraicDataType(name, List.of(types.get(1)), null);
                }
            }

            // Default case: regular type application for other cases
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


    /**
     * Parses the entire program, which is a series of declarations, and validates
     * that a "main" function is defined.
     * program  = { declaration } functionBody
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
        for (int i = 1; i < l.size(); i++) {
            result = new Application(result, l.get(i));
        }
        return result;
    }


    /**
     * Helper function to parse a program, set up the environment, and check all function types.
     *
     * @param programString The program code to parse and check.
     */
    private static void checkAndPrintTypes(String programString) {
        Map<String, DefinedValue> symbolMap = new HashMap<>();
        ParsedProgram program = parse(programString, symbolMap);

        // First, populate the environment with all constructors and function signatures
        Map<String, Type> baseEnv = new HashMap<>();
        AppLogger.info("Symbol map contents:");
        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            System.out.println("  " + entry.getKey() + " : " +
                (entry.getValue() instanceof Constructor ?
                    ((Constructor) entry.getValue()).getType() :
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

            // Test 10:
            "data list a = emptylist | cons a (list a);\n" +
                "badFunction : list Int -> Int;\n" +
                "badFunction = λxs. match xs with\n" +
                "    emptylist -> 0\n" +
                "    | cons x xs_tail -> x + badFunction xs_tail\n" +
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

}