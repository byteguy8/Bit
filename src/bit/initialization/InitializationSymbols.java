package bit.initialization;

import bit.Bit;
import bit.Symbols;
import bit.SymbolsError;
import bit.Token;

import java.util.HashMap;
import java.util.Map;

public class InitializationSymbols extends Symbols<InitializationContainer> {
    private final Map<String, Void> initialized;

    public InitializationSymbols(Symbols<InitializationContainer> enclosing, Map<String, InitializationContainer> symbols, Map<String, Void> initialized) {
        super(enclosing, symbols);
        this.initialized = initialized;
    }

    public InitializationSymbols(Symbols<InitializationContainer> enclosing) {
        this(enclosing, new HashMap<>(), new HashMap<>());
    }

    public InitializationSymbols() {
        this(null);
    }

    private SymbolsError error(Token identifier, String message) {
        Bit.error(identifier, message);
        return new SymbolsError();
    }

    @Override
    public InitializationSymbols declare(Token identifier, InitializationContainer initializationContainer) {
        if (getSymbols().containsKey(identifier.lexeme))
            throw error(identifier, String.format("Already exists a symbol named '%s'", identifier.lexeme));

        Map<String, InitializationContainer> symbols = new HashMap<>(getSymbols());

        symbols.put(identifier.lexeme, initializationContainer);

        return new InitializationSymbols(getEnclosing(), symbols, new HashMap<>(initialized));
    }

    public boolean isInitialized(Token entity) {
        if (initialized.containsKey(entity.lexeme))
            return true;
        else if (getEnclosing() != null) {
            return ((InitializationSymbols) getEnclosing()).isInitialized(entity);
        } else {
            return false;
        }
    }

    public void setInitialized(Token entity) {
        initialized.put(entity.lexeme, null);
    }
}
