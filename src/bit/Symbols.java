package bit;

import java.util.HashMap;
import java.util.Map;

public class Symbols<T> {
    private long id = -1;
    private final Symbols<T> enclosing;
    private final Map<String, T> symbols;

    public Symbols(Symbols<T> enclosing, Map<String, T> symbols) {
        this.enclosing = enclosing;
        this.symbols = symbols;
    }

    public Symbols(Symbols<T> enclosing) {
        this(enclosing, new HashMap<>());
    }

    public Symbols() {
        this(null);
    }

    private SymbolsError error(Token identifier, String message) {
        Bit.error(identifier, message);
        return new SymbolsError();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEnclosed(){
        return enclosing != null;
    }

    public Symbols<T> getEnclosing() {
        return enclosing;
    }

    public Map<String, T> getSymbols() {
        return symbols;
    }

    public Symbols<T> declare(Token identifier, T container) {
        if (symbols.containsKey(identifier.lexeme))
            throw error(identifier, String.format("Already exists a symbol named '%s'", identifier.lexeme));

        Map<String, T> symbols = new HashMap<>(this.symbols);

        symbols.put(identifier.lexeme, container);

        return new Symbols<>(enclosing, symbols);
    }

    public void declareMutate(Token identifier, T container){
        if (symbols.containsKey(identifier.lexeme))
            throw error(identifier, String.format("Already exists a symbol named '%s'", identifier.lexeme));

        symbols.put(identifier.lexeme, container);
    }

    public T get(Token identifier) {
        if (symbols.containsKey(identifier.lexeme))
            return symbols.get(identifier.lexeme);
        else if (enclosing != null)
            return enclosing.get(identifier);
        else
            throw error(identifier, String.format("Doesn't exists a symbol named '%s'", identifier.lexeme));
    }
}
