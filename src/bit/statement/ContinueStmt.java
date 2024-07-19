package bit.statement;

import bit.Token;

public final class ContinueStmt extends Statement{
    public final Token continueToken;

    public ContinueStmt(Token continueToken) {
        this.continueToken = continueToken;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitContinueStmt(this);
    }
}