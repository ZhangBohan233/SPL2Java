package tokenizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class TokenLib {

    private final static String[] SYMBOLS = new String[]{"{", "}", ".", ","};

    private final static String[] MIDDLE = new String[]{"(", ")", "[", "]"};

    private final static String[] OTHERS = new String[]{"=", "@", ":"};

    private final static String[] LAZY = {"&&", "||"};
    private final static String[] OMITS = {"\n", "\r", "\t", " "};
    private final static String[] OP_EQ = {"+", "-", "*", "/", "%", "&", "^", "|", "<<", ">>"};
    private final static String[] NO_BUILD_LINE = {"else", "catch", "finally"};

    private final static String[] RESERVED = {"class", "function", "def", "if", "else", "new", "extends",
            "return", "break", "continue", "true", "false", "null", "operator", "while", "for", "import",
            "throw", "try", "catch", "finally", "abstract", "const", "var", "let"};

    private final static String[][] BINARY_OPERATORS = {{"+", "add"}, {"-", "sub"}, {"*", "mul"}, {"/", "div"},
            {"%", "mod"}, {"<", "lt"}, {">", "gt"}, {"<=", "le"}, {">=", "ge"}, {"==", "eq"}, {"!=", "neq"},
            {"&&", "and"}, {"||", "or"}, {"&", "band"}, {"^", "xor"}, {"|", "bor"}, {"<<", "lshift"}, {">>", "rshift"},
            {"===", ""}, {"!==", ""}, {"instanceof", ""}};

    private final static String[][] UNARY_OPERATORS = {{"!", "not"}};

    private final static String[][] ESCAPES = {};

    private static HashSet<String> symbols, middle, others, reserved, lazy, omits, opEq, noBuildLine;

    private static HashMap<String, String> binaryOperators, unaryOperators, escapes;

    private static HashMap<String, Integer> precedenceTable;

    @SuppressWarnings("unchecked")
    public static void init() {

        HashSet<String>[] sets = new HashSet[]{symbols, middle, others, reserved, lazy, omits, opEq, noBuildLine};
        String[][] names = {SYMBOLS, MIDDLE, OTHERS, RESERVED, LAZY, OMITS, OP_EQ, NO_BUILD_LINE};

        for (int i = 0; i < sets.length; i++) {
            sets[i] = new HashSet<>();
            sets[i].addAll(Arrays.asList(names[i]));
        }
        symbols = sets[0];
        middle = sets[1];
        others = sets[2];
        reserved = sets[3];
        lazy = sets[4];
        omits = sets[5];
        opEq = sets[6];
        noBuildLine = sets[7];

        HashMap<String, String>[] maps = new HashMap[]{binaryOperators, unaryOperators, escapes};
        String[][][] entries = {BINARY_OPERATORS, UNARY_OPERATORS, ESCAPES};

        for (int i = 0; i < maps.length; i++) {
            maps[i] = new HashMap<>();
            for (String[] arr : entries[i]) {
                maps[i].put(arr[0], arr[1]);
            }
        }

        binaryOperators = maps[0];
        unaryOperators = maps[1];
        escapes = maps[2];

        initPrecedence();
    }

    private static void initPrecedence() {
        precedenceTable = new HashMap<>();
        precedenceTable.put("+", 50);
        precedenceTable.put("-", 50);
        precedenceTable.put("*", 100);
        precedenceTable.put("/", 100);
        precedenceTable.put("%", 100);
        precedenceTable.put(">", 25);
        precedenceTable.put("<", 25);
        precedenceTable.put(">=", 25);
        precedenceTable.put("<=", 25);
        precedenceTable.put("==", 20);
        precedenceTable.put("!=", 20);
        precedenceTable.put("===", 20);
        precedenceTable.put("!==", 20);
        precedenceTable.put("&&", 5);
        precedenceTable.put("||", 5);
        precedenceTable.put("&", 12);
        precedenceTable.put("^", 11);
        precedenceTable.put("|", 10);
        precedenceTable.put("<<", 40);
        precedenceTable.put(">>", 40);
        precedenceTable.put(".", 500);
        precedenceTable.put("!", 200);
        precedenceTable.put("neg", 200);
        precedenceTable.put("return", 1);
        precedenceTable.put("throw", 1);
        precedenceTable.put("instanceof", 25);
        precedenceTable.put("=>", 500);
        precedenceTable.put("+=", 2);
        precedenceTable.put("-=", 2);
        precedenceTable.put("*=", 2);
        precedenceTable.put("/=", 2);
        precedenceTable.put("%=", 2);
        precedenceTable.put("&=", 2);
        precedenceTable.put("^=", 2);
        precedenceTable.put("|=", 2);
        precedenceTable.put("<<=", 2);
        precedenceTable.put(">>=", 2);
    }

    static boolean inAll(String s) {
        return symbols.contains(s) || others.contains(s) || middle.contains(s) || binaryOperators.containsKey(s) ||
                unaryOperators.containsKey(s);
    }

    static boolean inOpEq(String fullSymbol) {
        String sub = fullSymbol.substring(0, fullSymbol.length() - 1);
        return opEq.contains(sub);
    }

    static boolean inOmits(String s) {
        return omits.contains(s);
    }

    public static boolean isReserved(String s) {
        return reserved.contains(s);
    }

    public static boolean inNoBuildLine(String s) {
        return noBuildLine.contains(s);
    }

    public static boolean isNormalUnaryOperator(String s) {
        return unaryOperators.containsKey(s);
    }

    public static boolean isBinaryOperator(String s) {
        return binaryOperators.containsKey(s);
    }

    public static boolean isSymbol(String s) {
        return symbols.contains(s);
    }

    public static boolean isOpEq(String full) {
        String op = full.substring(0, full.length() - 1);
        return opEq.contains(op);
    }

    public static int getPrecedence(String op) {
        return precedenceTable.get(op);
    }
}
