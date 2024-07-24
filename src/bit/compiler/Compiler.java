package bit.compiler;

import bit.*;
import bit.expression.*;
import bit.instruction.*;
import bit.instruction.arithmetic.Add;
import bit.instruction.arithmetic.Div;
import bit.instruction.arithmetic.Mul;
import bit.instruction.arithmetic.Sub;
import bit.instruction.conditional.*;
import bit.instruction.logical.And;
import bit.instruction.logical.Or;
import bit.instruction.stack.Epilogue;
import bit.instruction.stack.Prologue;
import bit.memory.CompilerRegister;
import bit.memory.address.Address;
import bit.memory.address.Global;
import bit.memory.address.type.LabelAddress;
import bit.memory.address.type.QuadWord;
import bit.memory.address.type.Register;
import bit.memory.address.type.StackVariable;
import bit.WordSize;
import bit.statement.*;

import java.util.*;

import static bit.Utils.registerBySize;

public class Compiler implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final int[] paramsIndexes = {5, 4, 3, 2, 8, 9};

    private int ifTags = 0;
    private int exitTags = 0;
    private int whileTags = 0;
    private int stringsCounter = 0;

    private boolean isAssigning = false;
    private Object assignTarget = null;

    private String whileBreakLabel = null;
    private String whileConditionalLabel = null;

    private long scopeId = 0;
    private Map<Long, Map<String, BitType>> scopes = null;
    private final Map<String, GlobalSymbol> globals = new HashMap<>();

    private final Stack<Frame> frames = new Stack<>();

    private final List<Instruction> externSection = new ArrayList<>();
    private final List<Instruction> dataSection = new ArrayList<>();
    private final List<Instruction> textSection = new ArrayList<>();

    private final List<List<Instruction>> functions = new ArrayList<>();

    private final String PRINT_INT_FMT = "print_int_fmt";
    private final String PRINT_LONG_FMT = "print_long_fmt";
    private final String PRINT_STRING_FMT = "print_string_fmt";

    {
        frames.push(new Frame());
    }

    private BitType getType(Token identifier) {
        Map<String, BitType> scope = scopes.get(scopeId);
        return scope.get(identifier.lexeme);
    }

    private Frame currentFrame() {
        return frames.peek();
    }

    private List<Instruction> currentInstructions() {
        return currentFrame().getInstructions();
    }

    public int newIfTag() {
        return ifTags++;
    }

    public int newExitTag() {
        return exitTags++;
    }

    public int newWhileTag() {
        return whileTags++;
    }

    private String generateIfLabel() {
        return String.format("IFLB_%d", newIfTag());
    }

    private String generateWhileLabel() {
        return String.format("WHLB_%d", newWhileTag());
    }

    private String generateExitLabel() {
        return String.format("EXLB_%d", newExitTag());
    }

    private CompilerRegister addSubResultReg(WordSize size) {
        switch (size) {
            case BYTE:
                return CompilerRegister.al;

            case WORD:
                return CompilerRegister.ax;

            case DOUBLE_WORD:
                return CompilerRegister.eax;

            case QUAD_WORD:
                return CompilerRegister.rax;

            default:
                throw new IllegalArgumentException("Illegal size");
        }
    }

    private CompilerRegister divMulResultReg(WordSize size) {
        switch (size) {
            case BYTE:
                return CompilerRegister.al;

            case WORD:
                return CompilerRegister.ax;

            case DOUBLE_WORD:
                return CompilerRegister.eax;

            case QUAD_WORD:
                return CompilerRegister.rax;

            default:
                throw new IllegalArgumentException("Illegal size");
        }
    }

    private FrameLine push(WordSize size) {
        return currentFrame().push(size);
    }

    private FrameLine pop() {
        return currentFrame().pop();
    }

    private FrameLine peek() {
        return currentFrame().peek();
    }

    private FrameLine getStack(String name) {
        Frame frame = frames.peek();
        return frame.get(name);
    }

    private FrameLine declareStack(WordSize size, String name) {
        Frame frame = frames.peek();
        return frame.declare(size, name);
    }

    private void declareGlobal(String name, BitType type, WordSize size) {
        if (globals.containsKey(name))
            throw new IllegalArgumentException(String.format("Global symbol '%s' already exists", name));

        globals.put(name, new GlobalSymbol(type, size, name));
    }

    private GlobalSymbol getGlobal(String name) {
        if (!globals.containsKey(name))
            throw new IllegalArgumentException(String.format("Global symbol '%s' doesn't exists", name));

        return globals.get(name);
    }

    private void addInstruction(Instruction instruction) {
        currentInstructions().add(instruction);
    }

    private void comment(String comment, boolean header) {
        addInstruction(new Comment(comment, header));
    }

    private void extern(String name) {
        externSection.add(new Extern(name));
    }

    private void data(String name, WordSize size, Object value) {
        dataSection.add(new SectionData(name, size, value));
    }

    private void dataStr(String name, String value) {
        data(name, WordSize.BYTE, String.format("\"%s\", 10, 0", value));
    }

    private String str(String value) {
        String name = String.format("str_%d", stringsCounter++);
        data(name, WordSize.BYTE, String.format("\"%s\", 0", value));
        return name;
    }

    private void sectionText(boolean global, String value) {
        textSection.add(new SectionText(global, value));
    }

    private void prologue() {
        addInstruction(new Prologue());
    }

    private void epilogue() {
        addInstruction(new Epilogue());
    }

    private void call(String name) {
        addInstruction(new Call(name));
    }

    private void ret() {
        addInstruction(new Ret());
    }

    private int alignment() {
        addInstruction(new Sub(new Register(CompilerRegister.rsp), literal(0L)));
        return currentInstructions().size() - 1;
    }

    private void label(String name) {
        addInstruction(new Label(name));
    }

    public LabelAddress labelAddress(String label) {
        return new LabelAddress(label);
    }

    private void lea(WordSize size, Address left, Address right) {
        addInstruction(new Lea(size, left, right));
    }

    private void mov(WordSize size, Address left, Address right) {
        addInstruction(new Mov(size, left, right));
    }

    private void add(CompilerRegister left, CompilerRegister right) {
        addInstruction(new Add(new Register(left), new Register(right)));
    }

    private void sub(CompilerRegister left, CompilerRegister right) {
        addInstruction(new Sub(new Register(left), new Register(right)));
    }

    private void mul(CompilerRegister left) {
        addInstruction(new Mul(new Register(left)));
    }

    private void div(CompilerRegister left) {
        addInstruction(new Div(new Register(left)));
    }

    private void and(Address left, Address right) {
        addInstruction(new And(left, right));
    }

    private void or(Address left, Address right) {
        addInstruction(new Or(left, right));
    }

    private void cmp(CompilerRegister left, CompilerRegister right) {
        addInstruction(new Cmp((Register) register(left), (Register) register(right)));
    }

    private void setl(Address address) {
        addInstruction(new Setl(address));
    }

    private void setg(Address address) {
        addInstruction(new Setg(address));
    }

    private void setle(Address address) {
        addInstruction(new Setle(address));
    }

    private void setge(Address address) {
        addInstruction(new Setge(address));
    }

    private void sete(Address address) {
        addInstruction(new Sete(address));
    }

    private void setne(Address address) {
        addInstruction(new Setne(address));
    }

    private void je(LabelAddress address) {
        addInstruction(new Je(address));
    }

    private Jmp jmp(LabelAddress label) {
        Jmp instruction = new Jmp(label);

        addInstruction(instruction);

        return instruction;
    }

    private void print(WordSize size, Address address) {
        String formatter;

        if (size == WordSize.BYTE || size == WordSize.WORD || size == WordSize.DOUBLE_WORD)
            formatter = PRINT_INT_FMT;
        else
            formatter = PRINT_LONG_FMT;

        addInstruction(new Print(size, formatter, address));
    }

    private void printStr(Address address) {
        addInstruction(new Print(WordSize.QUAD_WORD, PRINT_STRING_FMT, address));
    }

    private QuadWord literal(long value) {
        return new QuadWord(value);
    }

    private Address register(CompilerRegister register) {
        return new Register(register);
    }

    private StackVariable stack(int position) {
        return new StackVariable(position);
    }

    private String compileInstructions(List<Instruction> instructions) {
        StringBuilder sb = new StringBuilder();

        for (Instruction instruction : instructions) {
            if (instruction instanceof Comment || instruction instanceof Label) {
                sb.append(String.format("%s\n", instruction.translate()));
                continue;
            }

            sb.append(String.format("%s\n", Utils.intent(4, instruction.translate())));
        }

        return sb.toString();
    }

    private void evaluate(Expression expression) {
        expression.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    private void executeSimpleBlock(List<Statement> statements) {
        currentFrame().nextScope();

        for (Statement statement : statements)
            execute(statement);

        currentFrame().previousScope();
    }

    private void executeFunctionBlock(List<Statement> statements) {
        frames.push(new Frame());

        for (Statement statement : statements)
            execute(statement);

        frames.pop();
    }

    public void setScopes(Map<Long, Map<String, BitType>> scopes) {
        this.scopes = scopes;
    }

    public String compile(List<Statement> statements) {
        extern("printf");

        dataStr(PRINT_INT_FMT, "%d");
        dataStr(PRINT_LONG_FMT, "%ld");
        dataStr(PRINT_STRING_FMT, "%s");

        sectionText(true, "main");

        prologue();
        int alignmentIndex = alignment();

        List<Statement> functionsDeclarations = new ArrayList<>();

        for (Statement statement : statements) {
            if (statement instanceof FnDeclarationStmt) {
                functionsDeclarations.add(statement);
                continue;
            }

            execute(statement);
        }

        for (Statement functionsDeclaration : functionsDeclarations)
            execute(functionsDeclaration);

        epilogue();
        ret();

        Sub alignment = (Sub) currentInstructions().get(alignmentIndex);
        alignment.right = literal(frames.peek().getAlignment());

        StringBuilder rawASM = new StringBuilder();

        rawASM.append(compileInstructions(externSection));

        rawASM.append("section .data\n");
        rawASM.append(compileInstructions(dataSection));

        rawASM.append("section .text\n");
        rawASM.append(compileInstructions(textSection));

        for (List<Instruction> function : functions)
            rawASM.append(compileInstructions(function));

        rawASM.append("main:\n");
        rawASM.append(compileInstructions(currentInstructions()));

        return rawASM.toString();
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        BitType type = expr.type;
        Object value = expr.value;

        WordSize size = Utils.bitTypeToSize(type);

        FrameLine line = push(size);

        line.type = type;

        if (type == BitType.BOOL)
            mov(size, stack(line.position), literal(((boolean) value) ? 1L : 0L));
        else if (type == BitType.INT)
            mov(size, stack(line.position), literal((long) value));
        else {
            String str = (String) value;
            String name = str(str);

            line.payload = name;

            CompilerRegister register = registerBySize(size, 0);

            lea(size, register(register), new Global(name));
            mov(size, stack(line.position), register(register));
        }

        return null;
    }

    @Override
    public Void visitAssignmentExpr(AssignmentExpr expr) {
        Expression left = expr.left;
        Expression right = expr.right;

        isAssigning = true;
        evaluate(left);
        isAssigning = false;

        Object rawTarget = assignTarget;
        assignTarget = null;

        evaluate(right);

        FrameLine valueLine = peek();

        if (rawTarget instanceof FrameLine) {
            FrameLine targetLine = (FrameLine) rawTarget;
            CompilerRegister intermediateRegister = registerBySize(valueLine.size, 0);

            mov(valueLine.size, register(intermediateRegister), stack(valueLine.position));
            mov(targetLine.size, stack(targetLine.position), register(intermediateRegister));
        } else {
            GlobalSymbol symbol = (GlobalSymbol) rawTarget;
            CompilerRegister intermediateRegister = registerBySize(valueLine.size, 0);

            mov(valueLine.size, register(intermediateRegister), stack(valueLine.position));
            mov(symbol.getSize(), new Global(symbol.getName()), register(intermediateRegister));
        }

        return null;
    }

    @Override
    public Void visitIdentifierExpr(IdentifierExpr expr) {
        Token identifierToken = expr.identifier;
        String name = identifierToken.lexeme;

        if (expr.isGlobal) {
            GlobalSymbol symbol = getGlobal(name);

            if (isAssigning) {
                assignTarget = symbol;
                return null;
            }

            WordSize size = symbol.getSize();
            CompilerRegister intermediateRegister = registerBySize(size, 0);

            FrameLine line = push(size);

            line.type = symbol.getType();

            mov(size, register(intermediateRegister), new Global(name));
            mov(size, stack(line.position), register(intermediateRegister));
        } else {
            FrameLine variableLine = getStack(identifierToken.lexeme);

            if (isAssigning) {
                assignTarget = variableLine;
                return null;
            }

            CompilerRegister intermediateRegister = registerBySize(variableLine.size, 0);
            mov(variableLine.size, register(intermediateRegister), stack(variableLine.position));

            FrameLine valueLine = push(variableLine.size);

            valueLine.type = variableLine.type;
            valueLine.payload = variableLine.payload;

            mov(variableLine.size, stack(valueLine.position), register(intermediateRegister));
        }

        return null;
    }

    @Override
    public Void visitBinaryExpr(BinaryExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        TokenType operator = expr.operator.type;

        CompilerRegister addSubRight = null;
        CompilerRegister addSubLeft = null;

        if (operator == TokenType.MINUS || operator == TokenType.PLUS) {
            FrameLine l0 = pop();
            FrameLine l1 = pop();

            addSubRight = registerBySize(l0.size, 1); //rbx
            addSubLeft = registerBySize(l1.size, 0); // rax

            mov(l0.size, register(addSubRight), stack(l0.position));
            mov(l1.size, register(addSubLeft), stack(l1.position));
        }

        switch (operator) {
            case PLUS:
                add(addSubLeft, addSubRight);
                break;

            case MINUS:
                sub(addSubLeft, addSubRight);
                break;

            case SLASH:
            case PERCENT: {
                FrameLine l0 = pop();
                FrameLine l1 = pop();

                CompilerRegister rdx = registerBySize(WordSize.QUAD_WORD, 3);
                CompilerRegister rbx = registerBySize(l0.size, 1);
                CompilerRegister rax = registerBySize(l1.size, 0);

                // Cleaning the register where the result will be
                mov(WordSize.QUAD_WORD, register(rdx), literal(0L));

                mov(l0.size, register(rbx), stack(l0.position)); //rbx
                mov(l1.size, register(rax), stack(l1.position)); //rax

                div(rbx);

                break;
            }
            case ASTERISK: {
                FrameLine l0 = pop();
                FrameLine l1 = pop();

                CompilerRegister rdx = registerBySize(l0.size, 3);
                CompilerRegister rax = registerBySize(l1.size, 0);

                mov(l0.size, register(rdx), stack(l0.position));
                mov(l1.size, register(rax), stack(l1.position));

                mul(rdx);

                break;
            }
        }

        // Size of result operation
        WordSize size = Utils.bitTypeToSize(expr.type);

        if (operator == TokenType.MINUS || operator == TokenType.PLUS) {
            FrameLine l0 = push(size);
            CompilerRegister resultReg = addSubResultReg(size);

            mov(size, stack(l0.position), register(resultReg));
        }

        if (operator == TokenType.SLASH || operator == TokenType.ASTERISK) {
            FrameLine l0 = push(size);
            CompilerRegister resultReg = divMulResultReg(size);

            mov(size, stack(l0.position), register(resultReg));
        }

        if (operator == TokenType.PERCENT) {
            FrameLine l0 = push(size);
            mov(size, stack(l0.position), register(CompilerRegister.edx));
        }

        return null;
    }

    @Override
    public Void visitCallExpr(CallExpr expr) {
        List<Expression> arguments = expr.arguments;
        List<BitType> argumentsTypes = expr.argumentsTypes;
        BitType returnType = expr.returnType;


        if (arguments != null) {
            for (Expression argument : arguments)
                evaluate(argument);

            for (int i = argumentsTypes.size() - 1; i >= 0; i--) {
                BitType argumentsType = argumentsTypes.get(i);
                WordSize size = Utils.bitTypeToSize(argumentsType);

                CompilerRegister register = registerBySize(size, paramsIndexes[i]);
                mov(size, register(register), stack(pop().position));
            }
        }

        IdentifierExpr identifierExpr = (IdentifierExpr) expr.left;
        String name = identifierExpr.identifier.lexeme;

        call(name);

        if (returnType != null && returnType != BitType.VOID) {
            WordSize size = Utils.bitTypeToSize(returnType);
            CompilerRegister register = registerBySize(size, 0);

            FrameLine line = push(size);
            line.type = returnType;

            mov(size, stack(line.position), register(register));
        }

        return null;
    }

    @Override
    public Void visitComparisonExpr(Comparison expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        TokenType operator = expr.operator.type;

        FrameLine l0 = pop();
        FrameLine l1 = pop();

        CompilerRegister rbx = registerBySize(l0.size, 1);
        CompilerRegister rax = registerBySize(l1.size, 0);

        mov(l0.size, register(rbx), stack(l0.position));
        mov(l1.size, register(rax), stack(l1.position));

        cmp(rax, rbx);

        switch (operator) {
            case LESS_THAN:
                setl(stack(push(WordSize.BYTE).position));
                break;

            case GREATER_THAN:
                setg(stack(push(WordSize.BYTE).position));
                break;

            case LESS_EQUALS:
                setle(stack(push(WordSize.BYTE).position));
                break;

            case GREATER_EQUALS:
                setge(stack(push(WordSize.BYTE).position));
                break;

            case EQUALS_EQUALS:
                sete(stack(push(WordSize.BYTE).position));
                break;
        }

        return null;
    }

    @Override
    public Void visitLogicalExpr(LogicalExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        TokenType operator = expr.operator.type;

        FrameLine l0 = pop();
        FrameLine l1 = pop();

        CompilerRegister rbx = registerBySize(l0.size, 1);
        CompilerRegister rax = registerBySize(l1.size, 0);

        mov(l0.size, register(rbx), stack(l0.position));
        mov(l1.size, register(rax), stack(l1.position));

        switch (operator) {
            case OR:
                or(register(rax), register(rbx));
                break;

            case AND:
                and(register(rax), register(rbx));
                break;
        }

        mov(WordSize.BYTE, stack(push(WordSize.BYTE).position), register(rax));

        return null;

    }

    @Override
    public Void visitEqualityExpr(EqualityExpr expr) {
        evaluate(expr.left);
        evaluate(expr.right);

        TokenType operator = expr.operator.type;

        FrameLine l0 = pop();
        FrameLine l1 = pop();

        CompilerRegister rbx = registerBySize(l0.size, 1);
        CompilerRegister rax = registerBySize(l1.size, 0);

        mov(l0.size, register(rbx), stack(l0.position));
        mov(l1.size, register(rax), stack(l1.position));

        cmp(rax, rbx);

        switch (operator) {
            case BANG_EQUALS:
                setne(stack(push(WordSize.BYTE).position));
                break;

            case EQUALS_EQUALS:
                sete(stack(push(WordSize.BYTE).position));
                break;
        }

        return null;
    }

    @Override
    public Void visitUnaryExpr(UnaryExpr expr) {
        evaluate(expr.right);

        BitType type = expr.type;
        WordSize size = Utils.bitTypeToSize(type);
        TokenType operator = expr.operator.type;

        switch (operator) {
            case MINUS: {
                FrameLine valueLine = pop();
                CompilerRegister rax = registerBySize(valueLine.size, 0);

                CompilerRegister rdx = registerBySize(size, 3);

                mov(size, register(rdx), literal(-1L));
                mov(valueLine.size, register(rax), stack(valueLine.position));

                mul(rdx);

                FrameLine resultLine = push(size);
                CompilerRegister resultReg = divMulResultReg(size);

                mov(size, stack(resultLine.position), register(resultReg));

                break;
            }

            case BANG: {
                if (!expr.isLast)
                    return null;

                FrameLine valueLine = pop();
                CompilerRegister rax = registerBySize(valueLine.size, 0);

                CompilerRegister rbx = registerBySize(size, 1);

                mov(valueLine.size, register(rax), stack(valueLine.position));
                mov(size, register(rbx), literal(0L));

                cmp(rax, rbx);

                if (expr.counter % 2 == 0)
                    sete(stack(push(size).position));
                else
                    setne(stack(push(size).position));

                break;
            }
        }

        return null;
    }

    @Override
    public Void visitExpressionStmt(ExprStmt statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitVarDeclarationStmt(VarDeclarationStmt statement) {
        BitType type = statement.type;
        Token identifierToken = statement.identifierToken;
        Expression initializer = statement.initializer;

        WordSize size = Utils.bitTypeToSize(type);
        String name = identifierToken.lexeme;

        if (initializer != null)
            evaluate(initializer);

        if (statement.isGlobal) {
            data(name, size, 0L);

            if (initializer != null) {
                FrameLine line = pop();
                CompilerRegister register = registerBySize(size, 0);

                mov(size, register(register), stack(line.position));
                mov(size, new Global(name), register(register));
            }

            declareGlobal(name, type, size);
            comment(String.format("global %s declared", name), false);
        } else {
            FrameLine valueLine = null;

            if (initializer != null)
                valueLine = pop();

            FrameLine variableLine = declareStack(size, identifierToken.lexeme);

            if (valueLine != null) {
                variableLine.type = valueLine.type;
                variableLine.payload = valueLine.payload;
            }
        }

        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt statement) {
        evaluate(statement.value);

        FrameLine line = pop();

        if (statement.type == BitType.BOOL)
            mov(WordSize.QUAD_WORD, register(CompilerRegister.rsi), literal(0L));

        if (line.type == BitType.STR)
            printStr(stack(line.position));
        else
            print(line.size, stack(line.position));

        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt statement) {
        executeSimpleBlock(statement.statements);
        return null;
    }

    @Override
    public Void visitFnDeclarationStmt(FnDeclarationStmt statement) {
        Token identifierToken = statement.identifierToken;
        List<Statement> statements = statement.body;

        Frame frame = new Frame(frames.peek().getAlignment());

        frames.push(frame);

        label(identifierToken.lexeme);
        prologue();
        int alignmentIndex = alignment();

        if (statement.paramsTypes != null) {
            for (int i = 0; i < statement.paramsTypes.size(); i++) {
                BitType type = statement.paramsTypes.get(i);
                FnDeclarationStmt.FnParam param = statement.params.get(i);

                WordSize size = Utils.bitTypeToSize(type);
                FrameLine line = declareStack(size, param.identifier.lexeme);

                line.type = type;

                CompilerRegister register = registerBySize(line.size, paramsIndexes[i]);
                mov(line.size, stack(line.position), register(register));

                comment(String.format("retrieving argument '%s' at %d", param.identifier.lexeme, line.position), false);
            }
        }

        for (Statement s : statements)
            execute(s);

        epilogue();

        ret();

        frames.pop();

        List<Instruction> instructions = frame.getInstructions();

        instructions.set(alignmentIndex, new Sub(new Register(CompilerRegister.rsp), literal(frame.getAlignment())));

        functions.add(instructions);

        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt statement) {
        Expression returnExpr = statement.expression;
        BitType returnType = statement.returnType;

        if (returnExpr != null) {
            evaluate(returnExpr);

            WordSize size = Utils.bitTypeToSize(returnType);
            CompilerRegister register = registerBySize(size, 0);

            mov(size, register(register), stack(pop().position));
        }

        epilogue();
        ret();

        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt statement) {
        Expression ifExpression = statement.ifConditionExpr;
        List<Statement> ifStatements = statement.ifStatements;
        List<Statement> elseStatements = statement.elseStatements;

        String ifLabel = generateIfLabel();
        String exitLabel = generateExitLabel();

        evaluate(ifExpression);

        FrameLine line = pop();

        CompilerRegister rbx = registerBySize(line.size, 1);
        CompilerRegister rax = registerBySize(line.size, 0);

        mov(line.size, register(rbx), stack(line.position));
        mov(line.size, register(rax), literal(1L));

        cmp(rax, rbx);

        je(labelAddress(ifLabel));

        if (elseStatements != null)
            executeSimpleBlock(elseStatements);

        jmp(labelAddress(exitLabel));

        label(ifLabel);
        executeSimpleBlock(ifStatements);

        label(exitLabel);

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt statement) {
        final String previousBreakLabel = whileBreakLabel;
        final String previousConditionalLabel = whileConditionalLabel;

        String conditionalLabel = generateWhileLabel();
        String whileLabel = generateWhileLabel();
        whileBreakLabel = statement.containsBreak ? generateExitLabel() : null;
        whileConditionalLabel = conditionalLabel;

        jmp(labelAddress(conditionalLabel));
        // body
        label(whileLabel);
        executeSimpleBlock(statement.statements);
        // conditional
        label(conditionalLabel);

        evaluate(statement.condition);

        FrameLine line = pop();

        CompilerRegister rbx = registerBySize(line.size, 1);
        CompilerRegister rax = registerBySize(line.size, 0);

        mov(line.size, register(rbx), stack(line.position));
        mov(line.size, register(rax), literal(1L));

        cmp(rax, rbx);

        je(labelAddress(whileLabel));

        if (statement.containsBreak) label(whileBreakLabel);

        whileBreakLabel = previousBreakLabel;
        whileConditionalLabel = previousConditionalLabel;

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
}
