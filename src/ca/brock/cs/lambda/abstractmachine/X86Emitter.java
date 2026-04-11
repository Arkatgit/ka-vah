package ca.brock.cs.lambda.abstractmachine;

import ca.brock.cs.lambda.combinators.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a partially evaluated combinator tree into x86-64 assembly.
 * Adapts memory allocation and graph reduction semantics dynamically.
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

        program.addInstruction(new X86Instruction("_start"));

        program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RBP.toString()));
        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RBP.toString(), Registers.RSP.toString()));

        String mainLabel = sanitizeLabel(entryPointName);
        program.addInstruction(new X86Instruction(OpCodes.CALL, mainLabel));

        // Result left in RAX
        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), "60")); // sys_exit
        program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RDI.toString(), "0"));  // status 0
        program.addInstruction(new X86Instruction(OpCodes.SYSCALL));

        for (Map.Entry<String, Combinator> entry : globals.entrySet()) {
            String label = sanitizeLabel(entry.getKey());
            program.addInstruction(new X86Instruction(label));

            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.R15.toString())); // Save ret addr

            emitTerm(entry.getValue());

            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.RAX.toString())); // Result to rax

            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.R15.toString())); // Restore ret addr
            program.addInstruction(new X86Instruction(OpCodes.RET));
        }

        for (String ext : externalVariables) {
            if (!definedFunctions.contains(ext)) {
                program.addInstruction(new X86Instruction(ext));
                // Load its own label address as a tag for uninterpreted constructors
                program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + ext + "]"));
                program.addInstruction(new X86Instruction(OpCodes.RET));
            }
        }

        emitCombinatorRuntime();

        return program;
    }

    private void emitTerm(Combinator term) {
        if (term instanceof CombinatorConstant) {
            CombinatorConstant constant = (CombinatorConstant) term;
            Object val = constant.getValue();

            if (val instanceof Integer) {
                // Immediate numeric value
                program.addInstruction(new X86Instruction(OpCodes.MOV, Registers.RAX.toString(), String.valueOf(val)));
            } else {
                // String constants (Operators like '*', '+', 'IF')
                String label = sanitizeLabel(String.valueOf(val));
                externalVariables.add(label);
                program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
            }
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
        else if (term instanceof CombinatorVariable) {
            CombinatorVariable var = (CombinatorVariable) term;
            String label = sanitizeLabel(var.getName());

            if (!definedFunctions.contains(label)) {
                externalVariables.add(label);
            }

            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
        else if (term instanceof CombinatorApplication) {
            CombinatorApplication app = (CombinatorApplication) term;

            emitTerm(app.getArgument());
            emitTerm(app.getFunction());

            program.addInstruction(new X86Instruction(OpCodes.POP, Registers.RAX.toString()));
            program.addInstruction(new X86Instruction(OpCodes.CALL, Registers.RAX.toString()));

            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
        else if (term instanceof SCombinator || term instanceof KCombinator || term instanceof ICombinator ||
            term instanceof BCombinator || term instanceof CCombinator || term instanceof WCombinator ||
            term instanceof YCombinator || term instanceof CStarCombinator) {
            String label = getCombinatorLabel(term);
            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
        else {
            String label = sanitizeLabel(term.getClass().getSimpleName());
            externalVariables.add(label);
            program.addInstruction(new X86Instruction(OpCodes.LEA, Registers.RAX.toString(), "[" + label + "]"));
            program.addInstruction(new X86Instruction(OpCodes.PUSH, Registers.RAX.toString()));
        }
    }

    private String sanitizeLabel(String name) {
        if (name == null) return "lbl_null";
        String sanitized = name
            .replace("+", "plus")
            .replace("-", "minus")
            .replace("*", "mul")
            .replace("/", "div")
            .replace("=", "eq")
            .replace("<=", "le")
            .replace(">=", "ge")
            .replace("<", "lt")
            .replace(">", "gt")
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

    /**
     * Emits the combinator runtime and the dynamic closure allocator.
     */
    private void emitCombinatorRuntime() {
        // -----------------------------------------------------------------
        // HEAP ALLOCATOR (Equivalent to FCompiler MemoryCalculator)
        // Allocates 16 bytes. Returns pointer in RAX.
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "[heap_ptr]"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r15", "rax"));
        program.addInstruction(new X86Instruction(OpCodes.ADD, "r15", "16"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[heap_ptr]", "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // I Combinator: I x = x
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_I"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15")); // ret addr
        program.addInstruction(new X86Instruction(OpCodes.POP, "rax")); // x
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // K Combinator: K x y = x
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_K"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rax")); // x
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // y (discard)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // B Combinator: B x y z = x (y z)
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_B"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // z
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node")); // Alloc (y z)
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rax")); // push (y z)
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx")); // load x
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval x(y z)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // C Combinator: C x y z = x z y
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_C"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // z
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rdx")); // push z
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push y
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)(y)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // S Combinator: S x y z = x z (y z)
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_S"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // z
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node")); // Alloc (y z)
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rcx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rdx"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "r8", "rax")); // r8 = pointer to (y z)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rdx")); // push z
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r8")); // push (y z)
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x z)(y z)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // W Combinator: W x y = x y y
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_W"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // x
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // y
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push y
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval x(y)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push y
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (x y)(y)
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // Y Combinator: Y f = f (Y f)  [Creates Cyclic Graph]
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_Y"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // f
        program.addInstruction(new X86Instruction(OpCodes.CALL, "lbl_alloc_node"));
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax]", "rbx")); // Left side = f
        program.addInstruction(new X86Instruction(OpCodes.MOV, "[rax+8]", "rax")); // Right side points to ITSELF!
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rax")); // push cyclic node
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // Call f with the cyclic graph
        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));

        // -----------------------------------------------------------------
        // C* (CStar) Combinator: C* p q r s = p q s r
        // -----------------------------------------------------------------
        program.addInstruction(new X86Instruction("comb_CStar"));
        program.addInstruction(new X86Instruction(OpCodes.POP, "r15")); // ret addr
        program.addInstruction(new X86Instruction(OpCodes.POP, "rbx")); // p
        program.addInstruction(new X86Instruction(OpCodes.POP, "rcx")); // q
        program.addInstruction(new X86Instruction(OpCodes.POP, "rdx")); // r
        program.addInstruction(new X86Instruction(OpCodes.POP, "r8"));  // s

        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rcx")); // push q
        program.addInstruction(new X86Instruction(OpCodes.MOV, "rax", "rbx"));
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (p q)

        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r8"));  // push s
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (p q s)

        program.addInstruction(new X86Instruction(OpCodes.PUSH, "rdx")); // push r
        program.addInstruction(new X86Instruction(OpCodes.CALL, "rax")); // eval (p q s r)

        program.addInstruction(new X86Instruction(OpCodes.PUSH, "r15"));
        program.addInstruction(new X86Instruction(OpCodes.RET));
    }
}