package bit.statement;

import bit.BitType;
import bit.Token;

public final class ReturnStmt extends Statement {
    public BitType returnType;

    public final Token returnToken;
    public final bit.expression.Expression expression;

    public ReturnStmt(Token returnToken, bit.expression.Expression expression) {
        this.returnToken = returnToken;
        this.expression = expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitReturnStmt(this);
    }
}