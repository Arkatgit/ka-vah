//package ca.brock.cs.lambda.abstractmachine;
//
//import ca.brock.cs.lambda.combinators.*;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Compiles a partially evaluated combinator tree into x86-64 assembly.
// * Adapts memory allocation and graph reduction semantics dynamically.
// */
//public class X86Emitter {
//
//    private final X86Program program;
//    private final Set<String> definedFunctions;
//    private final Set<String> externalVariables;
//
//    public X86Emitter() {
//        this.program = new X86Program();
//        this.definedFunctions = new HashSet<>();
//        this.externalVariables = new HashSet<>();
//    }
//
//    public X86Program compile(Map<String, Combinator> globals, String entryPointName) {
//        for (String key : globals.keySet()) {
//            definedFunctions.add(sanitizeLabel(key));
//        }
//
//        program.addInstruction(new X86Instruction("_start"));
//
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RBP.toString()));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RBP.toString(), Registers.RSP.toString()));
//
//        String mainLabel = sanitizeLabel(entryPointName);
//        program.addInstruction(new X86Instruction(OpCodes.CALL, mainLabel));
//
//        // Result left in RAX
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), "60")); // sys_exit
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RDI.toString(), "0"));  // status 0
//        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));
//
//        for (Map.Entry<String, Combinator> entry : globals.entrySet()) {
//            String label = sanitizeLabel(entry.getKey());
//            program.addInstruction(new X86Instruction(label));
//
//            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.R15.toString())); // Save ret addr
//
//            emitTerm(entry.getValue());
//
//            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.RAX.toString())); // Result to rax
//
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.R15.toString())); // Restore ret addr
//            program.addInstruction(new X86Instruction(OpCodes.RET));
//        }
//
//        for (String ext : externalVariables) {
//            if (!definedFunctions.contains(ext)) {
//                program.addInstruction(new X86Instruction(ext));
//                // Load its own label address as a tag for uninterpreted constructors
//                program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + ext + "]"));
//                program.addInstruction(new X86Instruction(OpCodes.RET));
//            }
//        }
//
//        emitCombinatorRuntime();
//
//        return program;
//    }
//
//    private void emitTerm(Combinator term) {
//        if (term instanceof CombinatorConstant) {
//            CombinatorConstant constant = (CombinatorConstant) term;
//            Object val = constant.getValue();
//
//            if (val instanceof Integer) {
//                // Immediate numeric value
//                program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), String.valueOf(val)));
//            } else {
//                // String constants (Operators like '*', '+', 'IF')
//                String label = sanitizeLabel(String.valueOf(val));
//                externalVariables.add(label);
//                program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            }
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else if (term instanceof CombinatorVariable) {
//            CombinatorVariable var = (CombinatorVariable) term;
//            String label = sanitizeLabel(var.getName());
//
//            if (!definedFunctions.contains(label)) {
//                externalVariables.add(label);
//            }
//
//            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else if (term instanceof CombinatorApplication) {
//            CombinatorApplication app = (CombinatorApplication) term;
//
//            emitTerm(app.getArgument());
//            emitTerm(app.getFunction());
//
//            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.RAX.toString()));
//            program.addInstruction(new X86Instruction(OpCodes.CALL, Registers.RAX.toString()));
//
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else if (term instanceof SCombinator || term instanceof KCombinator || term instanceof ICombinator ||
//            term instanceof BCombinator || term instanceof CCombinator || term instanceof WCombinator ||
//            term instanceof YCombinator || term instanceof CStarCombinator) {
//            String label = getCombinatorLabel(term);
//            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else {
//            String label = sanitizeLabel(term.getClass().getSimpleName());
//            externalVariables.add(label);
//            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//    }
//
//    private String sanitizeLabel(String name) {
//        if (name == null) return "lbl_null";
//        String sanitized = name
//            .replace("+", "plus")
//            .replace("-", "minus")
//            .replace("*", "mul")
//            .replace("/", "div")
//            .replace("=", "eq")
//            .replace("<=", "le")
//            .replace(">=", "ge")
//            .replace("<", "lt")
//            .replace(">", "gt")
//            .replaceAll("[^a-zA-Z0-9_]", "_");
//        return "lbl_" + sanitized;
//    }
//
//    private String getCombinatorLabel(Combinator term) {
//        if (term instanceof SCombinator) return "comb_S";
//        if (term instanceof KCombinator) return "comb_K";
//        if (term instanceof ICombinator) return "comb_I";
//        if (term instanceof BCombinator) return "comb_B";
//        if (term instanceof CCombinator) return "comb_C";
//        if (term instanceof WCombinator) return "comb_W";
//        if (term instanceof YCombinator) return "comb_Y";
//        if (term instanceof CStarCombinator) return "comb_CStar";
//        return "unknown_comb";
//    }
//
//    /**
//     * Emits the combinator runtime and the dynamic closure allocator.
//     */
//    private void emitCombinatorRuntime() {
//        // -----------------------------------------------------------------
//        // HEAP ALLOCATOR (Equivalent to FCompiler MemoryCalculator)
//        // Allocates 16 bytes. Returns pointer in RAX.
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[heap_ptr]"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r15", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "r15", "16"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[heap_ptr]", "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // I Combinator: I x = x
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_I"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15")); // ret addr
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax")); // x
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // K Combinator: K x y = x
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_K"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax")); // x
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // y (discard)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // B Combinator: B x y z = x (y z)
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_B"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // z
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node")); // Alloc (y z)
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rax")); // push (y z)
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx")); // load x
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval x(y z)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // C Combinator: C x y z = x z y
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_C"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // z
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rdx")); // push z
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push y
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)(y)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // S Combinator: S x y z = x z (y z)
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_S"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // z
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node")); // Alloc (y z)
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax")); // r8 = pointer to (y z)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rdx")); // push z
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r8")); // push (y z)
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)(y z)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // W Combinator: W x y = x y y
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_W"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push y
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval x(y)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push y
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x y)(y)
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // Y Combinator: Y f = f (Y f)  [Creates Cyclic Graph]
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_Y"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // f
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx")); // Left side = f
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rax")); // Right side points to ITSELF!
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rax")); // push cyclic node
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // Call f with the cyclic graph
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // C* (CStar) Combinator: C* p q r s = p q s r
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_CStar"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15")); // ret addr
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // p
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // q
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // r
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r8"));  // s
//
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push q
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (p q)
//
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r8"));  // push s
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (p q s)
//
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rdx")); // push r
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (p q s r)
//
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//    }
//}

