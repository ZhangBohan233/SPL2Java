package interpreter;

import parser.BlockStmt;
import tokenizer.Position;

public class Function {

    private Position position;

    public ParameterPair[] params;

    public BlockStmt body;

    public Environment outerEnv;

    public Function(final Position position, final Environment outer) {
        this.position = position;
        this.outerEnv = outer;
    }
}
