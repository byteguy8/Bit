package bit.instruction.logical;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class Or extends Instruction {
    private final Address left;
    private final Address right;

    public Or(Address left, Address right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String translate() {
        return String.format("or %s, %s", left.translate(), right.translate());
    }
}
