package bit.instruction.conditional;

import bit.instruction.Instruction;
import bit.memory.address.type.LabelAddress;

public class Je extends Instruction {
    private final LabelAddress address;

    public Je(LabelAddress address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("je %s", address.translate());
    }
}
