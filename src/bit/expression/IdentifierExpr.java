package bit.expression;

import bit.Token;

public final class IdentifierExpr extends Expression {
    public boolean isGlobal = false;

    public final Token identifier;

    public IdentifierExpr(Token identifier) {
        this.identifier = identifier;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIdentifierExpr(this);
    }
}