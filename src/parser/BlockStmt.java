package parser;

import interpreter.EnvOptimizer;
import interpreter.Environment;
import interpreter.VariableCount;
import tokenizer.Position;
import util.Utility;

import java.util.ArrayList;

public class BlockStmt extends InternalNode {

    private ArrayList<Node> lines = new ArrayList<>();

    private VariableCount variableCount;

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
    public void lookUp(EnvOptimizer envOptimizer) {
        for (Node node : lines) {
            if (node instanceof InternalNode) {
                ((InternalNode) node).lookUp(envOptimizer);
            } else if (node instanceof NameNode) {
                ((NameNode) node).setVariable(Parser.GET, envOptimizer);
            }
        }
        variableCount = envOptimizer.getVariableCount();
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

    public ArrayList<Node> getLines() {
        return lines;
    }

    public VariableCount getVariableCount() {
        return variableCount;
    }
}
