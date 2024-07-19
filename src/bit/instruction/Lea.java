package bit.instruction;

import bit.Utils;
import bit.memory.address.Address;
import bit.WordSize;

public class Lea extends Instruction {
    public WordSize size;
    public Address left;
    public Address right;

    public Lea(WordSize size, Address left, Address right) {
        this.size = size;
        this.left = left;
        this.right = right;
    }

    @Override
    public String translate() {
        String sizeStr = Utils.wordSizeToGeneral(size);
        return String.format("lea %s %s, %s", sizeStr, left.translate(), right.translate());
    }
}
