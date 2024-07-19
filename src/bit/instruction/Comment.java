package bit.instruction;

public class Comment extends Instruction {
    private final String comment;
    private final boolean header;

    public Comment(String comment, boolean header) {
        this.comment = comment;
        this.header = header;
    }

    @Override
    public String translate() {
        if (header) return String.format("; **********|%s|*********", comment);
        else return String.format("; %s", comment);
    }
}
