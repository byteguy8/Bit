package bit.instruction;

public class Label extends Instruction {
    private final String name;

    public Label(String name) {
        this.name = name;
    }

    @Override
    public String translate() {
        return String.format("%s:", name);
    }
}
