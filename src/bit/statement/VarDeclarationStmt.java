package bit.statement;

import bit.BitType;
import bit.Token;

public final class VarDeclarationStmt extends Statement {
    public BitType type;
    public boolean isGlobal;

    public final Token typeToken;
    public final Token identifierToken;
    public final bit.expression.Expression initializer;

    public VarDeclarationStmt(
            Token typeToken,
            Token identifierToken,
            bit.expression.Expression initializer) {
        this.typeToken = typeToken;
        this.identifierToken = identifierToken;
        this.initializer = initializer;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVarDeclarationStmt(this);
    }
}