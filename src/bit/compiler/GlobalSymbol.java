package bit.compiler;

import bit.BitType;
import bit.WordSize;

public class GlobalSymbol {
    private final BitType type;
    private final WordSize size;
    private final String name;

    public GlobalSymbol(BitType type, WordSize size, String name) {
        this.type = type;
        this.size = size;
        this.name = name;
    }

    public BitType getType() {
        return type;
    }

    public WordSize getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}
