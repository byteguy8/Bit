package bit.instruction;

public class Call extends Instruction {
    private final String name;

    public Call(String name) {
        this.name = name;
    }

    @Override
    public String translate() {
        return String.format("call %s", name);
    }
}
