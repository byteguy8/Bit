package bit.instruction.conditional;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class Setge extends Instruction {
    private final Address address;

    public Setge(Address address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("setge %s", address.translate());
    }
}
