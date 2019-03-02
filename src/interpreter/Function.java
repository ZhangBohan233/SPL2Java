package interpreter;

import parser.BlockStmt;
import tokenizer.Position;

public class Function {

    private Position position;

    public ParameterPair[] params;

    public BlockStmt body;

    public Environment outerEnv;

    public VariableCount variableCount;

    public Function(final Position position, final Environment outer, final VariableCount variableCount) {
        this.position = position;
        this.outerEnv = outer;
        this.variableCount = variableCount;
    }
}
