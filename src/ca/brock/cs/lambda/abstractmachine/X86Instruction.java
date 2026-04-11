package ca.brock.cs.lambda.abstractmachine;

/**
 * Represents a single x86 instruction.
 */
public class X86Instruction {
    private final OpCodes opCode;
    private final String operand1;
    private final String operand2;
    private final String label;

    public X86Instruction(OpCodes opCode, String operand1, String operand2) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.label = null;
    }

    public X86Instruction(OpCodes opCode, String operand1) {
        this(opCode, operand1, null);
    }

    public X86Instruction(OpCodes opCode) {
        this(opCode, null, null);
    }

    public X86Instruction(String label) {
        this.opCode = null;
        this.operand1 = null;
        this.operand2 = null;
        this.label = label;
    }

    public boolean isPushRax() {
        return opCode == OpCodes.PUSH && "rax".equalsIgnoreCase(operand1);
    }

    public boolean isPopRax() {
        return opCode == OpCodes.POP && "rax".equalsIgnoreCase(operand1);
    }

    @Override
    public String toString() {
        if (label != null) {
            return label + ":";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("    ").append(opCode.name().toLowerCase());
        if (operand1 != null) {
            sb.append(" ").append(operand1);
            if (operand2 != null) {
                sb.append(", ").append(operand2);
            }
        }
        return sb.toString();
    }
}