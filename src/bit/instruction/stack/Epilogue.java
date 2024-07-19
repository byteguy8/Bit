package bit.instruction.stack;

import bit.instruction.Instruction;
import bit.instruction.Mov;
import bit.memory.CompilerRegister;
import bit.memory.address.type.Register;

public class Epilogue extends Instruction {
    @Override
    public String translate() {
        Register rsp = new Register(CompilerRegister.rsp);
        Register rbp = new Register(CompilerRegister.rbp);

        Mov mov = new Mov(rsp, rbp);
        Pop pop = new Pop(rbp);

        return String.format("%s\n%s", mov.translate(), pop.translate());
    }
}
