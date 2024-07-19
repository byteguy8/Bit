package bit.memory.address.type;

import bit.memory.address.Address;

public class LabelAddress extends Address {
    private final String label;

    public LabelAddress(String label) {
        this.label = label;
    }

    @Override
    public String translate() {
        return label;
    }
}
