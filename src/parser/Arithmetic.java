package parser;

import interpreter.SplException;
import parser.Node;

public abstract class Arithmetic {

    static Object arithmetic(final Long left, final Object right, final String op) {
        String rType = right.getClass().getTypeName();
        switch (rType) {
            case "java.lang.Long":
                return longArithmetic(left, (Long) right, op);
            case "IntNode":
                return longArithmetic(left, ((IntNode) right).value, op);
            case "java.lang.Double":
                return longArithmetic(left, (Double) right, op);
            case "FloatNode":
                return longArithmetic(left, ((FloatNode) right).value, op);
            default:
                throw new SplException(String.format("Unsupported arithmetic type: Long %s %s", op, rType));
        }
    }

    static Object arithmetic(final Double left, final Object right, final String op) {
        String rType = right.getClass().getTypeName();
        switch (rType) {
            case "java.lang.Long":
                return doubleArithmetic(left, (Long) right, op);
            case "IntNode":
                return doubleArithmetic(left, ((IntNode) right).value, op);
            case "java.lang.Double":
                return doubleArithmetic(left, (Double) right, op);
            case "FloatNode":
                return doubleArithmetic(left, ((FloatNode) right).value, op);
            default:
                throw new SplException(String.format("Unsupported arithmetic type: Long %s %s", op, rType));
        }
    }

    private static Object longArithmetic(final Long left, final Long right, final String op) {
        switch (op) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                return left / right;
            case "%":
                return left % right;
            case ">":
                return left > right;
            case "<":
                return left < right;
            case ">=":
                return left >= right;
            case "<=":
                return left <= right;
            case "<<":
                return left << right;
            case ">>":
                return left >> right;
            case "&":
                return left & right;
            case "^":
                return left ^ right;
            case "|":
                return left | right;
            case "==":
                return left.equals(right);
            case "!=":
                return !left.equals(right);
            case "===":
                return left.equals(right);
            case "!==":
                return !left.equals(right);
            default:
                throw new SplException(String.format("Unsupported operation %s between int and int", op));
        }
    }

    private static Object longArithmetic(final Long left, final Double right, final String op) {
        switch (op) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                return left / right;
            case "%":
                return left % right;
            case ">":
                return left > right;
            case "<":
                return left < right;
            case ">=":
                return left >= right;
            case "<=":
                return left <= right;
            case "==":
                return left.longValue() == right.doubleValue();
            case "!=":
                return left.longValue() != right.doubleValue();
            case "===":
                return false;
            case "!==":
                return true;
            default:
                throw new SplException(String.format("Unsupported operation %s between int and float", op));
        }
    }

    static Object doubleArithmetic(final Double left, final Double right, final String op) {
        switch (op) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                return left / right;
            case "%":
                return left % right;
            case ">":
                return left > right;
            case "<":
                return left < right;
            case ">=":
                return left >= right;
            case "<=":
                return left <= right;
            case "==":
                return left.equals(right);
            case "!=":
                return !left.equals(right);
            case "===":
                return left.equals(right);
            case "!==":
                return !left.equals(right);
            default:
                throw new SplException(String.format("Unsupported operation %s between float and float", op));
        }
    }

    static Object doubleArithmetic(final Double left, final Long right, final String op) {
        switch (op) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                return left / right;
            case "%":
                return left % right;
            case ">":
                return left > right;
            case "<":
                return left < right;
            case ">=":
                return left >= right;
            case "<=":
                return left <= right;
            case "==":
                return left.doubleValue() == right.longValue();
            case "!=":
                return left.doubleValue() != right.longValue();
            case "===":
                return false;
            case "!==":
                return true;
            default:
                throw new SplException(String.format("Unsupported operation %s between float and float", op));
        }
    }

}
