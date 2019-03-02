package interpreter;

import parser.Parser;

public class HeapVariable extends Variable {

    public String name;

    public HeapVariable(final String name) {
        super(-1, -1, Parser.ASSIGN);
        this.name = name;
    }
}
