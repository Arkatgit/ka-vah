import ca.brock.cs.lambda.Abstraction;
import ca.brock.cs.lambda.Application;
import ca.brock.cs.lambda.Term;
import ca.brock.cs.lambda.Variable;

public class LambdaCalculus {
    public static void main(String[] args) {

        // Example of Lambda expression: (λx. x) y
        Term x = new Variable("x");
        Term identity = new Abstraction("x", x); // λx. x
        Term y = new Variable("y");

        Term application = new Application(identity, y); // (λx. x) y

        System.out.println(application.toString()); // Output: (λx. x y)
        System.out.println(new Abstraction("x",new Application(x,y)));
        System.out.println(new Application(new Application(x,y),x));
        System.out.println(new Application(x,new Application(y,x)));

    }
}