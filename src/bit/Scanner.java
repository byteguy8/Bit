package bit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private int lastBreak;

    private int line;
    private int from;
    private int to;
    private String source;
    private List<Token> tokens;

    private int start;
    private int current;

    private final Map<String, TokenType> keywords = new HashMap<>();

    public Scanner() {
        keywords.put("bool", TokenType.BOOL_TYPE);
        keywords.put("int", TokenType.INT_TYPE);
        keywords.put("str", TokenType.STR_TYPE);

        keywords.put("i8", TokenType.I8_TYPE);
        keywords.put("i16", TokenType.I16_TYPE);
        keywords.put("i32", TokenType.I32_TYPE);
        keywords.put("i64", TokenType.I64_TYPE);


        keywords.put("print", TokenType.PRINT);
        keywords.put("fn", TokenType.FN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("export", TokenType.EXPORT);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("struct", TokenType.STRUCT);
    }

    private ScannerError error(String message) {
        Bit.error(line, from, to, message);
        return new ScannerError();
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        to++;
        return source.charAt(current++);
    }

    private boolean match(char c) {
        if (isAtEnd()) return false;

        if (peek() == c) {
            advance();
            return true;
        }

        return false;
    }

    private void addToken(Object literal, TokenType type) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(line, from, to, lexeme, literal, type));
    }

    private void addToken(TokenType type) {
        addToken(null, type);
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.') {
            if (!isDigit(peekNext())) {
                throw error(String.format("Illegal number: expect a digit, but got %c", peekNext()));
            }

            advance();

            while (isDigit(peek())) advance();
        }

        String rawLiteral = source.substring(start, current);
        long literal = Long.parseLong(rawLiteral);

        addToken(literal, TokenType.NUMBER);
    }

    private void string() {
        int lines = 0;

        while (!isAtEnd() && peek() != '"') {
            if (advance() == '\n')
                lines++;
        }

        if (peek() != '"')
            throw error("Unterminated string");

        advance();

        String str = source.substring(start + 1, current - 1);
        addToken(str, TokenType.STRING);

        line += lines;
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        Object lexeme = source.substring(start, current);

        addToken(keywords.getOrDefault(lexeme, TokenType.IDENTIFIER));
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '+':
                addToken(TokenType.PLUS);
                break;

            case '-':
                addToken(TokenType.MINUS);
                break;

            case '/':
                addToken(TokenType.SLASH);
                break;

            case '*':
                addToken(TokenType.ASTERISK);
                break;

            case '%':
                addToken(TokenType.PERCENT);
                break;

            case ';':
                addToken(TokenType.SEMICOLON);
                break;

            case ':':
                addToken(TokenType.COLON);
                break;

            case ',':
                addToken(TokenType.COMMA);
                break;

            case '(':
                addToken(TokenType.LEFT_PARENTHESIS);
                break;

            case ')':
                addToken(TokenType.RIGHT_PARENTHESIS);
                break;

            case '{':
                addToken(TokenType.LEFT_BRACKET);
                break;

            case '}':
                addToken(TokenType.RIGHT_BRACKET);
                break;

            case '|':
                if (match('|'))
                    addToken(TokenType.OR);
                break;

            case '&':
                if (match('&'))
                    addToken(TokenType.AND);
                break;

            case '<':
                if (match('='))
                    addToken(TokenType.LESS_EQUALS);
                else
                    addToken(TokenType.LESS_THAN);
                break;

            case '>':
                if (match('='))
                    addToken(TokenType.GREATER_EQUALS);
                else
                    addToken(TokenType.GREATER_THAN);
                break;

            case '=':
                if (match('='))
                    addToken(TokenType.EQUALS_EQUALS);
                else
                    addToken(TokenType.EQUALS);
                break;

            case '!':
                if (match('='))
                    addToken(TokenType.BANG_EQUALS);
                else
                    addToken(TokenType.BANG);
                break;

            case '\n':
                lastBreak = current;
                line++;
                from = 1;
                to = 0;
                break;

            case '\t':
            case ' ':
                break;

            default:
                if (isDigit(c))
                    number();
                else if (c == '"')
                    string();
                else if (isAlphaNumeric(c))
                    identifier();
                else
                    throw error(String.format("Unknown token '%c'", c));
        }
    }

    public List<Token> scanTokens(String source) {
        this.lastBreak = 0;

        this.line = 1;
        this.from = 1;
        this.to = 0;

        this.start = 0;
        this.current = 0;
        this.source = source;
        this.tokens = new ArrayList<>();

        while (!isAtEnd()) {
            scanToken();
            start = current;
            from = start - lastBreak + 1;
        }

        to++;
        addToken(TokenType.EOF);

        return new ArrayList<>(tokens);
    }
}
