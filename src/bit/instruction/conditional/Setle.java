package bit.instruction.conditional;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class Setle extends Instruction {
    private final Address address;

    public Setle(Address address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("setle %s", address.translate());
    }
}
