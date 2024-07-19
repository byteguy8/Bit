package bit.instruction;

public class SectionText extends Instruction {
    public final boolean global;
    public final String value;

    public SectionText(boolean global, String value) {
        this.global = global;
        this.value = value;
    }

    @Override
    public String translate() {
        if (global) return String.format("global %s", value);
        else return value;
    }
}
