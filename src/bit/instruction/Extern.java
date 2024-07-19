package bit.instruction;

public class Extern extends Instruction {
    private final String name;

    public Extern(String name) {
        this.name = name;
    }

    @Override
    public String translate() {
        return String.format("extern %s", name);
    }
}
