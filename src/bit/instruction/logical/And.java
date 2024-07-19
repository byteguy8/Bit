package bit.instruction.logical;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class And extends Instruction {
    private final Address left;
    private final Address right;

    public And(Address left, Address right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String translate() {
        return String.format("and %s, %s", left.translate(), right.translate());
    }
}
