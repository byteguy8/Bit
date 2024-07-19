package bit.memory.address.type;

import bit.memory.address.Address;

public final class DoubleWord extends Address {
    public int value;

    public DoubleWord(int value) {
        this.value = value;
    }

    @Override
    public String translate() {
        return String.format("%d", value);
    }
}
