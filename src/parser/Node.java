package parser;

import interpreter.*;
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

abstract class InternalNode extends Node {

    InternalNode(final Position position) {
        super(position);
    }

    public abstract void lookUp(EnvOptimizer envOptimizer);
}

class NameNode extends LeafNode {

    String name;

    Variable variable;

    NameNode(final Position position, String name) {
        super(position);

        this.name = name;
        nodeType = NAME_NODE;
    }

    void setVariable(int varLevel, EnvOptimizer envOptimizer) {
        switch (varLevel) {
            case Parser.ASSIGN:
                variable = envOptimizer.get(name);
                break;
            case Parser.VAR:
                variable = envOptimizer.addVar(name);
                break;
            case Parser.CONST:
                break;
            case Parser.GET:
                variable = envOptimizer.get(name);
                break;
            default:
                throw new SplException("sss");
        }
    }

    @Override
    public Object evaluate(Environment env) {
//        return env.get(name, position);
        return env.get(variable, position);
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

abstract class BinaryExpr extends InternalNode {

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
    public void lookUp(EnvOptimizer envOptimizer) {
        ((NameNode) left).setVariable(varLevel, envOptimizer);
        if (right instanceof InternalNode) {
            ((InternalNode) right).lookUp(envOptimizer);
        } else if (right instanceof NameNode) {
            ((NameNode) right).setVariable(Parser.GET, envOptimizer);
        }
    }

    @Override
    public Object evaluate(Environment env) {
        Variable leftName = ((NameNode) left).variable;
        Object rightObj = right.evaluate(env);
        switch (varLevel) {
            case Parser.ASSIGN:
                env.assign(leftName, rightObj, position);
                break;
            case Parser.CONST:
//                env.defineConst(leftName, rightObj, position);
                break;
            case Parser.VAR:
                env.defineVar(leftName, rightObj, position);
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
    public void lookUp(EnvOptimizer envOptimizer) {
        if (left instanceof NameNode) {
            ((NameNode) left).setVariable(Parser.GET, envOptimizer);
        }
        if (right instanceof NameNode) {
            ((NameNode) right).setVariable(Parser.GET, envOptimizer);
        }
        if (right instanceof InternalNode) {
            ((InternalNode) right).lookUp(envOptimizer);
        }
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

class UnaryExpr extends InternalNode {

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
    public void lookUp(EnvOptimizer envOptimizer) {
        if (value instanceof InternalNode) {
            ((InternalNode) value).lookUp(envOptimizer);
        }
    }

    @Override
    public Object evaluate(Environment env) {
        return null;
    }
}

abstract class ConditionStmt extends InternalNode {

    BlockStmt condition;
    Node doBlock;

    ConditionStmt(final Position position) {
        super(position);
    }
}

class IfStmt extends ConditionStmt {

    Node elseBlock;

    private VariableCount[] variableCounts;  // {countOfDoBlock, countOfElseBlock}

    IfStmt(final Position position) {
        super(position);

        nodeType = IF_STMT;
    }

    @Override
    public Object evaluate(Environment env) {
        Boolean result = (Boolean) condition.evaluate(env);

        if (result) {
            Environment inner = new Environment(Environment.SUB_SCOPE, env, variableCounts[0]);
            return doBlock.evaluate(inner);
        } else if (elseBlock != null) {
            Environment inner = new Environment(Environment.SUB_SCOPE, env, variableCounts[1]);
            return elseBlock.evaluate(inner);
        } else return null;
    }

    @Override
    public void lookUp(EnvOptimizer envOptimizer) {
        condition.lookUp(envOptimizer);
        variableCounts = new VariableCount[2];

        if (doBlock instanceof InternalNode) {
            EnvOptimizer inner = new EnvOptimizer(Environment.SUB_SCOPE, envOptimizer);
            ((InternalNode) doBlock).lookUp(inner);
            variableCounts[0] = new VariableCount(inner.getVariableCounter(), inner.getConstCounter());
        }
        if (elseBlock instanceof InternalNode) {
            EnvOptimizer inner = new EnvOptimizer(Environment.SUB_SCOPE, envOptimizer);
            ((InternalNode) elseBlock).lookUp(inner);
            variableCounts[1] = new VariableCount(inner.getVariableCounter(), inner.getConstCounter());
        }
    }
}

class WhileStmt extends ConditionStmt {

    private VariableCount[] variableCounts;  // {countTitleBlock, countDoBlock}

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
    public void lookUp(EnvOptimizer envOptimizer) {
        EnvOptimizer titleOptimizer = new EnvOptimizer(Environment.LOOP_SCOPE, envOptimizer);
        EnvOptimizer innerOptimizer = new EnvOptimizer(Environment.SUB_SCOPE, titleOptimizer);

        condition.lookUp(titleOptimizer);
        if (doBlock instanceof InternalNode) {
            ((InternalNode) doBlock).lookUp(innerOptimizer);
        }
        variableCounts = new VariableCount[2];
        variableCounts[0] = titleOptimizer.getVariableCount();
        variableCounts[1] = innerOptimizer.getVariableCount();
    }

    @Override
    public Object evaluate(Environment env) {
        Environment titleScope = new Environment(Environment.LOOP_SCOPE, env, variableCounts[0]);
        Environment innerScope = new Environment(Environment.SUB_SCOPE, titleScope, variableCounts[1]);
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

    VariableCount[] variableCounts;

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
    public void lookUp(EnvOptimizer envOptimizer) {
        int partCount = condition.lineCount();
        if (partCount == 2) {
//            return evalForEachLoop(env);
        } else if (partCount == 3) {
            forLoopLookUp(envOptimizer);
        } else {
            throw new SplException("Unknown syntax of for-loop");
        }
    }

    private void forLoopLookUp(EnvOptimizer envOptimizer) {
        EnvOptimizer titleOptimizer = new EnvOptimizer(Environment.LOOP_SCOPE, envOptimizer);
        EnvOptimizer innerOptimizer = new EnvOptimizer(Environment.SUB_SCOPE, titleOptimizer);

        Node start = condition.getLine(0);
        Node end = condition.getLine(1);
        Node step = condition.getLine(2);
        if (start instanceof InternalNode) {
            ((InternalNode) start).lookUp(titleOptimizer);
        }
        if (end instanceof InternalNode) {
            ((InternalNode) end).lookUp(titleOptimizer);
        }
        if (step instanceof InternalNode) {
            ((InternalNode) step).lookUp(titleOptimizer);
        }
        if (doBlock instanceof InternalNode) {
            ((InternalNode) doBlock).lookUp(innerOptimizer);
        }
        variableCounts = new VariableCount[2];
        variableCounts[0] = titleOptimizer.getVariableCount();
        variableCounts[1] = innerOptimizer.getVariableCount();
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
        Environment titleScope = new Environment(Environment.LOOP_SCOPE, env, variableCounts[0]);
        Environment innerScope = new Environment(Environment.SUB_SCOPE, titleScope, variableCounts[1]);
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

class DefStmt extends InternalNode {

    NameNode name;

    BlockStmt params;

    BlockStmt body;

    VariableCount variableCount;

    DefStmt(final Position position, final NameNode functionName) {
        super(position);

        name = functionName;
        nodeType = DEF_STMT;
    }

    @Override
    public String toString() {
        return "function " + name + "(" + params.toString() + ") {\n" + body.toString() + "\n}";
    }

    @Override
    public void lookUp(EnvOptimizer envOptimizer) {
        name.variable = envOptimizer.addVar(name.name);
        EnvOptimizer functionOptimizer = new EnvOptimizer(Environment.FUNCTION_SCOPE, envOptimizer);
        for (Node node : params.getLines()) {
            if (node instanceof NameNode) {
                functionOptimizer.addVar(((NameNode) node).name);
                ((NameNode) node).setVariable(Parser.VAR, functionOptimizer);
            } else if (node instanceof AssignmentNode) {
                // TODO
            }
        }
        body.lookUp(functionOptimizer);
        variableCount = functionOptimizer.getVariableCount();
    }

    @Override
    public Object evaluate(Environment env) {
        Function function = new Function(position, env, variableCount);

        int paramsLength = params.lineCount();
        ParameterPair[] pairs = new ParameterPair[paramsLength];

        for (int i = 0; i < paramsLength; i++) {
            Node node = params.getLine(i);
            if (node.nodeType == NAME_NODE) {
                pairs[i] = new ParameterPair(((NameNode) node).variable, null);
            } else if (node.nodeType == ASSIGNMENT_NODE) {
                AssignmentNode assignmentNode = (AssignmentNode) node;
                Object value = assignmentNode.right.evaluate(env);
                pairs[i] = new ParameterPair(((NameNode) assignmentNode.left).variable, value);
            } else {
                throw new SplException(String.format(
                        "Unexpected syntax in function declaration, in file '%s', at line %d",
                        position.getFileName(), position.getLineNumber()
                ));
            }
        }

        function.params = pairs;
        function.body = body;

        env.defineVar(name.variable, function, position);
        return function;
    }
}

class FunctionCall extends InternalNode {

    NameNode name;

    BlockStmt arguments;

    FunctionCall(final Position position, final NameNode name) {
        super(position);

        this.name = name;
        nodeType = FUNCTION_CALL;
    }

    @Override
    public void lookUp(EnvOptimizer envOptimizer) {
        name.variable = envOptimizer.get(name.name);
    }

    @Override
    public Object evaluate(Environment env) {
//        return null;
        Function function = (Function) env.get(name.variable, position);
        Environment callScope = new Environment(Environment.FUNCTION_SCOPE, function.outerEnv, function.variableCount);
//        Object[] args = new Object[function.params.length];
        for (int i = 0; i < function.params.length; i++) {
            Variable argName = function.params[i].variable;
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
            callScope.defineVar(argName, arg, position);
        }
        return function.body.evaluate(callScope);
    }
}