//package ca.brock.cs.lambda.abstractmachine;
//
//import ca.brock.cs.lambda.combinators.*;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Compiles a combinator tree into x86-64 assembly using Graph Reduction.
// * Implements a Tagged Evaluator Loop for Lazy/Normal-Order execution.
// */
//public class X86Emitter {
//
//    private final X86Program program;
//    private final Set<String> definedFunctions;
//    private final Set<String> externalVariables;
//
//    public X86Emitter() {
//        this.program = new X86Program();
//        this.definedFunctions = new HashSet<>();
//        this.externalVariables = new HashSet<>();
//    }
//
//    public X86Program compile(Map<String, Combinator> globals, String entryPointName) {
//        for (String key : globals.keySet()) {
//            definedFunctions.add(sanitizeLabel(key));
//        }
//
//        program.addInstruction(new X86Instruction("_start"));
//
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RBP.toString()));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RBP.toString(), Registers.RSP.toString()));
//
//        // Push the tagged pointer for the main function graph
//        String mainLabel = sanitizeLabel(entryPointName);
//        program.addInstruction(new X86Instruction(OpCodes.LEA, "rax", "[" + mainLabel + "]"));
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "2"));
//
//        // ---------------------------------------------------------
//        // GUARANTEED REDUCTION LOOP
//        // Keep evaluating RAX until it resolves to a tagged integer
//        // ---------------------------------------------------------
//        program.addInstruction(new X86Instruction(".force_eval_loop"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "1")); // Is LSB 1? (Integer check)
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".force_eval_done")); // Yes, it's an int, break loop
//        // It evaluated to a function or thunk, loop again to force it
//        program.addInstruction(new X86Instruction(OpCodes.JMP, ".force_eval_loop"));
//
//        program.addInstruction(new X86Instruction(".force_eval_done"));
//        // ---------------------------------------------------------
//
//        // Result left in RAX, print it
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_print_int"));
//
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), "60")); // sys_exit
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RDI.toString(), "0"));
//        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));
//
//        for (Map.Entry<String, Combinator> entry : globals.entrySet()) {
//            String label = sanitizeLabel(entry.getKey());
//            program.addInstruction(new X86Instruction(label));
//            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.R15.toString()));
//
//            emitTerm(entry.getValue());
//
//            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.RAX.toString()));
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.R15.toString()));
//            program.addInstruction(new X86Instruction(OpCodes.RET));
//        }
//
//        emitCombinatorRuntime();
//        emitNativeOperators();
//        emitPrintIntRoutine();
//
//        return program;
//    }
//
//    private void emitTerm(Combinator term) {
//        if (term instanceof CombinatorConstant) {
//            CombinatorConstant constant = (CombinatorConstant) term;
//            Object val = constant.getValue();
//
//            // AGGRESSIVE TAGGING: Catch Integers even if they are stored as Strings
//            boolean isNumber = false;
//            long numVal = 0;
//
//            if (val instanceof Integer || val instanceof Long) {
//                isNumber = true;
//                numVal = ((Number) val).longValue();
//            } else {
//                try {
//                    numVal = Long.parseLong(String.valueOf(val));
//                    isNumber = true;
//                } catch (NumberFormatException e) {
//                    // It's a constructor or uninterpreted function string, not a number
//                }
//            }
//
//            if (isNumber) {
//                // TAGGING: Shift left 1, OR with 1. (e.g. 0 -> 1, 1 -> 3, 42 -> 85)
//                long taggedVal = (numVal << 1) | 1;
//                program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), String.valueOf(taggedVal)));
//            } else {
//                String label = sanitizeLabel(String.valueOf(val));
//                externalVariables.add(label);
//                program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//                program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
//            }
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else if (term instanceof CombinatorVariable) {
//            CombinatorVariable var = (CombinatorVariable) term;
//            String label = sanitizeLabel(var.getName());
//            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else if (term instanceof CombinatorApplication) {
//            CombinatorApplication app = (CombinatorApplication) term;
//
//            emitTerm(app.getArgument());
//            emitTerm(app.getFunction());
//
//            // GRAPH REDUCTION: Allocate a heap node [Function, Argument]
//            program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//            program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//            program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//            program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//            program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else {
//            String label = getCombinatorLabel(term);
//            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//    }
//
//    private String sanitizeLabel(String name) {
//        if (name == null) return "lbl_null";
//        String sanitized = name
//            .replace("+", "plus")
//            .replace("-", "minus")
//            .replace("*", "mul")
//            .replace("<=", "lteq")
//            .replaceAll("[^a-zA-Z0-9_]", "_");
//        return "lbl_" + sanitized;
//    }
//
//    private String getCombinatorLabel(Combinator term) {
//        if (term instanceof SCombinator) return "comb_S";
//        if (term instanceof KCombinator) return "comb_K";
//        if (term instanceof ICombinator) return "comb_I";
//        if (term instanceof BCombinator) return "comb_B";
//        if (term instanceof CCombinator) return "comb_C";
//        if (term instanceof WCombinator) return "comb_W";
//        if (term instanceof YCombinator) return "comb_Y";
//        if (term instanceof CStarCombinator) return "comb_CStar";
//        return "unknown_comb";
//    }
//
//    private void emitPrintIntRoutine() {
//        program.addInstruction(new X86Instruction("lbl_print_int"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbp"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbp", "rsp"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r12"));
//
//        // UNTAG BEFORE PRINTING
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rax", "1"));
//
//        program.addInstruction(new X86Instruction(OpCodes.SUB, "rsp", "32"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "0"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "10"));
//        program.addInstruction(new X86Instruction(OpCodes.LEA, "r12", "[rsp+31]"));
//
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "byte ptr [r12]", "10"));
//        program.addInstruction(new X86Instruction(OpCodes.DEC, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.INC, "rcx"));
//
//        program.addInstruction(new X86Instruction(".print_loop"));
//        program.addInstruction(new X86Instruction(OpCodes.XOR, "rdx", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.DIV, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "dl", "'0'"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[r12]", "dl"));
//        program.addInstruction(new X86Instruction(OpCodes.DEC, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.INC, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CMP, "rax", "0"));
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".print_loop"));
//
//        program.addInstruction(new X86Instruction(OpCodes.INC, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdi", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rsi", "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdx", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));
//
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "rsp", "32"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbp"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//    }
//
//    private void emitCombinatorRuntime() {
//        // -----------------------------------------------------------------
//        // THE EVALUATOR UNWINDER
//        // Core engine of the Graph Reduction machine. Unwinds applications.
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".eval_done")); // It's an integer value
//
//        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "2"));
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".eval_func")); // It's a function pointer
//
//        // It's a Heap Node. Unwind the spine: Push argument, evaluate function
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "[rax+8]"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[rax]"));
//        program.addInstruction(new X86Instruction(OpCodes.JMP, "lbl_eval"));
//
//        program.addInstruction(new X86Instruction(".eval_func"));
//        program.addInstruction(new X86Instruction(OpCodes.AND, "rax", "-3")); // Clear function tag bit
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax"));      // Call combinator/primitive
//        program.addInstruction(new X86Instruction(OpCodes.JMP, "lbl_eval"));  // Evaluate the resulting graph
//
//        program.addInstruction(new X86Instruction(".eval_done"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // HEAP ALLOCATOR
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[heap_ptr]"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r15", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "r15", "16"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[heap_ptr]", "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // COMBINATORS (Build graphs lazily instead of executing strictly)
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction("comb_I"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction("comb_K"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction("comb_B"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction("comb_C"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // --- ADDED C* COMBINATOR ---
//        program.addInstruction(new X86Instruction("comb_CStar"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // arg 1 (a)
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // arg 2 (b)
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx")); // left child = b
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rbx")); // right child = a (resulting in 'b a')
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction("comb_S"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r9", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r9"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction("comb_Y"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rax")); // Cyclic graph
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction("comb_W"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//    }
//
//    private void emitNativeOperators() {
//        // NATIVE ADDITION (+)
//        program.addInstruction(new X86Instruction("lbl_plus"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Arg 1
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Arg 2
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1")); // Untag
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1")); // Untag
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1")); // Retag
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE MULTIPLICATION (*)
//        program.addInstruction(new X86Instruction("lbl_mul"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.IMUL, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE SUBTRACTION (-)
//        program.addInstruction(new X86Instruction("lbl_minus"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.SUB, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE LESS-THAN-EQUAL (<=)
//        program.addInstruction(new X86Instruction("lbl_lteq"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CMP, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.JLE, ".is_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "1"));   // False (Tagged 0)
//        program.addInstruction(new X86Instruction(OpCodes.JMP, ".cmp_done"));
//        program.addInstruction(new X86Instruction(".is_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "3"));   // True (Tagged 1)
//        program.addInstruction(new X86Instruction(".cmp_done"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE CONDITIONAL BRANCH (IF)
//        program.addInstruction(new X86Instruction("lbl_IF"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax")); // cond
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // true_branch
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // false_branch
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Condition
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CMP, "rax", "3")); // 3 is tagged True
//        program.addInstruction(new X86Instruction(OpCodes.JE, ".do_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx")); // Select False Graph
//        program.addInstruction(new X86Instruction(OpCodes.JMP, ".if_done"));
//        program.addInstruction(new X86Instruction(".do_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx")); // Select True Graph
//        program.addInstruction(new X86Instruction(".if_done"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET)); // Return graph pointer to unwinder
//    }
//}

//package ca.brock.cs.lambda.abstractmachine;
//
//import ca.brock.cs.lambda.combinators.*;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Compiles a combinator tree into x86-64 assembly using Graph Reduction.
// * Implements a Tagged Evaluator Loop for Lazy/Normal-Order execution.
// */
//public class X86Emitter {
//
//    private final X86Program program;
//    private final Set<String> definedFunctions;
//    private final Set<String> externalVariables;
//
//    public X86Emitter() {
//        this.program = new X86Program();
//        this.definedFunctions = new HashSet<>();
//        this.externalVariables = new HashSet<>();
//    }
//
//    public X86Program compile(Map<String, Combinator> globals, String entryPointName) {
//        for (String key : globals.keySet()) {
//            definedFunctions.add(sanitizeLabel(key));
//        }
//
//        program.addInstruction(new X86Instruction(".p2align 3\n_start"));
//
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RBP.toString()));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RBP.toString(), Registers.RSP.toString()));
//
//        // Push the tagged pointer for the main function graph
//        String mainLabel = sanitizeLabel(entryPointName);
//        program.addInstruction(new X86Instruction(OpCodes.LEA, "rax", "[" + mainLabel + "]"));
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "2"));
//
//        // ---------------------------------------------------------
//        // GUARANTEED REDUCTION LOOP
//        // Keep evaluating RAX until it resolves to a tagged integer
//        // ---------------------------------------------------------
//        program.addInstruction(new X86Instruction(".force_eval_loop"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "1")); // Is LSB 1? (Integer check)
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".force_eval_done")); // Yes, it's an int, break loop
//        // It evaluated to a function or thunk, loop again to force it
//        program.addInstruction(new X86Instruction(OpCodes.JMP, ".force_eval_loop"));
//
//        program.addInstruction(new X86Instruction(".force_eval_done"));
//        // ---------------------------------------------------------
//
//        // Result left in RAX, print it
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_print_int"));
//
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), "60")); // sys_exit
//        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RDI.toString(), "0"));
//        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));
//
//        for (Map.Entry<String, Combinator> entry : globals.entrySet()) {
//            String label = sanitizeLabel(entry.getKey());
//            // ALIGNMENT FIX: Guarantee 8-byte alignment for all generated user functions
//            // By embedding a newline, we bypass the automatic colon append on directives.
//            program.addInstruction(new X86Instruction(".p2align 3\n" + label));
//            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.R15.toString()));
//
//            emitTerm(entry.getValue());
//
//            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.RAX.toString()));
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.R15.toString()));
//            program.addInstruction(new X86Instruction(OpCodes.RET));
//        }
//
//        emitCombinatorRuntime();
//        emitNativeOperators();
//        emitPrintIntRoutine();
//
//        return program;
//    }
//
//    private void emitTerm(Combinator term) {
//        if (term instanceof CombinatorConstant) {
//            CombinatorConstant constant = (CombinatorConstant) term;
//            Object val = constant.getValue();
//
//            // AGGRESSIVE TAGGING: Catch Integers even if they are stored as Strings
//            boolean isNumber = false;
//            long numVal = 0;
//
//            if (val instanceof Integer || val instanceof Long) {
//                isNumber = true;
//                numVal = ((Number) val).longValue();
//            } else {
//                try {
//                    numVal = Long.parseLong(String.valueOf(val));
//                    isNumber = true;
//                } catch (NumberFormatException e) {
//                    // It's a constructor or uninterpreted function string, not a number
//                }
//            }
//
//            if (isNumber) {
//                // TAGGING: Shift left 1, OR with 1. (e.g. 0 -> 1, 1 -> 3, 42 -> 85)
//                long taggedVal = (numVal << 1) | 1;
//                program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), String.valueOf(taggedVal)));
//            } else {
//                String label = sanitizeLabel(String.valueOf(val));
//                externalVariables.add(label);
//                program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//                program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
//            }
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else if (term instanceof CombinatorVariable) {
//            CombinatorVariable var = (CombinatorVariable) term;
//            String label = sanitizeLabel(var.getName());
//            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else if (term instanceof CombinatorApplication) {
//            CombinatorApplication app = (CombinatorApplication) term;
//
//            emitTerm(app.getArgument());
//            emitTerm(app.getFunction());
//
//            // GRAPH REDUCTION: Allocate a heap node [Function, Argument]
//            program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//            program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//            program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//            program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//            program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//        else {
//            String label = getCombinatorLabel(term);
//            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
//            program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
//            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
//        }
//    }
//
//    private String sanitizeLabel(String name) {
//        if (name == null) return "lbl_null";
//        String sanitized = name
//            .replace("+", "plus")
//            .replace("-", "minus")
//            .replace("*", "mul")
//            .replace("<=", "lteq")
//            .replaceAll("[^a-zA-Z0-9_]", "_");
//        return "lbl_" + sanitized;
//    }
//
//    private String getCombinatorLabel(Combinator term) {
//        if (term instanceof SCombinator) return "comb_S";
//        if (term instanceof KCombinator) return "comb_K";
//        if (term instanceof ICombinator) return "comb_I";
//        if (term instanceof BCombinator) return "comb_B";
//        if (term instanceof CCombinator) return "comb_C";
//        if (term instanceof WCombinator) return "comb_W";
//        if (term instanceof YCombinator) return "comb_Y";
//        if (term instanceof CStarCombinator) return "comb_CStar";
//        return "unknown_comb";
//    }
//
//    private void emitPrintIntRoutine() {
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_print_int"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbp"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbp", "rsp"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r12"));
//
//        // UNTAG BEFORE PRINTING
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rax", "1"));
//
//        program.addInstruction(new X86Instruction(OpCodes.SUB, "rsp", "32"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "0"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "10"));
//        program.addInstruction(new X86Instruction(OpCodes.LEA, "r12", "[rsp+31]"));
//
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "byte ptr [r12]", "10"));
//        program.addInstruction(new X86Instruction(OpCodes.DEC, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.INC, "rcx"));
//
//        program.addInstruction(new X86Instruction(".print_loop"));
//        program.addInstruction(new X86Instruction(OpCodes.XOR, "rdx", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.DIV, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "dl", "'0'"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[r12]", "dl"));
//        program.addInstruction(new X86Instruction(OpCodes.DEC, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.INC, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CMP, "rax", "0"));
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".print_loop"));
//
//        program.addInstruction(new X86Instruction(OpCodes.INC, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdi", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rsi", "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdx", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));
//
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "rsp", "32"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r12"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbp"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//    }
//
//    private void emitCombinatorRuntime() {
//        // -----------------------------------------------------------------
//        // THE EVALUATOR UNWINDER
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".eval_done")); // It's an integer value
//
//        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "2"));
//        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".eval_func")); // It's a function pointer
//
//        // It's a Heap Node. Unwind the spine: Push argument, evaluate function
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "[rax+8]"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[rax]"));
//        program.addInstruction(new X86Instruction(OpCodes.JMP, "lbl_eval"));
//
//        program.addInstruction(new X86Instruction(".eval_func"));
//        program.addInstruction(new X86Instruction(OpCodes.AND, "rax", "-3")); // Clear function tag bit
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax"));      // Call combinator/primitive
//        program.addInstruction(new X86Instruction(OpCodes.JMP, "lbl_eval"));  // Evaluate the resulting graph
//
//        program.addInstruction(new X86Instruction(".eval_done"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // HEAP ALLOCATOR
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[heap_ptr]"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r15", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "r15", "16"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[heap_ptr]", "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // -----------------------------------------------------------------
//        // COMBINATORS
//        // -----------------------------------------------------------------
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_I"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_K"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_B"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_C"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_CStar"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // arg 1 (a)
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // arg 2 (b)
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx")); // left child = b
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rbx")); // right child = a (resulting in 'b a')
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_S"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r9", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r9"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_Y"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rax")); // Cyclic graph
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        program.addInstruction(new X86Instruction(".p2align 3\ncomb_W"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r8"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//    }
//
//    private void emitNativeOperators() {
//        // NATIVE ADDITION (+)
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_plus"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Arg 1
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Arg 2
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1")); // Untag
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1")); // Untag
//        program.addInstruction(new X86Instruction(OpCodes.ADD, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1")); // Retag
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE MULTIPLICATION (*)
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_mul"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.IMUL, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE SUBTRACTION (-)
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_minus"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.SUB, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE LESS-THAN-EQUAL (<=)
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_lteq"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
//        program.addInstruction(new X86Instruction(OpCodes.CMP, "rbx", "rcx")); // Execute
//        program.addInstruction(new X86Instruction(OpCodes.JLE, ".is_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "1"));   // False (Tagged 0)
//        program.addInstruction(new X86Instruction(OpCodes.JMP, ".cmp_done"));
//        program.addInstruction(new X86Instruction(".is_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "3"));   // True (Tagged 1)
//        program.addInstruction(new X86Instruction(".cmp_done"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//
//        // NATIVE CONDITIONAL BRANCH (IF)
//        program.addInstruction(new X86Instruction(".p2align 3\nlbl_IF"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rax")); // cond
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // true_branch
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // false_branch
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Condition
//        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
//        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
//        program.addInstruction(new X86Instruction(OpCodes.CMP, "rax", "3")); // 3 is tagged True
//        program.addInstruction(new X86Instruction(OpCodes.JE, ".do_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx")); // Select False Graph
//        program.addInstruction(new X86Instruction(OpCodes.JMP, ".if_done"));
//        program.addInstruction(new X86Instruction(".do_true"));
//        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx")); // Select True Graph
//        program.addInstruction(new X86Instruction(".if_done"));
//        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
//        program.addInstruction(new X86Instruction(OpCodes.RET));
//    }
//}

package ca.brock.cs.lambda.abstractmachine;

import ca.brock.cs.lambda.combinators.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a combinator tree into x86-64 assembly using Graph Reduction.
 * Implements a Tagged Evaluator Loop for Lazy/Normal-Order execution.
 */
public class X86Emitter {

    private final X86Program program;
    private final Set<String> definedFunctions;
    private final Set<String> externalVariables;

    public X86Emitter() {
        this.program = new X86Program();
        this.definedFunctions = new HashSet<>();
        this.externalVariables = new HashSet<>();
    }

    public X86Program compile(Map<String, Combinator> globals, String entryPointName) {
        for (String key : globals.keySet()) {
            definedFunctions.add(sanitizeLabel(key));
        }

        program.addInstruction(new X86Instruction(".p2align 3\n_start"));

        // ---------------------------------------------------------
        // INITIALIZE THE HEAP POINTER
        // ---------------------------------------------------------
        program.addInstruction(new X86Instruction(OpCodes.LEA, "rax", "[HEAP]"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[heap_ptr]", "rax"));

        program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RBP.toString()));
        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RBP.toString(), Registers.RSP.toString()));

        // Push the tagged pointer for the main function graph
        String mainLabel = sanitizeLabel(entryPointName);
        program.addInstruction(new X86Instruction(OpCodes.LEA, "rax", "[" + mainLabel + "]"));
        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "2"));

        // ---------------------------------------------------------
        // GUARANTEED REDUCTION LOOP
        // Keep evaluating RAX until it resolves to a tagged integer
        // ---------------------------------------------------------
        program.addInstruction(new X86Instruction(".force_eval_loop"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "1")); // Is LSB 1? (Integer check)
        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".force_eval_done")); // Yes, it's an int, break loop
        // It evaluated to a function or thunk, loop again to force it
        program.addInstruction(new X86Instruction(OpCodes.JMP, ".force_eval_loop"));

        program.addInstruction(new X86Instruction(".force_eval_done"));
        // ---------------------------------------------------------

        // Result left in RAX, print it
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_print_int"));

        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), "60")); // sys_exit
        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RDI.toString(), "0"));
        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));

        for (Map.Entry<String, Combinator> entry : globals.entrySet()) {
            String label = sanitizeLabel(entry.getKey());
            // User-defined globals act as thunks (arity 0).
            program.addInstruction(new X86Instruction(".p2align 3\n.quad 0\n" + label));
            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.R15.toString()));

            emitTerm(entry.getValue());

            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.RAX.toString()));
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.R15.toString()));
            program.addInstruction(new X86Instruction(OpCodes.RET));
        }

        emitCombinatorRuntime();
        emitNativeOperators();
        emitPrintIntRoutine();
