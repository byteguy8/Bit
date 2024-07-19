package bit.statement;

import bit.Token;

public final class BreakStmt extends Statement {
    public final Token breakToken;

    public BreakStmt(Token breakToken) {
        this.breakToken = breakToken;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBreakStmt(this);
    }
}