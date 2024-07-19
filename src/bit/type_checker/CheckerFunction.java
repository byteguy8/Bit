package bit.type_checker;

import bit.BitType;

import java.util.List;

public class CheckerFunction {
    private final BitType returnType;
    private List<BitType> params;

    public CheckerFunction(BitType returnType) {
        this.returnType = returnType;
    }

    public BitType getReturnType() {
        return returnType;
    }

    public List<BitType> getParams() {
        return params;
    }

    public void setParams(List<BitType> params) {
        this.params = params;
    }
}
