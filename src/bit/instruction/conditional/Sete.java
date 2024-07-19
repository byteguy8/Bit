package bit.instruction.conditional;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class Sete extends Instruction {
    private final Address address;

    public Sete(Address address) {
        this.address = address;
    }

    @Override
    public String translate() {
        return String.format("sete %s", address.translate());
    }
}
