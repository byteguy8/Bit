package bit.type_checker;

import bit.BitType;

public class CheckerContainer {
    private final BitType type;
    private final Object value;

    public CheckerContainer(BitType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public BitType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
