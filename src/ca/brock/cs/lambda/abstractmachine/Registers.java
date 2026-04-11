package ca.brock.cs.lambda.abstractmachine;

/**
 * x86-64 Registers used in code emission.
 */
public enum Registers {
    RAX, RBX, RCX, RDX, RSI, RDI, RBP, RSP,
    R8, R9, R10, R11, R12, R13, R14, R15;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}