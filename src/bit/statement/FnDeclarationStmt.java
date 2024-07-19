package bit.statement;

import bit.BitType;
import bit.Token;

import java.util.List;

public final class FnDeclarationStmt extends Statement {
    public static final class FnParam {
        public final Token type;
        public final Token identifier;

        public FnParam(Token type, Token identifier) {
            this.type = type;
            this.identifier = identifier;
        }
    }

    public List<BitType> paramsTypes;

    public final boolean export;
    public final Token fnToken;
    public final Token identifierToken;
    public final List<FnParam> params;
    public final Token returnTypeToken;
    public final List<Statement> body;

    public FnDeclarationStmt(
            boolean export,
            Token fnToken,
            Token identifierToken,
            List<FnParam> params,
            Token returnTypeToken,
            List<Statement> body) {
        this.export = export;
        this.fnToken = fnToken;
        this.identifierToken = identifierToken;
        this.params = params;
        this.returnTypeToken = returnTypeToken;
        this.body = body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFnDeclarationStmt(this);
    }
}