package ca.brock.cs.lambda;

public class Application extends Term {
    private Term function;
    private Term argument;

    private static final int precedence = 20;

    public Application(Term function, Term argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public String toStringPrec(int prec) {
        String result =  function.toStringPrec(precedence) + " " + argument.toStringPrec(precedence+1);
        if (prec > precedence) {
            result = "(" + result + ")";
        }
        return result;
    }
}
