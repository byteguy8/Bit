package bit.instruction.conditional;

import bit.instruction.Instruction;
import bit.memory.address.Address;

public class Setg extends Instruction {
    private final Address address;

    public Setg(Address left) {
        this.address = left;
    }

    @Override
    public String translate() {
        return String.format("setg %s", address.translate());
    }
}
