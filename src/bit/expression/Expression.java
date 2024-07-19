package bit.expression;

public abstract class Expression {
    public interface Visitor<T> {
        T visitLiteralExpr(Literal expr);

        T visitAssignmentExpr(AssignmentExpr expr);

        T visitIdentifierExpr(IdentifierExpr expr);

        T visitBinaryExpr(BinaryExpr expr);

        T visitCallExpr(CallExpr expr);

        T visitComparisonExpr(Comparison expr);

        T visitLogicalExpr(LogicalExpr expr);

        T visitEqualityExpr(EqualityExpr expr);

        T visitUnaryExpr(UnaryExpr expr);
    }

    public abstract <T> T accept(Visitor<T> visitor);
}