//        emitDataSections();

        return program;
    }

    private void emitTerm(Combinator term) {
        if (term instanceof CombinatorConstant) {
            CombinatorConstant constant = (CombinatorConstant) term;
            Object val = constant.getValue();

            // AGGRESSIVE TAGGING: Catch Integers even if they are stored as Strings
            boolean isNumber = false;
            long numVal = 0;

            if (val instanceof Integer || val instanceof Long) {
                isNumber = true;
                numVal = ((Number) val).longValue();
            } else {
                try {
                    numVal = Long.parseLong(String.valueOf(val));
                    isNumber = true;
                } catch (NumberFormatException e) {
                    // It's a constructor or uninterpreted function string, not a number
                }
            }

            if (isNumber) {
                // TAGGING: Shift left 1, OR with 1. (e.g. 0 -> 1, 1 -> 3, 42 -> 85)
                long taggedVal = (numVal << 1) | 1;
                program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), String.valueOf(taggedVal)));
            } else {
                String label = sanitizeLabel(String.valueOf(val));
                externalVariables.add(label);
                program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
                program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
            }
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
        else if (term instanceof CombinatorVariable) {
            CombinatorVariable var = (CombinatorVariable) term;
            String label = sanitizeLabel(var.getName());
            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
            program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
        else if (term instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) term;

            emitTerm(app.getArgument());
            emitTerm(app.getFunction());

            // GRAPH REDUCTION: Allocate a heap node [Function, Argument]
            program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
            program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
            program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
            program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
            program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
        else {
            String label = getCombinatorLabel(term);
            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
            program.addInstruction(new X86Instruction(OpCodes.OR, Registers.RAX.toString(), "2")); // Tag as function
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
    }

    private String sanitizeLabel(String name) {
        if (name == null) return "lbl_null";
        String sanitized = name
            .replace("+", "plus")
            .replace("-", "minus")
            .replace("*", "mul")
            .replace("<=", "lteq")
            .replaceAll("[^a-zA-Z0-9_]", "_");
        return "lbl_" + sanitized;
    }

    private String getCombinatorLabel(Combinator term) {
        if (term instanceof SCombinator) return "comb_S";
        if (term instanceof KCombinator) return "comb_K";
        if (term instanceof ICombinator) return "comb_I";
        if (term instanceof BCombinator) return "comb_B";
        if (term instanceof CCombinator) return "comb_C";
        if (term instanceof WCombinator) return "comb_W";
        if (term instanceof YCombinator) return "comb_Y";
        if (term instanceof CStarCombinator) return "comb_CStar";
        return "unknown_comb";
    }

    private void emitPrintIntRoutine() {
        program.addInstruction(new X86Instruction(".p2align 3\nlbl_print_int"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbp"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbp", "rsp"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r12"));

        // UNTAG BEFORE PRINTING
        program.addInstruction(new X86Instruction(OpCodes.SAR, "rax", "1"));

        program.addInstruction(new X86Instruction(OpCodes.SUB, "rsp", "32"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "0"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "10"));
        program.addInstruction(new X86Instruction(OpCodes.LEA, "r12", "[rsp+31]"));

        program.addInstruction(new X86Instruction(OpCodes.MOV, "byte ptr [r12]", "10"));
        program.addInstruction(new X86Instruction(OpCodes.DEC, "r12"));
        program.addInstruction(new X86Instruction(OpCodes.INC, "rcx"));

        program.addInstruction(new X86Instruction(".print_loop"));
        program.addInstruction(new X86Instruction(OpCodes.XOR, "rdx", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.DIV, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.ADD, "dl", "'0'"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[r12]", "dl"));
        program.addInstruction(new X86Instruction(OpCodes.DEC, "r12"));
        program.addInstruction(new X86Instruction(OpCodes.INC, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.CMP, "rax", "0"));
        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".print_loop"));

        program.addInstruction(new X86Instruction(OpCodes.INC, "r12"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "1"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdi", "1"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rsi", "r12"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdx", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));

        program.addInstruction(new X86Instruction(OpCodes.ADD, "rsp", "32"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r12"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbp"));
        program.addInstruction(new X86Instruction(OpCodes.RET));
    }

    private void emitCombinatorRuntime() {
        // -----------------------------------------------------------------
        // THE EVALUATOR UNWINDER
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction(".p2align 3\nlbl_eval"));
        // Establish a local spine frame to calculate argument depth
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbp"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbp", "rsp"));

        program.addInstruction(new X86Instruction(".eval_loop"));
        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "1"));
        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".eval_done")); // Int

        program.addInstruction(new X86Instruction(OpCodes.TEST, "rax", "2"));
        program.addInstruction(new X86Instruction(OpCodes.JNZ, ".eval_func")); // Func

        // It's a Heap Node. Unwind the spine: Push argument, evaluate function
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "[rax+8]"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[rax]"));
        program.addInstruction(new X86Instruction(OpCodes.JMP, ".eval_loop"));

        program.addInstruction(new X86Instruction(".eval_func"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.AND, "rcx", "-3")); // Clear function tag bit
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdx", "[rcx-8]")); // Read ARITY

        // Calculate pending arguments from the stack depth
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rbp"));
        program.addInstruction(new X86Instruction(OpCodes.SUB, "r8", "rsp"));
        program.addInstruction(new X86Instruction(OpCodes.SHR, "r8", "3")); // Divide by 8

        program.addInstruction(new X86Instruction(OpCodes.CMP, "r8", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.JL, ".partial_application"));

        // Sufficient arguments - execute!
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.JMP, ".eval_loop")); // Evaluate resulting graph against remaining args

        // Reconstruct partial application graph from stack
        program.addInstruction(new X86Instruction(".partial_application"));
        program.addInstruction(new X86Instruction(OpCodes.CMP, "rsp", "rbp"));
        program.addInstruction(new X86Instruction(OpCodes.JE, ".eval_done"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.JMP, ".partial_application"));

        program.addInstruction(new X86Instruction(".eval_done"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rsp", "rbp"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbp"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // HEAP ALLOCATOR (With Overflow Checking)
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction(".p2align 3\nlbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[heap_ptr]"));
        program.addInstruction(new X86Instruction(OpCodes.LEA, "r15", "[rax + 16]"));

        program.addInstruction(new X86Instruction(OpCodes.LEA, "r11", "[HEAP + 67108864]"));
        program.addInstruction(new X86Instruction(OpCodes.CMP, "r15", "r11"));
        // Newline trick safely inserts "jae" without requiring it in the OpCodes enum
        program.addInstruction(new X86Instruction("\tjae lbl_heap_overflow\n.alloc_ok"));

        program.addInstruction(new X86Instruction(OpCodes.MOV, "[heap_ptr]", "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // System crash sequence upon heap exhaustion
        program.addInstruction(new X86Instruction(".p2align 3\nlbl_heap_overflow"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "60")); // sys_exit
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rdi", "1"));  // error code 1
        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));

        // -----------------------------------------------------------------
        // COMBINATORS
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction(".p2align 3\n.quad 1\ncomb_I"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rax"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        program.addInstruction(new X86Instruction(".p2align 3\n.quad 2\ncomb_K"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rax"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        program.addInstruction(new X86Instruction(".p2align 3\n.quad 3\ncomb_B"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "r8"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        program.addInstruction(new X86Instruction(".p2align 3\n.quad 3\ncomb_C"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r8"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        program.addInstruction(new X86Instruction(".p2align 3\n.quad 2\ncomb_CStar"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // arg 1 (a)
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // arg 2 (b)
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        program.addInstruction(new X86Instruction(".p2align 3\n.quad 3\ncomb_S"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r9", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r9"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "r8"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        program.addInstruction(new X86Instruction(".p2align 3\n.quad 1\ncomb_Y"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rax")); // Cyclic graph
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        program.addInstruction(new X86Instruction(".p2align 3\n.quad 2\ncomb_W"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "r8"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));
    }

    private void emitNativeOperators() {
        // NATIVE ADDITION (+)
        program.addInstruction(new X86Instruction(".p2align 3\n.quad 2\nlbl_plus"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Arg 1
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Arg 2
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1")); // Untag
        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1")); // Untag
        program.addInstruction(new X86Instruction(OpCodes.ADD, "rbx", "rcx")); // Execute
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1")); // Retag
        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // NATIVE MULTIPLICATION (*)
        program.addInstruction(new X86Instruction(".p2align 3\n.quad 2\nlbl_mul"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1"));
        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1"));
        program.addInstruction(new X86Instruction(OpCodes.IMUL, "rbx", "rcx")); // Execute
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1"));
        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // NATIVE SUBTRACTION (-)
        program.addInstruction(new X86Instruction(".p2align 3\n.quad 2\nlbl_minus"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.SAR, "rbx", "1"));
        program.addInstruction(new X86Instruction(OpCodes.SAR, "rcx", "1"));
        program.addInstruction(new X86Instruction(OpCodes.SUB, "rbx", "rcx")); // Execute
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.SHL, "rax", "1"));
        program.addInstruction(new X86Instruction(OpCodes.OR, "rax", "1"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // NATIVE LESS-THAN-EQUAL (<=)
        program.addInstruction(new X86Instruction(".p2align 3\n.quad 2\nlbl_lteq"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rbx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rcx", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.CMP, "rbx", "rcx")); // Execute
        program.addInstruction(new X86Instruction(OpCodes.JLE, ".is_true"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "1"));   // False (Tagged 0)
        program.addInstruction(new X86Instruction(OpCodes.JMP, ".cmp_done"));
        program.addInstruction(new X86Instruction(".is_true"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "3"));   // True (Tagged 1)
        program.addInstruction(new X86Instruction(".cmp_done"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // NATIVE CONDITIONAL BRANCH (IF)
        program.addInstruction(new X86Instruction(".p2align 3\n.quad 3\nlbl_IF"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rax")); // cond
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // true_branch
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // false_branch
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_eval")); // Evaluate Condition
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.CMP, "rax", "3")); // 3 is tagged True
        program.addInstruction(new X86Instruction(OpCodes.JE, ".do_true"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rcx")); // Select False Graph
        program.addInstruction(new X86Instruction(OpCodes.JMP, ".if_done"));
        program.addInstruction(new X86Instruction(".do_true"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx")); // Select True Graph
        program.addInstruction(new X86Instruction(".if_done"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));
    }

//    private void emitDataSections() {
//        program.addInstruction(new X86Instruction(
//            ".section .bss\n" +
//                "    .lcomm HEAP, 67108864\n" +
//                "    .lcomm heap_ptr, 8\n" +
//                "_bss_end" // Dummy label to safely absorb the auto-generated colon
//        ));
//    }
}