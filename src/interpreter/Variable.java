package interpreter;

public class Variable {

    public int index;
    public int varType;
    public int scopeDistance;


    public Variable(final int index, final int scopeDistance, final int type) {
        this.index = index;
        this.varType = type;
        this.scopeDistance = scopeDistance;
    }

    @Override
    public String toString() {
        return "Variable<" + index + ">";
    }
}
