package bit.instruction;

import bit.memory.CompilerRegister;
import bit.memory.address.Address;
import bit.memory.address.type.DoubleWord;
import bit.memory.address.type.FunctionName;
import bit.memory.address.type.Register;
import bit.WordSize;

import static bit.Utils.registerBySize;

public class Print extends Instruction {
    private final WordSize size;
    private final String formatter;
    private final Address address;

    public Print(WordSize size, String formatter, Address address) {
        this.size = size;
        this.formatter = formatter;
        this.address = address;
    }

    @Override
    public String translate() {
        Mov m0 = new Mov(new Register(CompilerRegister.rdi), new FunctionName(formatter));
        Mov m1 = address == null ? null : new Mov(new Register(registerBySize(size, 4)), address);
        Mov m2 = new Mov(new Register(CompilerRegister.rax), new DoubleWord(0));

        if (m1 == null)
            return String.format("%s\n%s\ncall printf",
                    m0.translate(),
                    m2.translate());
        else
            return String.format("%s\n%s\n%s\ncall printf",
                    m0.translate(),
                    m1.translate(),
                    m2.translate());
    }
}
