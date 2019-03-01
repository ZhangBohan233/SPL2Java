package parser;

import interpreter.Environment;
import interpreter.Function;
import interpreter.ParameterPair;
import interpreter.SplException;
import tokenizer.Position;
import tokenizer.TokenLib;
import util.Utility;

import java.util.ArrayList;

public abstract class Node {

    static int indent = 0;

    public final static int MULTIPLIER = 1000;

    final static int INT_NODE = 1;
    final static int FLOAT_NODE = 2;
    final static int LITERAL_NODE = 3;
    final static int NAME_NODE = 4;
    final static int BOOLEAN_STMT = 5;
    final static int NULL_STMT = 6;
    final static int BREAK_STMT = 7;
    final static int CONTINUE_STMT = 8;
    final static int ASSIGNMENT_NODE = 9;
    final static int DOT = 10;
    final static int ANONYMOUS_CALL = 11;
    final static int BINARY_OPERATOR = 12;
    final static int NEGATIVE_EXPR = 13;
    final static int NOT_EXPR = 14;
    final static int RETURN_STMT = 15;
    final static int BLOCK_STMT = 16;
    final static int IF_STMT = 17;
    final static int WHILE_STMT = 18;
    final static int FOR_LOOP_STMT = 19;
    final static int DEF_STMT = 20;
    final static int FUNCTION_CALL = 21;
    final static int CLASS_STMT = 22;
    final static int CLASS_INIT = 23;
    final static int INVALID_TOKEN = 24;
    final static int ABSTRACT = 25;
    final static int THROW_STMT = 26;
    final static int TRY_STMT = 27;
    final static int CATCH_STMT = 28;
    final static int TYPE_NODE = 29;
    final static int JUMP_NODE = 30;
    final static int UNDEFINED_NODE = 31;

    Position position;

    int nodeType;

    public Node(final Position position) {
        this.position = position;
    }

    public abstract Object evaluate(Environment env);
}

abstract class LeafNode extends Node {

    LeafNode(final Position position) {
        super(position);
    }
}

class NameNode extends LeafNode {

    String name;

    NameNode(final Position position, String name) {
        super(position);

        this.name = name;
        nodeType = NAME_NODE;
    }

    @Override
    public Object evaluate(Environment env) {
        return env.get(name, position);
    }

    @Override
    public String toString() {
        return String.format("Name(%s)", name);
    }
}

class IntNode extends LeafNode {

    long value;

    IntNode(final Position position, String numText) {
        super(position);

        value = Long.valueOf(numText);
        nodeType = INT_NODE;
    }

    @Override
    public String toString() {
        return String.format("Int(%d)", value);
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}

class FloatNode extends LeafNode {

    double value;

    FloatNode(final Position position, String numText) {
        super(position);

        value = Double.valueOf(numText);
        nodeType = FLOAT_NODE;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }

    @Override
    public String toString() {
        return String.format("Float(%f)", value);
    }
}

class BooleanStmt extends LeafNode {

    private boolean value;

    BooleanStmt(final Position position, final boolean stringValue) {
        super(position);

        value = stringValue;
        nodeType = BOOLEAN_STMT;
    }

    @Override
    public Object evaluate(Environment env) {
        return value;
    }
}

class NullStmt extends LeafNode {

    NullStmt(final Position position) {
        super(position);

        nodeType = NULL_STMT;
    }

    @Override
    public Object evaluate(Environment env) {
        return Environment.NULL_POINTER;
    }
}

class AbstractStmt extends LeafNode {

    AbstractStmt(final Position position) {
        super(position);

        nodeType = ABSTRACT;
    }

    @Override
    public Object evaluate(Environment env) {
        throw new SplException("Method not implemented.");
    }
}

class LiteralNode extends LeafNode {

    private String literal;

    LiteralNode(final Position position, String literal) {
        super(position);

        this.literal = literal;
        nodeType = LITERAL_NODE;
    }

    @Override
    public Object evaluate(Environment env) {
        return literal;
    }

    @Override
    public String toString() {
        return String.format("Literal(%s)", literal);
    }
}

abstract class BinaryExpr extends Node {

    protected Node left;
    protected Node right;
    protected String symbol;

    BinaryExpr(final Position position, final String symbol) {
        super(position);

        this.symbol = symbol;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    boolean noLeft() {
        return left == null;
    }

    boolean noRight() {
        return right == null;
    }

    @Override
    public String toString() {
        String ls = left == null ? "null" : left.toString();
        String rs = right == null ? "null" : right.toString();
        return "BE(" + ls + symbol + rs + ")";
    }
}

class AssignmentNode extends BinaryExpr {

