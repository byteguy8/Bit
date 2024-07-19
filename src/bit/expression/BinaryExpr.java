package bit.expression;

import bit.BitType;
import bit.Token;

public final class BinaryExpr extends Expression {
    public BitType type;

    public final Expression left;
    public final Token operator;
    public final Expression right;

    public BinaryExpr(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBinaryExpr(this);
    }
}