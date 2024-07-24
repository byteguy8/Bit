package bit.initialization;

import bit.Token;

public class InitializationContainer {
    private final Token identifier;

    public InitializationContainer(Token identifier) {
        this.identifier = identifier;
    }

    public Token getIdentifier() {
        return identifier;
    }
}