    private int varLevel;

    AssignmentNode(final Position position, final int level) {
        super(position, "=");

        varLevel = level;
        nodeType = ASSIGNMENT_NODE;
    }

    @Override
    public Object evaluate(Environment env) {
        String leftName = ((NameNode) left).name;
        Object rightObj = right.evaluate(env);
        switch (varLevel) {
            case Parser.ASSIGN:
                env.assign(leftName, rightObj, position);
                break;
            case Parser.CONST:
                env.defineConst(leftName, rightObj, position);
                break;
            case Parser.VAR:
                env.defineVar(leftName, rightObj, position);
                break;
            case Parser.LET:
                env.defineLocal(leftName, rightObj, position);
                break;
            default:
                throw new SplException("Unknown variable level");
        }
        return right;
    }
}

class BinaryOperator extends BinaryExpr {

    private int extraPrecedence;

    private boolean assignment;

    BinaryOperator(final Position position, final String operator, final int extra) {
        super(position, operator);

        this.extraPrecedence = extra * MULTIPLIER;
        assignment = false;
        nodeType = BINARY_OPERATOR;
    }

    public void setAssignment(boolean assignment) {
        this.assignment = assignment;
    }

    public int getPrecedence() {
        return TokenLib.getPrecedence(symbol) + extraPrecedence;
    }

    @Override
    public Object evaluate(Environment env) {
        Object leftObj = left.evaluate(env);
        if (symbol.equals("||") || symbol.equals("&&")) {  // performs lazy evaluation
            return null;
        } else {
            Object rightObj = right.evaluate(env);

            String lType = leftObj.getClass().getTypeName();
//            System.out.println(lType);
            switch (lType) {
                case "java.lang.Long":
                    return Arithmetic.arithmetic((Long) leftObj, rightObj, symbol);
                case "IntNode":
                    return Arithmetic.arithmetic(((IntNode) leftObj).value, rightObj, symbol);
                case "java.lang.Double":
                    return Arithmetic.arithmetic((Double) leftObj, rightObj, symbol);
                case "FloatNode":
                    return Arithmetic.arithmetic(((FloatNode) leftObj).value, rightObj, symbol);
                default:
                    throw new SplException("Unsupported type for arithmetic");
            }
        }
    }

}

class UnaryExpr extends Node {

    private Node value;
    private String symbol;
    private int extraPrecedence;

    UnaryExpr(final Position position, final String operator, final int extra) {
        super(position);

        this.symbol = operator;
        this.extraPrecedence = extra;
        switch (operator) {
            case "!":
                nodeType = NOT_EXPR;
                break;
            case "-":
                nodeType = NEGATIVE_EXPR;
                break;
            case "return":
                nodeType = RETURN_STMT;
                break;
            default:
                throw new SplException("Unknown unary operator");
        }
    }

    public void setValue(Node value) {
        this.value = value;
    }

    public int getPrecedence() {
        return TokenLib.getPrecedence(symbol) + extraPrecedence;
    }

    boolean noValue() {
        return value == null;
    }

    @Override
    public Object evaluate(Environment env) {
        return null;
    }
}

abstract class ConditionStmt extends Node {

    BlockStmt condition;
    Node doBlock;

    ConditionStmt(final Position position) {
        super(position);
    }
}

class IfStmt extends ConditionStmt {

    Node elseBlock;

    IfStmt(final Position position) {
        super(position);

        nodeType = IF_STMT;
    }

    @Override
    public Object evaluate(Environment env) {
        Boolean result = (Boolean) condition.evaluate(env);
        Environment inner = new Environment(Environment.SUB_SCOPE, env);
        if (result) {
            return doBlock.evaluate(inner);
        } else if (elseBlock != null) {
            return elseBlock.evaluate(inner);
        } else return null;
    }
}

class WhileStmt extends ConditionStmt {

    WhileStmt(final Position position) {
        super(position);

        nodeType = WHILE_STMT;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("while (");
        sb.append(condition.toString());
        sb.append(") {\n");
        if (doBlock != null) {
            sb.append(doBlock.toString());
        }
        sb.append("\n}");
        return sb.toString();
    }

    @Override
    public Object evaluate(Environment env) {
        Environment titleScope = new Environment(Environment.LOOP_SCOPE, env);
        Environment innerScope = new Environment(Environment.SUB_SCOPE, titleScope);
        Object result = null;
        while (!titleScope.broken && (Boolean) condition.evaluate(titleScope)) {
            innerScope.invalidate();
            result = doBlock.evaluate(innerScope);
            titleScope.resume();
        }
        return result;
    }
}

class ForLoopStmt extends ConditionStmt {

