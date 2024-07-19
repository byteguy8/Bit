package bit.name_resolver;

public class NameContainer {
    private final boolean isGlobal;

    public NameContainer(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
}
