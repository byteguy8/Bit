package bit.expression;

import bit.BitType;

public final class Literal extends Expression {
    public BitType type;
    public final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}