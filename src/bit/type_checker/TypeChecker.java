package bit.type_checker;

import bit.*;
import bit.expression.*;
import bit.statement.*;

import java.util.*;

public class TypeChecker implements Expression.Visitor<BitType>, Statement.Visitor<Void> {
    private final Symbols<CheckerContainer> globals = new Symbols<>();
    private Symbols<CheckerContainer> locals = globals;

    private int unaryCount = 0;
    private boolean unaryFlag = false;
    private final List<UnaryExpr> unaryExprs = new ArrayList<>();

    private boolean isCalling = false;
    private final Stack<CheckerFunction> callStack = new Stack<>();
    private final Stack<CheckerFunction> functionStack = new Stack<>();

    @Override
    public BitType visitLiteralExpr(Literal expr) {
        Object value = expr.value;

        BitType type;

        if (value instanceof Boolean)
            type = BitType.BOOL;
        else if (value instanceof Number)
            type = BitType.INT;
        else if (value instanceof String)
            type = BitType.STR;
        else
            throw new IllegalStateException("Unsupported value type");

        expr.type = type;

        return type;
    }

    @Override
    public BitType visitAssignmentExpr(AssignmentExpr expr) {
        BitType leftType = evaluate(expr.left);
        Token equalsToken = expr.equlasToken;
        BitType rightType = evaluate(expr.right);

        if (leftType != rightType)
            throw error(equalsToken, String.format("Left type '%s' do not match right type '%s'", leftType, rightType));

        return leftType;
    }

    @Override
    public BitType visitIdentifierExpr(IdentifierExpr expr) {
        Token identifierToken = expr.identifier;

        CheckerContainer container = get(identifierToken);

        if (isCalling) {
            CheckerFunction function = (CheckerFunction) container.getValue();
            callStack.push(function);
        }

        return get(identifierToken).getType();
    }

    @Override
    public BitType visitBinaryExpr(BinaryExpr expr) {
        BitType leftType = evaluate(expr.left);
        Token operatorToken = expr.operator;
        BitType rightType = evaluate(expr.right);

        if (leftType == BitType.INT && rightType == BitType.INT) {
            expr.type = BitType.INT;
            return BitType.INT;
        }

        throw error(operatorToken, String.format("Unexpected left type '%s', and right type '%s', with operator %s", leftType, rightType, operatorToken.lexeme));
    }

    @Override
    public BitType visitCallExpr(CallExpr expr) {
        Expression leftExpr = expr.left;
        Token leftParenToken = expr.leftParenToken;
        List<Expression> arguments = expr.arguments;

        final boolean previousIsCalling = isCalling;
        isCalling = true;

        BitType type = evaluate(leftExpr);

        isCalling = previousIsCalling;

        if (type != BitType.FUNCTION)
            throw error(leftParenToken, String.format("Expect a function, but got '%s'", type));

        int argsCount = arguments == null ? 0 : arguments.size();

        CheckerFunction function = callStack.pop();
        List<BitType> paramsTypes = function.getParams();

        if (argsCount != paramsTypes.size())
            throw error(leftParenToken, String.format("Function expect %d arguments, but got %d", paramsTypes.size(), argsCount));

        if (arguments != null) {
            List<BitType> argumentsTypes = new ArrayList<>();

            for (int i = 0; i < arguments.size(); i++) {
                Expression argExpr = arguments.get(i);
                BitType argType = evaluate(argExpr);
                BitType paramType = paramsTypes.get(i);

                if (argType != paramType)
                    throw error(leftParenToken, String.format("Function parameter %d is of type '%s', but passed argument is of type '%s'", i, paramType, argType));

                argumentsTypes.add(argType);
            }

            expr.argumentsTypes = argumentsTypes;
        }

        expr.returnType = function.getReturnType();

        return function.getReturnType();
    }

    @Override
    public BitType visitComparisonExpr(Comparison expr) {
        BitType leftType = evaluate(expr.left);
        Token operatorToken = expr.operator;
        BitType rightType = evaluate(expr.right);

        if (leftType == BitType.INT && rightType == BitType.INT)
            return BitType.BOOL;

        throw error(operatorToken, String.format("Unexpected left type '%s', and right type '%s', with operator %s", leftType, rightType, operatorToken.lexeme));
    }

    @Override
    public BitType visitLogicalExpr(LogicalExpr expr) {
        BitType leftType = evaluate(expr.left);
        Token operatorToken = expr.operator;
        BitType rightType = evaluate(expr.right);

        if (leftType == BitType.BOOL && rightType == BitType.BOOL)
            return BitType.BOOL;

        throw error(operatorToken, String.format("Unexpected left type '%s', and right type '%s', with operator %s", leftType, rightType, operatorToken.lexeme));
    }

