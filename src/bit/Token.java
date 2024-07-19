package bit;

public class Token {
    public final int line;
    public final int from;
    public final int to;
    public final String lexeme;
    public final Object literal;
    public final TokenType type;

    public Token(int line, int from, int to, String lexeme, Object literal, TokenType type) {
        this.line = line;
        this.from = from;
        this.to = to;
        this.lexeme = lexeme;
        this.literal = literal;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("line: %d from: %d to: %d type: %s lexeme: %s", line, from, to, type, lexeme);
    }
}
