package bit.instruction.stack;

import bit.instruction.Instruction;
import bit.instruction.Mov;
import bit.memory.CompilerRegister;
import bit.memory.address.type.Register;

public class Prologue extends Instruction {
    @Override
    public String translate() {
        Register rsp = new Register(CompilerRegister.rsp);
        Register rbp = new Register(CompilerRegister.rbp);

        Push push = new Push(rbp);
        Mov mov = new Mov(rbp, rsp);

        return String.format("%s\n%s", push.translate(), mov.translate());
    }
}
