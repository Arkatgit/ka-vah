//package ca.brock.cs.lambda.abstractmachine;
//
///**
// * Defines the supported x86-64 instruction mnemonics for the abstract machine.
// */
//public enum OpCodes {
//    // Data transfer
//    MOV, PUSH, POP, LEA,
//
//    // Arithmetic
//    ADD, SUB, IMUL, IDIV, INC, DEC,
//
//    // Bitwise / Logic
//    AND, OR, XOR, NOT, CMP, TEST,
//
//    // Control flow
//    JMP, JE, JNE, JG, JGE, JL, JLE,
//    CALL, RET,
//
//    // Misc
//    NOP, SYSCALL
//}

package ca.brock.cs.lambda.abstractmachine;

/**
 * Defines the supported x86-64 instruction mnemonics for the abstract machine.
 */
public enum OpCodes {
    MOV, PUSH, POP, LEA,
    ADD, SUB, IMUL, IDIV,  CQO, INC, DEC, DIV,
    XOR, OR, AND, SHL,SHR, SAR, TEST,       // Bitwise operations for Tagging
    CALL, RET, SYSCALL, JMP,
    CMP, JNE, JE, JL, JG, JLE, JGE, JNZ // Branching logic
}