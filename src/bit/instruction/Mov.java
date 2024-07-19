package bit.instruction;

import bit.Utils;
import bit.memory.address.Address;
import bit.WordSize;

public final class Mov extends Instruction {
    public WordSize size;
    public Address left;
    public Address right;

    public Mov(Address left, Address right) {
        this.size = null;
        this.left = left;
        this.right = right;
    }

    public Mov(WordSize size, Address left, Address right) {
        this.size = size;
        this.left = left;
        this.right = right;
    }

    @Override
    public String translate() {
        String sizeStr = size == null ? " " : String.format(" %s ", Utils.wordSizeToGeneral(size));
        return String.format("mov%s%s, %s", sizeStr, left.translate(), right.translate());
    }
}
