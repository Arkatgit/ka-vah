package ca.brock.ca.interpreter;

// Function type (e.g., α → β)
public class FType extends Type {
    private Type input;
    private Type output;

    public FType(Type input, Type output) {
        this.input = input;
        this.output = output;
    }

    public Type getInput() {
        return input;
    }

    public Type getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "(" + input + " → " + output + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FType) {
            FType other = (FType) obj;
            return this.input.equals(other.input) && this.output.equals(other.output);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return input.hashCode() + output.hashCode();
    }
}