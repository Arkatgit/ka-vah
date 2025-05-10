package ca.brock.cs.lambda.types;

public class TypeError extends RuntimeException {
    public TypeError() {
        super();
    }

    public TypeError(String message) {
        super(message);
    }

    public TypeError(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeError(Throwable cause) {
        super(cause);
    }
}