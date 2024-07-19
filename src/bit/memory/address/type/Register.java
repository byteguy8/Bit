package bit.memory.address.type;

import bit.memory.CompilerRegister;
import bit.memory.address.Address;

public final class Register extends Address {
    public final CompilerRegister register;

    public Register(CompilerRegister register) {
        this.register = register;
    }

    @Override
    public String translate() {
        return String.format("%s", register);
    }
}
