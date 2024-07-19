package bit.expression;

import bit.Token;

public final class LogicalExpr extends Expression {
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public LogicalExpr(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitLogicalExpr(this);
    }
}