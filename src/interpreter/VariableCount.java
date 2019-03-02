package interpreter;

public class VariableCount {

    private int varCount, constCount;

    public VariableCount(final int varCount, final int constCount) {
        this.varCount = varCount;
        this.constCount = constCount;
    }

    public int getConstCount() {
        return constCount;
    }

    public int getVarCount() {
        return varCount;
    }
}
