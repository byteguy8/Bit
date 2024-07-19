package bit;

import bit.expression.*;
import bit.statement.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private int current;
    private List<Token> tokens;
    private boolean containsBreak = false;

    private boolean isType() {
        Token token = peek();
        TokenType type = token.type;

        boolean flag = type == TokenType.BOOL_TYPE ||
                type == TokenType.INT_TYPE ||
                type == TokenType.STR_TYPE;

        if (flag) current++;

        return flag;
    }

    private ParserError error(Token token, String message) {
        String msg;

        if (token.type == TokenType.EOF) {
            msg = String.format("%s But got end of file.", message);
        } else {
            msg = message;
        }

        Bit.error(
                token.line,
                token.from,
                token.to,
                msg
        );

        return new ParserError();
    }

    private boolean isAtEnd() {
        return tokens.get(current).type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean match(TokenType... types) {
        Token token = peek();

        for (TokenType type : types) {
            if (token.type == type) {
                current++;
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private Token consume(TokenType type, String message) {
        Token token = peek();

        if (token.type == type) {
            current++;
            return token;
        }

        throw error(token, message);
    }

    private Expression expression() {
        return assignmentExpr();
    }

    private Expression assignmentExpr() {
        Expression left = logicalOrExpr();

        if (match(TokenType.EQUALS)) {
            Token equalsToken = previous();
            Expression right = assignmentExpr();

            return new AssignmentExpr(left, equalsToken, right);
        }

        return left;
    }

    private Expression logicalOrExpr() {
        Expression left = logicalAndExpr();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = logicalAndExpr();

            left = new LogicalExpr(left, operator, right);
        }

        return left;
    }

    private Expression logicalAndExpr() {
        Expression left = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();

            left = new LogicalExpr(left, operator, right);
        }

        return left;
    }

    private Expression equality() {
        Expression left = comparisonExpr();

        while (match(TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS)) {
            Token operator = previous();
            Expression right = comparisonExpr();

            left = new EqualityExpr(left, operator, right);
        }

        return left;
    }

    private Expression comparisonExpr() {
        Expression left = termExpr();

        while (match(TokenType.LESS_THAN, TokenType.GREATER_THAN, TokenType.LESS_EQUALS, TokenType.GREATER_EQUALS)) {
            Token operator = previous();
            Expression right = termExpr();

            left = new Comparison(left, operator, right);
        }

        return left;
    }

    private Expression termExpr() {
        Expression left = factorExpr();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expression right = factorExpr();

            left = new BinaryExpr(left, operator, right);
        }

        return left;
    }

    private Expression factorExpr() {
        Expression left = unaryExpr();

        while (match(TokenType.SLASH, TokenType.ASTERISK, TokenType.PERCENT)) {
            Token operator = previous();
            Expression right = unaryExpr();

            left = new BinaryExpr(left, operator, right);
        }

        return left;
    }

    private Expression unaryExpr() {
        if (match(TokenType.MINUS, TokenType.BANG)) {
            Token operator = previous();
            Expression right = unaryExpr();

            return new UnaryExpr(operator, right);
        }

        return callExpr();
    }

    private List<Expression> callArgs() {
        List<Expression> args = new ArrayList<>();

        do {
            args.add(expression());
        } while (match(TokenType.COMMA));

        return args;
    }

    private Expression callExpr() {
        Expression expression = primaryExpr();

        if (match(TokenType.LEFT_PARENTHESIS)) {
            Token leftParenToken = previous();
            List<Expression> arguments = null;

            if (!check(TokenType.RIGHT_PARENTHESIS)) {
                arguments = callArgs();
            }

            consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after call list of arguments.");

            return new CallExpr(expression, leftParenToken, arguments);
        }

        return expression;
    }

    private Expression primaryExpr() {
        if (match(TokenType.TRUE)) {
            return new Literal(true);
        }

        if (match(TokenType.FALSE)) {
            return new Literal(false);
        }

        if (match(TokenType.NUMBER)) {
            return new Literal(previous().literal);
        }

        if (match(TokenType.STRING)) {
            return new Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new IdentifierExpr(previous());
        }

        throw error(peek(), "Unexpected token");
    }

    private Statement statement() {
        if (isType()) {
            return varDeclarationStmt();
        }

        if (match(TokenType.FN))
            return fnDeclarationStmt();

        return noDeclaration();
    }

    private Statement varDeclarationStmt() {
        Token typeToken = previous();
        Token identifier = consume(TokenType.IDENTIFIER, "Expect variable name after 'var' or 'val' keyword.");
        Expression initializer = match(TokenType.EQUALS) ? expression() : null;

        consume(TokenType.SEMICOLON, "Expect ';' at end of var declaration");

        return new VarDeclarationStmt(
                typeToken,
                identifier,
                initializer
        );
    }

    private List<FnDeclarationStmt.FnParam> fnParams() {
        List<FnDeclarationStmt.FnParam> params = new ArrayList<>();

        do {
            if (isType()) {
                Token type = previous();
                Token identifier = consume(TokenType.IDENTIFIER, "Expect parameter identifier after parameter type.");
                FnDeclarationStmt.FnParam param = new FnDeclarationStmt.FnParam(type, identifier);

                params.add(param);
            } else {
                throw error(peek(), "Illegal function parameter type.");
            }
        } while (match(TokenType.COMMA));

        return params;
    }

    private Statement fnDeclarationStmt() {
        Token fnToken = previous();
        boolean export = match(TokenType.EXPORT);
        Token identifier = consume(TokenType.IDENTIFIER, "Expect function identifier after 'fn' keyword.");
        List<FnDeclarationStmt.FnParam> params = null;
        Token returnType = null;
        List<Statement> statements;

        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after function identifier.");

        if (!check(TokenType.RIGHT_PARENTHESIS)) {
            params = fnParams();
        }

        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after function parameters list.");

        if (match(TokenType.COLON)) {
            if (isType()) {
                returnType = previous();
            } else {
                throw error(identifier, "Illegal return type after ':'.");
            }
        }

        consume(TokenType.LEFT_BRACKET, "Expect '{' at start of function body.");
        statements = blockStmt();

        return new FnDeclarationStmt(export, fnToken, identifier, params, returnType, statements);
    }

    private Statement noDeclaration() {
        if (match(TokenType.PRINT)) {
            return printStmt();
        }

        if (match(TokenType.RETURN)) {
            return returnStmt();
        }

        if (match(TokenType.BREAK)) {
            return breakStmt();
        }

        if (match(TokenType.CONTINUE)) {
            return continueStmt();
        }

        if (match(TokenType.LEFT_BRACKET)) {
            return new BlockStmt(blockStmt());
        }

        if (match(TokenType.IF)) {
            return ifStmt();
        }

        if (match(TokenType.WHILE)) {
            return whileStmt();
        }

        return expressionStmt();
    }

    private Statement printStmt() {
        Token printToken = previous();
        Expression value = expression();

        consume(TokenType.SEMICOLON, "Expect ';' at end of print statement.");

        return new PrintStmt(printToken, value);
    }

    private Statement returnStmt() {
        Token returnToken = previous();
        Expression expression = null;

        if (!check(TokenType.SEMICOLON)) expression = expression();

        consume(TokenType.SEMICOLON, "Expect ';' at end of return stmt.");

        return new ReturnStmt(returnToken, expression);
    }

    private Statement breakStmt() {
        Token breakToken = previous();
        consume(TokenType.SEMICOLON, "Expect ';' at end of break statement.");
        return new BreakStmt(breakToken);
    }

    private Statement continueStmt() {
        Token continueToken = previous();
        consume(TokenType.SEMICOLON, "Expect ';' at end of continue statement.");
        return new ContinueStmt(continueToken);
    }

    private List<Statement> blockStmt() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACKET)) {
            Statement statement = statement();

            if (!containsBreak && statement instanceof BreakStmt)
                containsBreak = true;

            statements.add(statement);
        }

        consume(TokenType.RIGHT_BRACKET, "Expect '}' at end of block statements.");

        return statements;
    }

    private Statement ifStmt() {
        Token ifToken = previous();
        Expression ifConditionExpr;
        List<Statement> ifStatements;
        List<Statement> elseStatements = null;

        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'if' keyword.");

        ifConditionExpr = logicalOrExpr();

        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after if condition.");
        consume(TokenType.LEFT_BRACKET, "Expect '{' at start of if statement.");

        ifStatements = blockStmt();

        if (match(TokenType.ELSE)) {
            consume(TokenType.LEFT_BRACKET, "Expect '{' at start of else statement.");
            elseStatements = blockStmt();
        }

        return new IfStmt(ifToken, ifConditionExpr, ifStatements, elseStatements);
    }

    private Statement whileStmt() {
        Token whileToken = previous();
        Expression whileConditionExpr;
        List<Statement> statements;

        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'while' keyword.");

        whileConditionExpr = logicalOrExpr();

        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' after while statement condition.");

        final boolean previousBreak = containsBreak;
        containsBreak = false;

        consume(TokenType.LEFT_BRACKET, "Expect '{' at start of while statement body.");
        statements = blockStmt();

        WhileStmt statement = new WhileStmt(whileToken, whileConditionExpr, statements, containsBreak);
        containsBreak = previousBreak;

        return statement;
    }

    private Statement expressionStmt() {
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' at end of expression statement.");

        return new ExprStmt(expression);
    }

    public List<Statement> parseTokens(List<Token> tokens) {
        this.tokens = tokens;
        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }
}
