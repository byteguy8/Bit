package bit.instruction.arithmetic;

import bit.memory.address.Address;
import bit.memory.address.type.Register;
import bit.instruction.Instruction;

public final class Add extends Instruction {
    public final Register left;
    public final Address right;

    public Add(Register left, bit.memory.address.Address right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String translate() {
        return String.format("add %s, %s", left.translate(), right.translate());
    }
}
