package bit.instruction;

public class Ret extends Instruction {
    @Override
    public String translate() {
        return "ret";
    }
}
