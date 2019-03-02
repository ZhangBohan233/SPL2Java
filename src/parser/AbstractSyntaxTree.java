package parser;

import interpreter.Function;
import tokenizer.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class AbstractSyntaxTree {

    private BlockStmt block = new BlockStmt(new Position(0, "parser"));

    private Stack<Node> stack = new Stack<>();

    private AbstractSyntaxTree inner;

    private boolean inExpr = false;

    public AbstractSyntaxTree() {

    }

    void addName(final Position pos, final String name) {
        if (inner == null) {
            NameNode nameNode = new NameNode(pos, name);
            stack.push(nameNode);
        } else {
            inner.addName(pos, name);
        }
    }

    void addInteger(final Position pos, final String numString) {
        if (inner == null) {
            stack.push(new IntNode(pos, numString));
        } else {
            inner.addInteger(pos, numString);
        }
    }

    void addFloat(final Position pos, final String numString) {
        if (inner == null) {
            stack.push(new FloatNode(pos, numString));
        } else {
            inner.addFloat(pos, numString);
        }
    }

    void addLiteral(final Position pos, final String literal) {
        if (inner == null) {
            stack.push(new LiteralNode(pos, literal));
        } else {
            inner.addLiteral(pos, literal);
        }
    }

    void addBinaryOperator(final Position pos, final String op, final int extra) {
        if (inner == null) {
            inExpr = true;
            BinaryOperator bo = new BinaryOperator(pos, op, extra);
            stack.push(bo);
        } else {
            inner.addBinaryOperator(pos, op, extra);
        }
    }

    void addBinaryOperatorWithAssignment(final Position pos, final String op, final int extra) {
        if (inner == null) {
            inExpr = true;
            BinaryOperator bo = new BinaryOperator(pos, op, extra);
            bo.setAssignment(true);
            stack.push(bo);
        } else {
            inner.addBinaryOperatorWithAssignment(pos, op, extra);
        }
    }

    void addUnaryOperator(final Position pos, final String op, final int extra) {
        if (inner == null) {
            inExpr = true;
            UnaryExpr ue = new UnaryExpr(pos, op, extra);
            stack.push(ue);
        } else {
            inner.addUnaryOperator(pos, op, extra);
        }
    }

    void addBoolean(final Position pos, final boolean value) {
        if (inner == null) {
            stack.push(new BooleanStmt(pos, value));
        } else {
            inner.addBoolean(pos, value);
        }
    }

    void addBreak(final Position pos) {
        if (inner == null) {
            stack.push(new BreakStmt(pos));
        } else {
            inner.addBreak(pos);
        }
    }

    void addContinue(final Position pos) {
        if (inner == null) {
            stack.push(new ContinueStmt(pos));
        } else {
            inner.addContinue(pos);
        }
    }

    void addAbstract(final Position pos) {
        if (inner == null) {
            stack.push(new AbstractStmt(pos));
        } else {
            inner.addAbstract(pos);
        }
    }

    void addNull(final Position pos) {
        if (inner == null) {
            stack.push(new NullStmt(pos));
        } else {
            inner.addNull(pos);
        }
    }

    void addAssignment(final Position pos, final int varLevel) {
        if (inner == null) {
            Node nameNode = stack.pop();
            AssignmentNode assignmentNode = new AssignmentNode(pos, varLevel);
            assignmentNode.setLeft(nameNode);
            stack.push(assignmentNode);
        } else {
            inner.addAssignment(pos, varLevel);
        }
    }

    void addIf(final Position pos) {
        if (inner == null) {
            IfStmt ifs = new IfStmt(pos);
            stack.push(ifs);
            inner = new AbstractSyntaxTree();
        } else {
            inner.addIf(pos);
        }
    }

    void addWhileLoop(final Position pos) {
        if (inner == null) {
            WhileStmt ws = new WhileStmt(pos);
            stack.push(ws);
            inner = new AbstractSyntaxTree();
        } else {
            inner.addWhileLoop(pos);
        }
    }

    void addForLoop(final Position pos) {
        if (inner == null) {
            ForLoopStmt ws = new ForLoopStmt(pos);
            stack.push(ws);
            inner = new AbstractSyntaxTree();
        } else {
            inner.addForLoop(pos);
        }
    }

    void addDef(Position pos, String functionName) {
        if (inner == null) {
            DefStmt ds = new DefStmt(pos, new NameNode(pos, functionName));
            stack.push(ds);
            inner = new AbstractSyntaxTree();
        } else {
            inner.addDef(pos, functionName);
        }
    }

    void addCall(Position pos, String functionName) {
        if (inner == null) {
            FunctionCall functionCall = new FunctionCall(pos, new NameNode(pos, functionName));
            stack.push(functionCall);
            inner = new AbstractSyntaxTree();
        } else {
            inner.addCall(pos, functionName);
        }
    }

    void buildClass() {

    }

    void buildCall() {
        if (inner.inner == null) {
            inner.buildLine();
            BlockStmt bs = inner.getBlock();
            inner = null;
            FunctionCall call = (FunctionCall) stack.pop();

            // TODO: CLASS INIT

            call.arguments = bs;
            stack.push(call);

        } else {
            inner.buildCall();
        }
    }

    void buildCondition() {
        if (inner.inner == null) {
            inner.buildLine();
            BlockStmt bs = inner.getBlock();
            inner = null;
            ConditionStmt cs = (ConditionStmt) stack.pop();
            cs.condition = bs;
            stack.push(cs);
        } else {
            inner.buildCondition();
        }
    }

    void buildFunctionParams() {
        if (inner.inner == null) {
            inner.buildLine();
            BlockStmt bs = inner.getBlock();
            inner = null;
//            System.out.println(bs);
            DefStmt defStmt = (DefStmt) stack.pop();
            defStmt.params = bs;
            stack.push(defStmt);
        } else {
            inner.buildFunctionParams();
        }
    }

    void newAst() {
        if (inner == null) {
            inner = new AbstractSyntaxTree();
        } else {
            inner.newAst();
        }
    }

    void buildExpr() {
        if (inner == null) {
            if (inExpr) {
                inExpr = false;
                ArrayList<Node> list = new ArrayList<>();
                while (!stack.isEmpty()) {
                    Node node = stack.peek();
                    if (node instanceof LeafNode || node instanceof BinaryOperator || node instanceof UnaryExpr ||
                            (node instanceof FunctionCall && ((FunctionCall) node).arguments != null)) {
                        list.add(node);
                        stack.pop();
                    } else {
                        break;
                    }
                }
                Collections.reverse(list);

                if (!list.isEmpty()) {
//                    System.out.println(list);
                    Node node = parseExpr(list);
                    stack.push(node);
                }
            }
        } else {
            inner.buildExpr();
        }
    }

    void buildLine() {
        if (inner == null) {
            buildExpr();
            if (!stack.isEmpty()) {
                ArrayList<Node> list = new ArrayList<>();
                list.add(stack.pop());
                while (!stack.isEmpty()) {
                    Node node = stack.pop();
                    if (node instanceof BinaryExpr) {
                        ((BinaryExpr) node).setRight(list.get(0));
                        list.set(0, node);
                    } else if (node instanceof BlockStmt) {
                        if (list.isEmpty()) list.add(node);
                        else list.add(0, node);
                    } else if (node instanceof IfStmt) {
                        System.out.println(list);
                        ((IfStmt) node).doBlock = list.get(0);
                        if (list.size() == 2) {
                            ((IfStmt) node).elseBlock = list.get(1);
                        }
                        list.clear();
                        list.add(node);
                    } else if (node instanceof WhileStmt) {
                        if (list.isEmpty()) {
                            list.add(node);
                        } else {
                            ((WhileStmt) node).doBlock = list.get(0);
                            list.set(0, node);
                        }
                    } else if (node instanceof ForLoopStmt) {
                        if (list.isEmpty()) {
                            list.add(node);
                        } else {
                            ((ForLoopStmt) node).doBlock = list.get(0);
                            list.set(0, node);
                        }
                    } else if (node instanceof DefStmt) {
                        if (list.isEmpty()) {
                            list.add(node);
                        } else {
                            ((DefStmt) node).body = (BlockStmt) list.get(0);
                            list.set(0, node);
                        }
                    } else {
                        if (list.isEmpty()) list.add(node);
                        else list.set(0, node);
                    }
                }
//                System.out.println(list.get(0));
                block.addLine(list.get(0));
            }
        } else {
            inner.buildLine();
        }
    }

    void buildAst() {
        if (inner.inner == null) {
            BlockStmt blockStmt = inner.getBlock();
//            traverseEnvironmentVariables(blockStmt);
            stack.add(blockStmt);
            inner = null;
        } else {
            inner.buildAst();
        }
    }

    public BlockStmt getBlock() {
        return block;
    }

    private static Node parseExpr(ArrayList<Node> list) {
        while (list.size() > 1) {
            int maxPrecedence = 0;
            int index = 0;
            for (int i = 0; i < list.size(); i++) {
                Node node = list.get(i);
                if (node instanceof BinaryOperator) {
                    BinaryOperator bo = (BinaryOperator) node;
                    int pre = bo.getPrecedence();
                    if (pre > maxPrecedence && bo.noLeft() && bo.noRight()) {
                        maxPrecedence = pre;
                        index = i;
                    }
                } else if (node instanceof UnaryExpr) {
                    UnaryExpr ue = (UnaryExpr) node;
                    int pre = ue.getPrecedence();
                    if (pre > maxPrecedence && ue.noValue()) {
                        maxPrecedence = pre;
                        index = i;
                    }
                }
            }
            Node node = list.get(index);
            if (node instanceof UnaryExpr) {
                UnaryExpr ue = (UnaryExpr) node;
                ue.setValue(list.remove(index + 1));
            } else {
                BinaryOperator bo = (BinaryOperator) node;
                bo.setRight(list.remove(index + 1));
                bo.setLeft(list.remove(index - 1));
            }
        }
        return list.get(0);
    }
}
