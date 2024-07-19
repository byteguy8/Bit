package bit.statement;

import java.util.List;

public final class BlockStmt extends Statement {
    public final List<Statement> statements;

    public BlockStmt(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitBlockStmt(this);
    }
}