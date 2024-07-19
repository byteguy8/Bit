package bit.expression;

import bit.BitType;
import bit.Token;

import java.util.List;

public final class CallExpr extends Expression {
    public BitType returnType;
    public List<BitType> argumentsTypes;

    public final Expression left;
    public final Token leftParenToken;
    public final List<Expression> arguments;

    public CallExpr(Expression left, Token leftParenToken, List<Expression> arguments) {
        this.left = left;
        this.leftParenToken = leftParenToken;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}