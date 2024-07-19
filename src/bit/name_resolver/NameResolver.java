package bit.name_resolver;

import bit.*;
import bit.expression.*;
import bit.expression.Expression;
import bit.statement.*;

import java.util.List;

public class NameResolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Symbols<NameContainer> globals = new Symbols<>();
    private Symbols<NameContainer> locals = globals;

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitAssignmentExpr(AssignmentExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public Void visitIdentifierExpr(IdentifierExpr expr) {
        Token identifier = expr.identifier;
        NameContainer container = get(identifier);

        expr.isGlobal = container.isGlobal();

        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr expr) {
        evaluate(expr.left);

        return null;
    }

    @Override
    public Void visitComparisonExpr(Comparison expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public Void visitLogicalExpr(LogicalExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public Void visitEqualityExpr(EqualityExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr expr) {
        evaluate(expr.right);
        return null;
    }

    @Override
    public Void visitExpressionStmt(ExprStmt statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitVarDeclarationStmt(VarDeclarationStmt statement) {
        Token identifierToken = statement.identifierToken;
        Expression initializerExpr = statement.initializer;

        boolean isGlobal = !locals.isEnclosed();

        if (initializerExpr != null)
            evaluate(initializerExpr);

        NameContainer container = new NameContainer(isGlobal);
        declare(identifierToken, container);

        statement.isGlobal = isGlobal;

        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt statement) {
        evaluate(statement.value);

        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt statement) {
        executeBlock(new Symbols<>(locals), statement.statements);

        return null;
    }

    @Override
    public Void visitFnDeclarationStmt(FnDeclarationStmt statement) {
        Token identifierToken = statement.identifierToken;
        List<FnDeclarationStmt.FnParam> params = statement.params;
        List<Statement> body = statement.body;

        boolean isGlobal = globals == locals;
        NameContainer container = new NameContainer(isGlobal);

        declare(identifierToken, container);

        Symbols<NameContainer> fnSymbols = new Symbols<>(locals);

        if (params != null) {
            for (FnDeclarationStmt.FnParam param : params) {
                Token paramIdentifierToken = param.identifier;
                NameContainer c = new NameContainer(false);

                fnSymbols.declareMutate(paramIdentifierToken, c);
            }
        }

        executeBlock(fnSymbols, body);

        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt statement) {
        Expression returnExpr = statement.expression;

        if (returnExpr != null)
            evaluate(returnExpr);

        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt statement) {
        Expression ifCondition = statement.ifExpression;
        List<Statement> ifStatements = statement.ifStatements;
        List<Statement> elseStatements = statement.elseStatements;

        evaluate(ifCondition);

        executeBlock(new Symbols<>(locals), ifStatements);

        if (elseStatements != null)
            executeBlock(new Symbols<>(locals), elseStatements);

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt statement) {
        Expression whileConditionExpr = statement.condition;
        List<Statement> body = statement.statements;

        evaluate(whileConditionExpr);
        executeBlock(new Symbols<>(locals), body);

        return null;
    }

    @Override
    public Void visitBreakStmt(BreakStmt statement) {
        return null;
    }

    @Override
    public Void visitContinueStmt(ContinueStmt statement) {
        return null;
    }

    private void evaluate(Expression expression) {
        expression.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private void executeBlock(Symbols<NameContainer> symbols, List<Statement> statements) {
        final Symbols<NameContainer> previous = locals;
        locals = symbols;

        for (Statement statement : statements)
            execute(statement);

        locals = previous;
    }

    private NameContainer get(Token identifier) {
        return locals.get(identifier);
    }

    private void declare(Token identifier, NameContainer container) {
        locals = locals.declare(identifier, container);
    }

    public void resolve(List<Statement> statements) {
        for (Statement statement : statements)
            execute(statement);
    }
}
