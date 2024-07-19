package bit.memory.address;

public class Global extends Address{
    private final String name;

    public Global(String name) {
        this.name = name;
    }

    @Override
    public String translate() {
        return String.format("[%s]", name);
    }
}