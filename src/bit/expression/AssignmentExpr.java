package bit.expression;

import bit.Token;

public final class AssignmentExpr extends Expression {
    public final Expression left;
    public final Token equlasToken;
    public final Expression right;

    public AssignmentExpr(Expression left, Token equlasToken, Expression right) {
        this.left = left;
        this.equlasToken = equlasToken;
        this.right = right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitAssignmentExpr(this);
    }
}