    @Override
    public BitType visitEqualityExpr(EqualityExpr expr) {
        BitType leftType = evaluate(expr.left);
        Token operatorToken = expr.operator;
        BitType rightType = evaluate(expr.right);

        if (leftType == BitType.INT && rightType == BitType.INT)
            return BitType.BOOL;

        throw error(operatorToken, String.format("Unexpected left type '%s', and right type '%s', with operator %s", leftType, rightType, operatorToken.lexeme));
    }

    @Override
    public BitType visitUnaryExpr(UnaryExpr expr) {
        final boolean isFirst = !unaryFlag;
        if (isFirst) unaryFlag = true;

        expr.counter = unaryCount++;
        unaryExprs.add(expr);

        Token operatorToken = expr.operator;
        BitType rightType = evaluate(expr.right);

        expr.type = rightType;

        if (operatorToken.type == TokenType.BANG && rightType == BitType.BOOL) {
            if (isFirst) {
                unaryCount = 0;
                unaryFlag = false;

                UnaryExpr lastExpr = unaryExprs.get(unaryExprs.size() - 1);
                lastExpr.isLast = true;

                unaryExprs.clear();
            }

            return BitType.BOOL;
        }

        if (operatorToken.type == TokenType.MINUS && rightType == BitType.INT)
            return BitType.INT;

        throw error(operatorToken, String.format("Unexpected right type '%s' with operator %s", rightType, operatorToken.lexeme));
    }

