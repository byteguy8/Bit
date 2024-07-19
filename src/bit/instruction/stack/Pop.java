package bit.instruction.stack;

import bit.memory.address.Address;
import bit.instruction.Instruction;

public class Pop extends Instruction {
    public final Address address;

    public Pop(Address address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("pop %s", address.translate());
    }
}
