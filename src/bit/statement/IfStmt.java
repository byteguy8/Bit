package bit.statement;

import bit.Token;

import java.util.List;

public final class IfStmt extends Statement {
    public final Token ifToken;
    public final bit.expression.Expression ifConditionExpr;
    public final List<Statement> ifStatements;
    public final List<Statement> elseStatements;

    public IfStmt(Token ifToken, bit.expression.Expression ifConditionExpr, List<Statement> ifStatements, List<Statement> elseStatements) {
        this.ifToken = ifToken;
        this.ifConditionExpr = ifConditionExpr;
        this.ifStatements = ifStatements;
        this.elseStatements = elseStatements;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}