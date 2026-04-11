package ca.brock.cs.lambda.abstractmachine;

/**
 * Defines the supported x86-64 instruction mnemonics for the abstract machine.
 */
public enum OpCodes {
    // Data transfer
    MOV, PUSH, POP, LEA,

    // Arithmetic
    ADD, SUB, IMUL, IDIV, INC, DEC,

    // Bitwise / Logic
    AND, OR, XOR, NOT, CMP, TEST,

    // Control flow
    JMP, JE, JNE, JG, JGE, JL, JLE,
    CALL, RET,

    // Misc
    NOP, SYSCALL
}