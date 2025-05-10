package ca.brock.cs.lambda.parser;

import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;

import java.util.List;

public class ProgParser {

    private static final String[] SYMBOLS = { "(", ")","True","False","and","or","=","<=","not","+","-","*",".", "\u03BB", "rec", "if", "then", "else"};

    private static final Terminals progOperators = Terminals.operators(SYMBOLS);

    public Parser<Term> getParser(Terminals operators) {
        Parser.Reference<Term> ref = Parser.newReference();

        Parser<Term> term = ref.lazy().between(operators.token("("), operators.token(")"))
            .or(Terminals.Identifier.PARSER.map(Variable::new))
            .or(Parsers.sequence(operators.token("\u03BB"),Terminals.Identifier.PARSER,operators.token("."),ref.lazy(),
                (s1,s2,s3,t) -> new Abstraction(s2,t)))
            .or(operators.token("True").retn(new BooleanLiteral(true)))
            .or(operators.token("False").retn(new BooleanLiteral(false)))
            .or(Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))))
            .or(Parsers.sequence(operators.token("if"),ref.lazy(),operators.token("then"),ref.lazy(),operators.token("else"),ref.lazy(),
                (t1,p1,t2,p2,t3,p3) -> new Conditional(p1,p2,p3)))
            .or(Parsers.sequence(operators.token("rec"),Terminals.Identifier.PARSER,operators.token("."),ref.lazy(),
                (s1,s2,s3,t) -> new Recursion(s2,t)))

            // Handle standalone operator sections (*), (+), (-)
            .or(Parsers.sequence(operators.token("("), operators.token("*"), operators.token(")"),
                (open, op, close) -> new Constant("*")))
            .or(Parsers.sequence(operators.token("("), operators.token("+"), operators.token(")"),
                (open, op, close) -> new Constant("+")))
            .or(Parsers.sequence(operators.token("("), operators.token("-"), operators.token(")"),
                (open, op, close) -> new Constant("-")))

            // Handle right sections (* t), (+ t), (- t)) using flip
            .or(Parsers.sequence(operators.token("("), operators.token("*"), ref.lazy(), operators.token(")"),
                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("*")), r)))
            .or(Parsers.sequence(operators.token("("), operators.token("+"), ref.lazy(), operators.token(")"),
                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("+")), r)))
            .or(Parsers.sequence(operators.token("("), operators.token("-"), ref.lazy(), operators.token(")"),
                (open, op, r, close) -> new Application(new Application(new Constant("flip"), new Constant("-")), r)))


            // Handle left sections (t *), (t +), (t -)
            .or(Parsers.sequence(operators.token("("), ref.lazy(), operators.token("*"), operators.token(")"),
                (open, l, op, close) -> new Application(new Constant("*"), l)))
            .or(Parsers.sequence(operators.token("("), ref.lazy(), operators.token("+"), operators.token(")"),
                (open, l, op, close) -> new Application(new Constant("+"), l)))
            .or(Parsers.sequence(operators.token("("), ref.lazy(), operators.token("-"), operators.token(")"),
                (open, l, op, close) -> new Application(new Constant("-"), l)));

