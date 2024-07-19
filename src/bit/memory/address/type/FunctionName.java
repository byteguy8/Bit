package bit.memory.address.type;

import bit.memory.address.Address;

public class FunctionName extends Address {
    public final String name;

    public FunctionName(String name) {
        this.name = name;
    }

    @Override
    public String translate() {
        return name;
    }
}
