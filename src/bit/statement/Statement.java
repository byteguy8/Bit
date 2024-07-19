package bit.statement;

public abstract class Statement {
    public interface Visitor<T> {

        T visitExpressionStmt(ExprStmt statement);

        T visitVarDeclarationStmt(VarDeclarationStmt statement);

        T visitPrintStmt(PrintStmt statement);

        T visitBlockStmt(BlockStmt statement);

        T visitFnDeclarationStmt(FnDeclarationStmt statement);

        T visitReturnStmt(ReturnStmt statement);

        T visitIfStmt(IfStmt statement);

        T visitWhileStmt(WhileStmt statement);

        T visitBreakStmt(BreakStmt statement);

        T visitContinueStmt(ContinueStmt statement);
    }

    public abstract <T> T accept(Visitor<T> visitor);
}