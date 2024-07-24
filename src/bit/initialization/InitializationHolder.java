package bit.initialization;

public class InitializationHolder {
    private final InitializationContainer initializationContainer;

    public InitializationHolder(InitializationContainer initializationContainer) {
        this.initializationContainer = initializationContainer;
    }

    public InitializationContainer getContainer() {
        return initializationContainer;
    }
}