    ForLoopStmt(final Position position) {
        super(position);

        nodeType = FOR_LOOP_STMT;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("for (");
        sb.append(condition.toString());
        sb.append(") {\n");
        if (doBlock != null) {
            sb.append(doBlock.toString());
        }
        sb.append("\n}");
        return sb.toString();
    }

    @Override
    public Object evaluate(Environment env) {
        int partCount = condition.lineCount();
        if (partCount == 2) {
            return evalForEachLoop(env);
        } else if (partCount == 3) {
            return evalForLoop(env);
        } else {
            throw new SplException("Unknown syntax of for-loop");
        }
    }

    private Object evalForLoop(Environment env) {
        Environment titleScope = new Environment(Environment.LOOP_SCOPE, env);
        Environment innerScope = new Environment(Environment.SUB_SCOPE, titleScope);
        Node start = condition.getLine(0);
        Node end = condition.getLine(1);
        Node step = condition.getLine(2);
        Object result = start.evaluate(titleScope);
        while (!titleScope.broken && (Boolean) end.evaluate(titleScope)) {
            innerScope.invalidate();
            result = doBlock.evaluate(innerScope);
            titleScope.resume();
            step.evaluate(titleScope);
        }
        return result;
    }

    private Object evalForEachLoop(Environment env) {
        return null;
    }
}

class ReturnStmt extends UnaryExpr {

    ReturnStmt(final Position position) {
        super(position, "return", 0);  // No return statement can be in parenthesis

        nodeType = RETURN_STMT;
    }
}

class BreakStmt extends Node {

    BreakStmt(final Position position) {
        super(position);

        nodeType = BREAK_STMT;
    }

    @Override
    public Object evaluate(Environment env) {
        env.breakLoop();
        return null;
    }
}

class ContinueStmt extends Node {

    ContinueStmt(final Position position) {
        super(position);

        nodeType = CONTINUE_STMT;
    }

    @Override
    public Object evaluate(Environment env) {
        env.pause();
        return null;
    }
}

class DefStmt extends Node {

    String name;

    BlockStmt params;

    BlockStmt body;

    DefStmt(final Position position, final String functionName) {
        super(position);

        name = functionName;
        nodeType = DEF_STMT;
    }

    @Override
    public String toString() {
        return "function " + name + "(" + params.toString() + ") {\n" + body.toString() + "\n}";
    }

    @Override
    public Object evaluate(Environment env) {
        Function function = new Function(position, env);

        int paramsLength = params.lineCount();
        ParameterPair[] pairs = new ParameterPair[paramsLength];

        for (int i = 0; i < paramsLength; i++) {
            Node node = params.getLine(i);
            if (node.nodeType == NAME_NODE) {
                pairs[i] = new ParameterPair(((NameNode) node).name, null);
            } else if (node.nodeType == ASSIGNMENT_NODE) {
                AssignmentNode assignmentNode = (AssignmentNode) node;
                Object value = assignmentNode.right.evaluate(env);
                pairs[i] = new ParameterPair(((NameNode) assignmentNode.left).name, value);
            } else {
                throw new SplException(String.format(
                        "Unexpected syntax in function declaration, in file '%s', at line %d",
                        position.getFileName(), position.getLineNumber()
                ));
            }
        }

        function.params = pairs;
        function.body = body;

        env.defineFunction(name, function, position);
        return function;
    }
}

class FunctionCall extends Node {

    String name;

    BlockStmt arguments;

    FunctionCall(final Position position, final String name) {
        super(position);

        this.name = name;
        nodeType = FUNCTION_CALL;
    }

    @Override
    public Object evaluate(Environment env) {
        Function function = (Function) env.get(name, position);
        Environment callScope = new Environment(Environment.FUNCTION_SCOPE, function.outerEnv);
//        Object[] args = new Object[function.params.length];
        for (int i = 0; i < function.params.length; i++) {
            String argName = function.params[i].name;
            Object arg;
            if (i < arguments.lineCount()) {
                arg = arguments.getLine(i).evaluate(env);
            } else {
                Object preset = function.params[i].defaultValue;
                if (preset == null) {
                    throw new SplException(String.format("Missing argument(s) in function '%s'", name));
                } else {
                    arg = preset;
                }
            }
            callScope.defineLocal(argName, arg, position);
        }
        return function.body.evaluate(callScope);
    }
}
