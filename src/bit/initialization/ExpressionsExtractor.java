package bit.initialization;

import bit.expression.*;
import bit.statement.*;

import java.util.ArrayList;
import java.util.List;

public class ExpressionsExtractor implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private List<Expression> expressions = new ArrayList<>();

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitAssignmentExpr(AssignmentExpr expr) {
        evaluate(expr.left);
        evaluate(expr.left);

        return null;
    }

    @Override
    public Void visitIdentifierExpr(IdentifierExpr expr) {
        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr expr) {
        evaluate(expr.left);
        evaluate(expr.left);

        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr expr) {
        evaluate(expr.left);

        if (expr.arguments != null) {
            for (Expression argument : expr.arguments) {
                evaluate(argument);
            }
        }

        return null;
    }

    @Override
    public Void visitComparisonExpr(Comparison expr) {
        evaluate(expr.left);
        evaluate(expr.left);

        return null;
    }

    @Override
    public Void visitLogicalExpr(LogicalExpr expr) {
        evaluate(expr.left);
        evaluate(expr.left);

        return null;
    }

    @Override
    public Void visitEqualityExpr(EqualityExpr expr) {
        evaluate(expr.left);
        evaluate(expr.left);

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
        if(statement.initializer != null)
            evaluate(statement.initializer);

        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt statement) {
        evaluate(statement.value);

        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt statement) {
        for (Statement stmt : statement.statements)
            execute(stmt);

        return null;
    }

    @Override
    public Void visitFnDeclarationStmt(FnDeclarationStmt statement) {
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt statement) {
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt statement) {
        evaluate(statement.ifConditionExpr);

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt statement) {
        evaluate(statement.condition);

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
        expressions.add(expression);

        expression.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    public void extract(Statement statement) {
        execute(statement);
    }

    public List<Expression> getExpressions() {
        return expressions;
    }
}
