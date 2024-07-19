package bit.instruction.conditional;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class Setl extends Instruction {
    private final Address address;

    public Setl(Address address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("setl %s", address.translate());
    }
}
