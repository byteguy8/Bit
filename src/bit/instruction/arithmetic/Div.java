package bit.instruction.arithmetic;

import bit.memory.address.type.Register;
import bit.instruction.Instruction;

public class Div extends Instruction {
    public final Register left;

    public Div(Register left) {
        this.left = left;
    }

    @Override
    public String translate() {
        return String.format("idiv %s", left.translate());
    }
}
