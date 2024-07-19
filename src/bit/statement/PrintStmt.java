package bit.statement;

import bit.BitType;
import bit.Token;

public final class PrintStmt extends Statement {
    public BitType type;

    public final Token printToken;
    public final bit.expression.Expression value;

    public PrintStmt(Token printToken, bit.expression.Expression value) {
        this.printToken = printToken;
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitPrintStmt(this);
    }
}