//            // Handle right sections (*) t1 t2, (+) t1 t2, (-)  t1 t2
//            .or(Parsers.sequence(operators.token("("), operators.token("*"), operators.token(")"), ref.lazy(), ref.lazy(),
//                (open, op, close, l, r) -> new Application(new Application(new Constant("*"), l), r)))
//            .or(Parsers.sequence(operators.token("("), operators.token("+"), operators.token(")"), ref.lazy(), ref.lazy(),
//                    (open, op, close, l, r) -> new Application(new Application(new Constant("+"), l), r)))
//            .or(Parsers.sequence(operators.token("("), operators.token("-"), operators.token(")"), ref.lazy(), ref.lazy(),
//                        (open, op, close, l, r) -> new Application(new Application(new Constant("-"), l), r)));


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

        ref.set(parser);
        return parser;
    }

    public Term parse(CharSequence source) {
        return getParser(progOperators)
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
        Term a = new ProgParser().parse("(t *)");
        System.out.println("(t *) :" + a);

        Term b = new ProgParser().parse("(* t)");
        System.out.println("(* t) : " + b); // Expected: Application(Application(Constant("flip"), Constant("*")), t)

        Term c = new ProgParser().parse("(t +)");
        System.out.println("(t +) :" + c); // Expected: Application(Constant("+"), t)

        Term d = new ProgParser().parse("(+ t)");
        System.out.println("(+ t) :" + d); // Expected: Application(Application(Constant("flip"), Constant("+")), t)

        Term e = new ProgParser().parse("t1 * t2");
        System.out.println("t1 * t2 :" +  e); // Expected: Multiplication(t1, t2)

        Term f = new ProgParser().parse("t1 + t2");
        System.out.println("t1 + t2 : " + f); // Expected: Addition(t1, t2)

        Term g = new ProgParser().parse("(+) 4 5");
        System.out.println("(+) 4 5 : " + g); // Expected: Subtraction(t1, t2)
    }
}
/*
package ca.brock.cs.lambda;

import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;

import java.util.List;

public class ProgParser {

    private static final String[] SYMBOLS = { "(", ")","True","False","and","or","=","<=","not","+","-","*",".", "\u03BB", "rec", "if", "then", "else"};

    private static final Terminals progOperators = Terminals.operators(SYMBOLS);

    public Parser<Term> getParser(Terminals operators) {
        Parser.Reference<Term> ref = Parser.newReference();

        // Base cases (non-recursive)
        Parser<Term> atom = Parsers.or(
            Terminals.Identifier.PARSER.map(Variable::new),
            operators.token("True").retn(new BooleanLiteral(true)),
            operators.token("False").retn(new BooleanLiteral(false)),
            Terminals.IntegerLiteral.PARSER.map(s -> new IntegerLiteral(Integer.valueOf(s))),
            operators.token("*").retn(new Constant("*")),
            operators.token("+").retn(new Constant("+")),
            operators.token("-").retn(new Constant("-"))
        );

        // Recursive cases (no left recursion)
        Parser<Term> term = Parsers.or(
            atom,
            ref.lazy().between(operators.token("("), operators.token(")")),
            Parsers.sequence(operators.token("\u03BB"), Terminals.Identifier.PARSER, operators.token("."), ref.lazy(),
                (s1, s2, s3, t) -> new Abstraction(s2, t)),
            Parsers.sequence(operators.token("if"), ref.lazy(), operators.token("then"), ref.lazy(), operators.token("else"), ref.lazy(),
                (t1, p1, t2, p2, t3, p3) -> new Conditional(p1, p2, p3)),
            Parsers.sequence(operators.token("rec"), Terminals.Identifier.PARSER, operators.token("."), ref.lazy(),
                (s1, s2, s3, t) -> new Recursion(s2, t)),
            Parsers.sequence(operators.token("*"), ref.lazy(),
                (op, r) -> new Application(new Constant("*"), r)), // Handle (* t)
            Parsers.sequence(operators.token("+"), ref.lazy(),
                (op, r) -> new Application(new Constant("+"), r)), // Handle (+ t)
            Parsers.sequence(operators.token("-"), ref.lazy(),
                (op, r) -> new Application(new Constant("-"), r)) // Handle (- t)
        );

        // Combine terms into applications
        Parser<Term> typeTerm = term.many1().map(this::makeApplications);

        // Operator table for infix and prefix operations
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

        ref.set(parser);
        return parser;
    }

    public Term parse(CharSequence source) {
        return getParser(progOperators)
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
        Term a = new ProgParser().parse("(t *)");
        System.out.println("(t *) : " + a); // Expected: Application(Constant("*"), t)

        Term b = new ProgParser().parse("(* t)");
        System.out.println("(* t) : " + b); // Expected: Application(Constant("*"), t)

        Term c = new ProgParser().parse("(t +)");
        System.out.println("(t +) : " + c); // Expected: Application(Constant("+"), t)

        Term d = new ProgParser().parse("(+ t)");
        System.out.println("(+ t) : " + d); // Expected: Application(Constant("+"), t)

        Term e = new ProgParser().parse("t1 * t2");
        System.out.println("t1 * t2 : " + e); // Expected: Multiplication(t1, t2)

        Term f = new ProgParser().parse("(*) t1 t2");
        System.out.println("(*) t1 t2 : " + f); // Expected: Application(Application(Constant("*"), t1), t2)

        Term q = new ProgParser().parse("t1 + t2");
        System.out.println("t1 + t2" + q); // Expected: Addition(t1, t2)

        Term g = new ProgParser().parse("t1 - t2");
        System.out.println("t1 - t2 : " + g); // Expected: Subtraction(t1, t2)
    }
}
*/