package bit.instruction.stack;

import bit.memory.address.Address;
import bit.instruction.Instruction;

public class Push extends Instruction {
    public final Address address;

    public Push(Address address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("push %s", address.translate());
    }
}