    @Override
    public Void visitExpressionStmt(ExprStmt statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitVarDeclarationStmt(VarDeclarationStmt statement) {
        Token typeToken = statement.typeToken;
        Token identifierToken = statement.identifierToken;
        Expression initializerExpr = statement.initializer;

        BitType leftType = tokenToType(typeToken);
        BitType rightType = initializerExpr == null ? leftType : evaluate(initializerExpr);

        if (leftType != rightType)
            throw error(identifierToken, String.format("Left type '%s' do not match right type '%s'", leftType, rightType));

        declare(identifierToken, leftType, null);

        statement.type = leftType;

        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt statement) {
        statement.type = evaluate(statement.value);
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt statement) {
        executeBlock(new Symbols<>(locals), statement.statements);
        return null;
    }

    @Override
    public Void visitFnDeclarationStmt(FnDeclarationStmt statement) {
        Token identifierToken = statement.identifierToken;
        List<FnDeclarationStmt.FnParam> parameters = statement.params;
        Token returnTypeToken = statement.returnTypeToken;
        List<Statement> body = statement.body;

        BitType returnType = returnTypeToken == null ? BitType.VOID : tokenToType(returnTypeToken);
        CheckerFunction function = new CheckerFunction(returnType);

        declare(identifierToken, BitType.FUNCTION, function);
        Symbols<CheckerContainer> functionEnvironment = new Symbols<>(locals);

        List<BitType> parametersType = new ArrayList<>();

        if (parameters != null) {
            for (FnDeclarationStmt.FnParam parameter : parameters) {
                Token paramTypeToken = parameter.type;
                Token paramIdentifierToken = parameter.identifier;
                BitType paramType = tokenToType(paramTypeToken);

                CheckerContainer container = new CheckerContainer(paramType, null);

                functionEnvironment.declareMutate(paramIdentifierToken, container);
                parametersType.add(paramType);
            }
        }

        statement.paramsTypes = parametersType;
        function.setParams(parametersType);

        functionStack.push(function);

        functionEnvironment = executeBlock(functionEnvironment, body);

        final Symbols<CheckerContainer> previousLocals = locals;
        locals = functionEnvironment;

        if (function.getReturnType() != BitType.VOID) {
            if (!checkBlockResolves(body, function.getReturnType()))
                throw error(identifierToken, "Function declared to return a not void type but no return statement found");
        }

        locals = previousLocals;

        functionStack.pop();

        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt statement) {
        Token returnToken = statement.returnToken;
        Expression returnExpr = statement.expression;

        if (functionStack.isEmpty())
            throw error(returnToken, "Return statement can't be used outside a function");

        BitType returnType = null;

        if (returnExpr != null) {
            returnType = evaluate(returnExpr);
            CheckerFunction function = functionStack.peek();

            if (returnType != function.getReturnType())
                throw error(returnToken, String.format("Function return type '%s' doesn't match with returned value type '%s'", function.getReturnType(), returnType));
        }

        statement.returnType = returnType;

        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt statement) {
        Token ifToken = statement.ifToken;
        Expression ifConditionExpr = statement.ifConditionExpr;
        List<Statement> ifBody = statement.ifStatements;
        List<Statement> elseBody = statement.elseStatements;

        BitType ifConditionType = evaluate(ifConditionExpr);

        if (ifConditionType != BitType.BOOL)
            throw error(ifToken, String.format("Expect bool value type in if condition, but got '%s'", ifConditionType));

        executeBlock(new Symbols<>(locals), ifBody);

        if (elseBody != null)
            executeBlock(new Symbols<>(locals), elseBody);

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt statement) {
        Token whileToken = statement.whileToken;
        Expression conditionExpr = statement.condition;
        List<Statement> body = statement.statements;

        BitType conditionType = evaluate(conditionExpr);

        if (conditionType != BitType.BOOL)
            throw error(whileToken, String.format("Expect a bool type value in while condition statement, but got '%s'", conditionType));

        executeBlock(new Symbols<>(locals), body);

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

    private void clear() {
        unaryCount = 0;
    }

    private BitType tokenToType(Token token) {
        TokenType type = token.type;

        switch (type) {
            case BOOL_TYPE:
                return BitType.BOOL;
            case INT_TYPE:
                return BitType.INT;
            case STR_TYPE:
                return BitType.STR;
            default:
                throw new IllegalArgumentException(String.format("Illegal token type '%s'", token.lexeme));
        }
    }

    /**
     * Validates a block (a list of statements) to check if resolves.
     * A block resolves if returns something of the correct given type.
     *
     * @param statements The block.
     * @param type       The type to check.
     * @return true if resolves, false if not.
     */
    private boolean checkBlockResolves(List<Statement> statements, BitType type) {
        for (int i = 0; i < statements.size(); i++) {
            Statement stmt = statements.get(i);

            if (stmt instanceof IfStmt) {
                IfStmt is = (IfStmt) stmt;

                if (checkIfReturn(is, type)) {
                    if (i < statements.size() - 1)
                        throw error(is.ifToken, "If statement resolves but not the last statement in the block");

                    return true;
                }
            }

            if (stmt instanceof ReturnStmt) {
                ReturnStmt rs = (ReturnStmt) stmt;

                Token retToken = rs.returnToken;
                Expression retExpr = rs.expression;

                BitType retType = retExpr == null ? BitType.VOID : evaluate(retExpr);

                if (i < statements.size() - 1)
                    throw error(retToken, "Return statement must be the last statement in a block");

                if (retType != type)
                    throw error(retToken, String.format("Declared return type '%s' don't match returned value type '%s'", type, retType));

                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a given if statements resolves.
     * An if statement resolves when all its branch return something.
     * <br><br>
     * This functions check further possibles if statements inside each
     * branch of the given if statement. In that case, the outer branch
     * resolves if the inner does.
     * <br><br>
     * In case on of the given branch of the passed if statement contains
     * an if statement, that inner if statement must be the end statement.
     * This function validates that too.
     *
     * @param ifStmt The if statement to check.
     * @param type   The type that then if statement must check for.
     * @return true if resolves and false if not.
     */
    private boolean checkIfReturn(IfStmt ifStmt, BitType type) {
        List<Statement> ifStmts = ifStmt.ifStatements;
        List<Statement> elseStmts = ifStmt.elseStatements;

        int retCount = 0;

        if (checkBlockResolves(ifStmts, type))
            retCount++;

        if (elseStmts != null)
            if (checkBlockResolves(elseStmts, type))
                retCount++;

        return retCount == 2;
    }

    private void declare(Token identifier, BitType type, Object value) {
        CheckerContainer container = new CheckerContainer(type, value);
        locals = locals.declare(identifier, container);
    }

    private CheckerContainer get(Token identifier) {
        return locals.get(identifier);
    }

    private BitType evaluate(Expression expression) {
        return expression.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private Symbols<CheckerContainer> executeBlock(Symbols<CheckerContainer> environment, List<Statement> statements) {
        final Symbols<CheckerContainer> previous = locals;
        locals = environment;

        Symbols<CheckerContainer> lastEnvironment;

        try {
            for (Statement statement : statements)
                execute(statement);
        } finally {
            lastEnvironment = locals;
            locals = previous;
        }

        return lastEnvironment;
    }

    public void check(List<Statement> statements) {
        for (Statement statement : statements)
            execute(statement);
    }
}
