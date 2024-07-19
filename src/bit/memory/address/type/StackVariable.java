package bit.memory.address.type;

import bit.memory.CompilerRegister;

public class StackVariable extends bit.memory.address.Address {
    public final long position;

    public StackVariable(long position) {
        this.position = position;
    }

    @Override
    public String translate() {
        return String.format("[%s - %d]",
                new Register(CompilerRegister.rbp).translate(),
                position);
    }
}
