package bit.initialization;

import bit.Bit;
import bit.ResolverError;
import bit.Token;
import bit.expression.*;
import bit.statement.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitializationChecker implements Expression.Visitor<InitializationHolder>, Statement.Visitor<Void> {
    private final InitializationSymbols global = new InitializationSymbols();
    private InitializationSymbols locals = global;

    private int depth = 0;
    private List<InitializationContainer> scopeSymbols = new ArrayList<>();

    private boolean checkInitialized = true;

    @Override
    public InitializationHolder visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public InitializationHolder visitAssignmentExpr(AssignmentExpr expr) {
        final boolean previousCheckInitialized = checkInitialized;
        checkInitialized = false;

        InitializationHolder initializationHolder = evaluate(expr.left);

        checkInitialized = previousCheckInitialized;

        if (initializationHolder == null)
            throw error(expr.equlasToken, "Illegal assignment target");

        InitializationContainer initializationContainer = initializationHolder.getContainer();

        locals.setInitialized(initializationContainer.getIdentifier());

        return null;
    }

    @Override
    public InitializationHolder visitIdentifierExpr(IdentifierExpr expr) {
        if (checkInitialized && !locals.isInitialized(expr.identifier))
            throw error(expr.identifier, "Illegal use of entity: is not initialized.");

        return new InitializationHolder(locals.get(expr.identifier));
    }

    @Override
    public InitializationHolder visitBinaryExpr(BinaryExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return new InitializationHolder(null);
    }

    @Override
    public InitializationHolder visitCallExpr(CallExpr expr) {
        evaluate(expr.left);

        if (expr.arguments != null)
            for (Expression argument : expr.arguments)
                evaluate(argument);

        return null;
    }

    @Override
    public InitializationHolder visitComparisonExpr(Comparison expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public InitializationHolder visitLogicalExpr(LogicalExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public InitializationHolder visitEqualityExpr(EqualityExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        return null;
    }

    @Override
    public InitializationHolder visitUnaryExpr(UnaryExpr expr) {
        evaluate(expr.right);

        return null;
    }

    @Override
    public Void visitExpressionStmt(ExprStmt statement) {
        evaluate(statement.expression);

        return null;
    }

    @Override
    public Void visitVarDeclarationStmt(VarDeclarationStmt statement) {
        declare(statement.identifierToken);

        if (statement.initializer != null)
            locals.setInitialized(statement.identifierToken);

        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt statement) {
        evaluate(statement.value);

        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt statement) {
        executeBlock(statement.statements);

        return null;
    }

    @Override
    public Void visitFnDeclarationStmt(FnDeclarationStmt statement) {
        declare(statement.identifierToken);
        locals.setInitialized(statement.identifierToken);

        final InitializationSymbols previousLocals = locals;
        locals = new InitializationSymbols(locals);

        if (statement.params != null) {
            for (FnDeclarationStmt.FnParam param : statement.params) {
                declare(param.identifier);
                locals.setInitialized(param.identifier);
            }
        }

        for (Statement stmt : statement.body)
            execute(stmt);

        locals = previousLocals;

        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt statement) {
        if (statement.expression != null)
            evaluate(statement.expression);

        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt statement) {
        evaluate(statement.ifConditionExpr);

        executeBlock(statement.ifStatements);

        if (statement.elseStatements != null)
            executeBlock(statement.elseStatements);

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt statement) {
        evaluate(statement.condition);

        executeBlock(statement.statements);

        return null;
    }

    @Override
    public Void visitBreakStmt(BreakStmt statement) {
        return null;
    }

    @Override
    public Void visitContinueStmt(ContinueStmt statement) {
        return null;
    }

    private ResolverError error(Token token, String message) {
        Bit.error(token, message);
        return new ResolverError();
    }

    private void declare(Token identifier) {
        InitializationContainer initializationContainer = new InitializationContainer(identifier);

        locals = locals.declare(identifier, initializationContainer);
    }

    private List<AssignmentExpr> checkForAssignments(Statement statement) {
        List<AssignmentExpr> exprs = new ArrayList<>();
        ExpressionsExtractor extractor = new ExpressionsExtractor();

        extractor.extract(statement);

        for (Expression expression : extractor.getExpressions()) {
            if (expression instanceof AssignmentExpr)
                exprs.add((AssignmentExpr) expression);
        }

        return exprs;
    }

    private InitializationContainer[] resolveIfInitializationBranch(List<Statement> statements) {
        Map<InitializationContainer, Void> symbols = new HashMap<>();

        for (Statement stmt : statements) {
            if (stmt instanceof BlockStmt) {
                InitializationContainer[] verifiedInitializationContainers = resolveIfInitializationBranch(((BlockStmt) stmt).statements);

                for (InitializationContainer verifiedInitializationContainer : verifiedInitializationContainers) {
                    if (symbols.containsKey(verifiedInitializationContainer))
                        continue;

                    symbols.put(verifiedInitializationContainer, null);
                }

                continue;
            }

            if (stmt instanceof IfStmt) {
                InitializationContainer[] verifiedInitializationContainers = checkIfInitializations((IfStmt) stmt);

                for (InitializationContainer verifiedInitializationContainer : verifiedInitializationContainers) {
                    if (symbols.containsKey(verifiedInitializationContainer))
                        continue;

                    symbols.put(verifiedInitializationContainer, null);
                }

                continue;
            }

            List<AssignmentExpr> exprs = checkForAssignments(stmt);

            for (AssignmentExpr expr : exprs) {
                InitializationHolder initializationHolder = evaluate(expr.left);
                InitializationContainer initializationContainer = initializationHolder.getContainer();

                if (symbols.containsKey(initializationContainer))
                    continue;

                symbols.put(initializationContainer, null);
            }
        }

        return symbols.keySet().toArray(new InitializationContainer[]{});
    }

    private InitializationContainer[] checkIfInitializations(IfStmt ifStmt) {
        final boolean enableNotifyUninitialized = checkInitialized;

        if (enableNotifyUninitialized)
            checkInitialized = false;

        Map<InitializationContainer, Integer> initializations = new HashMap<>();

        List<Statement> ifStmts = ifStmt.ifStatements;
        List<Statement> elseStmts = ifStmt.elseStatements;

        for (InitializationContainer initializationContainer : resolveIfInitializationBranch(ifStmts)) {
            initializations.put(initializationContainer, 1);
        }

        if (elseStmts != null) {
            for (InitializationContainer initializationContainer : resolveIfInitializationBranch(elseStmts)) {
                if (initializations.containsKey(initializationContainer)) {
                    initializations.put(initializationContainer, 2);
                    continue;
                }

                initializations.put(initializationContainer, 1);
            }
        }

        List<InitializationContainer> verifiedInitializationContainers = new ArrayList<>();

        for (Map.Entry<InitializationContainer, Integer> set : initializations.entrySet())
            if (set.getValue() == 2)
                verifiedInitializationContainers.add(set.getKey());

        if (enableNotifyUninitialized)
            checkInitialized = true;

        return verifiedInitializationContainers.toArray(new InitializationContainer[]{});
    }

    private InitializationHolder evaluate(Expression expression) {
        return expression.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private void executeBlock(List<Statement> statements) {
        final InitializationSymbols previousLocals = locals;
        locals = new InitializationSymbols(locals);

        depth++;

        for (Statement statement : statements) {
            if (statement instanceof IfStmt) {
                InitializationContainer[] initializationContainers = checkIfInitializations((IfStmt) statement);

                for (InitializationContainer initializationContainer : initializationContainers)
                    locals.setInitialized(initializationContainer.getIdentifier());
            }

            execute(statement);
        }

        depth--;

        locals = previousLocals;
    }

    private void executeBlockSymbols(InitializationSymbols symbols, List<Statement> statements) {
        final InitializationSymbols previous = locals;
        locals = symbols;

        for (Statement statement : statements)
            execute(statement);

        locals = previous;
    }

    public void check(List<Statement> statements) {
        for (Statement statement : statements) {
            if (statement instanceof IfStmt) {
                InitializationContainer[] initializationContainers = checkIfInitializations((IfStmt) statement);

                for (InitializationContainer initializationContainer : initializationContainers)
                    locals.setInitialized(initializationContainer.getIdentifier());
            }

            execute(statement);
        }
    }
}
