package bit.instruction.arithmetic;

import bit.memory.address.type.Register;
import bit.instruction.Instruction;

public class Mul extends Instruction {
    public final Register left;

    public Mul(Register left) {
        this.left = left;
    }

    @Override
    public String translate() {
        return String.format("mul %s", left.translate());
    }
}
