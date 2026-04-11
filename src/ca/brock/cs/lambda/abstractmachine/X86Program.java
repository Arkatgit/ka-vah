package ca.brock.cs.lambda.abstractmachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an entire x86-64 assembly program.
 */
public class X86Program {
    private final List<X86Instruction> instructions;
    private final List<String> dataSection;

    public X86Program() {
        this.instructions = new ArrayList<>();
        this.dataSection = new ArrayList<>();
    }

    public void addInstruction(X86Instruction instruction) {
        instructions.add(instruction);
    }

    public void addData(String label, String directive, String value) {
        dataSection.add(label + ": " + directive + " " + value);
    }

    public String emit() {
        StringBuilder sb = new StringBuilder();

        // Use Intel syntax for intuitive src/dest ordering (mov dest, src)
        sb.append(".intel_syntax noprefix\n\n");

        // ------------------------------------------------
        // BSS Section: Uninitialized Data (Graph Heap)
        // ------------------------------------------------
        sb.append(".section .bss\n");
        sb.append("    .lcomm HEAP, 1048576\n"); // Reserve 1MB for the heap
        sb.append("    .global heap_ptr\n\n");

        // ------------------------------------------------
        // Data Section: Initialized Data
        // ------------------------------------------------
        sb.append(".section .data\n");
        sb.append("    heap_ptr: .quad HEAP\n"); // Pointer to the next free heap byte
        for (String data : dataSection) {
            sb.append("    ").append(data).append("\n");
        }
        sb.append("\n");

        // ------------------------------------------------
        // Text Section: Executable Code
        // ------------------------------------------------
        sb.append(".section .text\n");
        sb.append(".global _start\n\n");

        // Peephole Optimization: remove adjacent `push rax` followed by `pop rax`
        List<X86Instruction> optimized = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            X86Instruction curr = instructions.get(i);
            if (i < instructions.size() - 1) {
                X86Instruction next = instructions.get(i + 1);
                if (curr.isPushRax() && next.isPopRax()) {
                    i++; // skip both instructions
                    continue;
                }
            }
            optimized.add(curr);
        }

        for (X86Instruction inst : optimized) {
            sb.append(inst.toString()).append("\n");
        }

        return sb.toString();
    }
}
//package ca.brock.cs.lambda.abstractmachine;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Represents an entire x86-64 assembly program.
// */
//public class X86Program {
//    private final List<X86Instruction> instructions;
//    private final List<String> dataSection;
//
//    public X86Program() {
//        this.instructions = new ArrayList<>();
//        this.dataSection = new ArrayList<>();
//    }
//
//    public void addInstruction(X86Instruction instruction) {
//        instructions.add(instruction);
//    }
//
//    public void addData(String label, String directive, String value) {
//        dataSection.add(label + ": " + directive + " " + value);
//    }
//
//    public String emit() {
//        StringBuilder sb = new StringBuilder();
//
//        // Data section
//        sb.append(".section .data\n");
//        for (String data : dataSection) {
//            sb.append("    ").append(data).append("\n");
//        }
//        sb.append("\n");
//
//        // Text section
//        sb.append(".section .text\n");
//        sb.append(".global _start\n\n");
//
//        for (X86Instruction inst : instructions) {
//            sb.append(inst.toString()).append("\n");
//        }
//
//        return sb.toString();
//    }
//}