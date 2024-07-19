package bit.statement;

import bit.Token;

import java.util.List;

public final class WhileStmt extends Statement {
    public final Token whileToken;
    public final bit.expression.Expression condition;
    public final List<Statement> statements;
    public final boolean containsBreak;

    public WhileStmt(
            Token whileToken,
            bit.expression.Expression condition,
            List<Statement> statements,
            boolean containsBreak) {
        this.whileToken = whileToken;
        this.condition = condition;
        this.statements = statements;
        this.containsBreak = containsBreak;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}