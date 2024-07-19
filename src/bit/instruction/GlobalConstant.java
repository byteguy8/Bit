package bit.instruction;

import bit.WordSize;

public class GlobalConstant extends Instruction {
    private final String name;
    private final WordSize size;
    private final int value;

    public GlobalConstant(String name, WordSize size, int value) {
        this.name = name;
        this.size = size;
        this.value = value;
    }

    @Override
    public String translate() {
        String sizeStr;
        switch (size) {
            case BYTE:
                sizeStr = "db";
                break;
            case WORD:
                sizeStr = "dw";
                break;
            case DOUBLE_WORD:
                sizeStr = "dd";
                break;
            case QUAD_WORD:
                sizeStr = "dq";
                break;
            default:
                sizeStr = "";
                break;
        }

        return String.format("%s:\n%s %d", name, sizeStr, value);
    }
}
