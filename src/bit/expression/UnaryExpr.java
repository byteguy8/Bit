package bit.expression;

import bit.BitType;
import bit.Token;

public class UnaryExpr extends Expression{
    public BitType type;
    public int counter = 0;
    public boolean isLast = false;

    public final Token operator;
    public final Expression right;

    public UnaryExpr(Token operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}