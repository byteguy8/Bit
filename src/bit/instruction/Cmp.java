package bit.instruction;

import bit.memory.address.type.Register;

public class Cmp extends Instruction {
    private final Register left;
    private final Register right;

    public Cmp(Register left, Register right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String translate() {
        return String.format("cmp %s, %s", left.translate(), right.translate());
    }
}
