package bit.instruction.arithmetic;

import bit.memory.address.type.Register;
import bit.instruction.Instruction;

public class Sub extends Instruction {
    public Register left;
    public bit.memory.address.Address right;

    public Sub(Register left, bit.memory.address.Address right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String translate() {
        return String.format("sub %s, %s", left.translate(), right.translate());
    }
}
