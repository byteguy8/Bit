package bit.compiler;

import bit.Utils;
import bit.instruction.Instruction;
import bit.WordSize;

import java.util.*;

public class Frame {
    private static final class FrameEnvironment {
        private final FrameEnvironment enclosing;
        private final Map<String, FrameLine> symbols;

        private FrameEnvironment(FrameEnvironment enclosing, Map<String, FrameLine> symbols) {
            this.enclosing = enclosing;
            this.symbols = symbols;
        }

        private FrameEnvironment(FrameEnvironment enclosing) {
            this(enclosing, new HashMap<>());
        }

        private FrameEnvironment() {
            this(null);
        }

        public FrameLine get(String name) {
            if (symbols.containsKey(name))
                return symbols.get(name);
            else if (enclosing != null)
                return enclosing.get(name);
            else
                throw new IllegalArgumentException(String.format("FrameLine named '%s' doesn't exists", name));
        }

        public void declare(String name, FrameLine line) {
            if (symbols.containsKey(name))
                throw new IllegalArgumentException(String.format("Already exists a FrameLine named '%s'", name));

            symbols.put(name, line);
        }
    }

    private int length;
    private int maxLength = 0;
    private final Stack<FrameLine> lines = new Stack<>();
    private FrameEnvironment environment = new FrameEnvironment();
    private final List<Instruction> instructions = new ArrayList<>();

    public Frame(int length){
        this.length = length;
    }

    public Frame(){
        this(0);
    }

    public FrameLine push(WordSize size) {
        int bytes = Utils.wordSizeToBytes(size);
        int position = length + bytes;
        FrameLine line = new FrameLine(position, size);

        lines.push(line);

        length += bytes;

        if (length > maxLength)
            maxLength = length;

        return line;
    }

    public FrameLine pop() {
        FrameLine line = lines.pop();
        int bytes = Utils.wordSizeToBytes(line.size);

        length -= bytes;

        return line;
    }

    public FrameLine peek() {
        return lines.peek();
    }

    public FrameLine get(String name) {
        return environment.get(name);
    }

    public FrameLine declare(WordSize size, String name) {
        int bytes = Utils.wordSizeToBytes(size);
        int position = length + bytes;
        FrameLine line = new FrameLine(position, size);

        lines.push(line);
        environment.declare(name, line);

        length += bytes;

        if (length > maxLength)
            maxLength = length;

        return line;
    }

    public int getLength() {
        return length;
    }

    public int getAlignment() {
        int mod = maxLength % 16;
        int padding = 16 - mod;

        return maxLength + padding;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void nextScope() {
        environment = new FrameEnvironment(environment);
    }

    public void previousScope() {
        if (environment.enclosing == null)
            throw new IllegalStateException("Can go previous");

        environment = environment.enclosing;
    }
}
