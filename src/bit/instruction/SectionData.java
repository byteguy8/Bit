package bit.instruction;

import bit.Utils;
import bit.WordSize;

public class SectionData extends Instruction {
    public final String name;
    public final WordSize size;
    public final Object str;

    public SectionData(String name, WordSize size, Object str) {
        this.name = name;
        this.size = size;
        this.str = str;
    }

    @Override
    public String translate() {
        return String.format("%s %s %s", name, Utils.wordSizeToDataSize(size), str);
    }
}
