package bit.compiler;

import bit.BitType;
import bit.WordSize;

public class FrameLine {
    public final int position;
    public final WordSize size;

    public BitType type;
    public Object payload;

    public FrameLine(int position, WordSize size) {
        this.position = position;
        this.size = size;
    }
}
