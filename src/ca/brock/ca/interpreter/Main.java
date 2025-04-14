package ca.brock.ca.interpreter;

public class Main {
    public static void main(String[] args) {
        // Define some types
        TVar alpha = new TVar("α");
        TVar beta = new TVar("β");
        FType funcType1 = new FType(alpha, beta);
        FType funcType2 = new FType(new Constant("Int"), alpha);

        // Create a unifier
        Unifier unifier = new Unifier();

        // Unify the types
        boolean result = unifier.unify(funcType1, funcType2);

        // Print the result
        if (result) {
            System.out.println("Unification succeeded!");
            System.out.println("Substitution: " + unifier.getEnv());
        } else {
            System.out.println("Unification failed.");
        }
    }
}