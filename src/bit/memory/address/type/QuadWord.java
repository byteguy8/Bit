package bit.memory.address.type;

import bit.memory.address.Address;

public class QuadWord extends Address {
    public long value;

    public QuadWord(long value) {
        this.value = value;
    }

    @Override
    public String translate() {
        return String.format("%d", value);
    }
}
