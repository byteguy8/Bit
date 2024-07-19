package bit.instruction.conditional;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class Setne extends Instruction {
    private final Address address;

    public Setne(Address address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("setne %s", address.translate());
    }
}
