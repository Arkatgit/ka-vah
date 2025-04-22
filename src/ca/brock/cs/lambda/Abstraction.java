package ca.brock.cs.lambda;

public class Abstraction extends Term {
    private String parameter;
    private Term body;

    private static final int precedence = 10;

    public Abstraction(String parameter, Term body) {
        this.parameter = parameter;
        this.body = body;
    }

    public String getParameter() {
        return parameter;
    }

    public Term getBody() {
        return body;
    }

    @Override
    public String toStringPrec(int prec) {
        String result =  "λ" + parameter + ". " + body.toStringPrec(precedence);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }
}