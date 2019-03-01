package parser;

import interpreter.Environment;
import tokenizer.Position;
import util.Utility;

import java.util.ArrayList;

public class BlockStmt extends Node {

    private ArrayList<Node> lines = new ArrayList<>();

    BlockStmt(final Position position) {
        super(position);

        nodeType = BLOCK_STMT;
    }

    void addLine(Node node) {
        lines.add(node);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String outerIndent = Utility.charMultiply(' ', indent);
        sb.append(outerIndent);
        sb.append("BlockStmt{\n");
        indent += 2;
        String innerIndent = Utility.charMultiply(' ', indent);
        for (Node n : lines) {
            sb.append(innerIndent).append(n.toString()).append("\n");
        }
        indent -= 2;
        sb.append(outerIndent);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Object evaluate(Environment env) {
        Object result = null;
        for (Node node : lines) {
            result = node.evaluate(env);
        }
        return result;
    }

    int lineCount() {
        return lines.size();
    }

    Node getLine(int index) {
        return lines.get(index);
    }
}
