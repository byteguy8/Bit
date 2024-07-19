package bit.instruction;

import bit.instruction.Instruction;
import bit.memory.address.type.LabelAddress;

public class Jmp extends Instruction {
    private LabelAddress label;

    public Jmp(LabelAddress label) {
        this.label = label;
    }

    public void setLabel(LabelAddress label) {
        this.label = label;
    }

    @Override
    public String translate() {
        return String.format("jmp %s", label.translate());
    }
}
