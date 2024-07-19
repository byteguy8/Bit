package bit.statement;

public final class ExprStmt extends Statement {
    public final bit.expression.Expression expression;

    public ExprStmt(bit.expression.Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitExpressionStmt(this);
    }
}