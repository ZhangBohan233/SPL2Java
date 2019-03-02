package parser;

import interpreter.EnvOptimizer;
import interpreter.Environment;
import tokenizer.Position;
import tokenizer.Token;
import tokenizer.TokenLib;
import tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.Stack;

public class Parser {

    private ArrayList<Token> tokens;

    public final static int ASSIGN = 0;
    public final static int CONST = 1;
    public final static int VAR = 2;
    public final static int GET = 3;
//    final static int LET = 3;

    public Parser(final Tokenizer tokenizer) {
        tokens = tokenizer.getTokens();
    }

    public BlockStmt parse() throws ParseException {
        AbstractSyntaxTree ast = new AbstractSyntaxTree();

        int i = 0;
        int afCount = 0;
        int callNest = 0;
        int braceCount = 0;
        int extraPrecedence = 0;
        int varLevel = ASSIGN;
        boolean inCondition = false;
        boolean inFunctionParams = false;
        Stack<Integer> classBraces = new Stack<>();  // records the brace number outside class

        while (true) {
            Token token = tokens.get(i);
            Token next;
            Position pos = token.getPosition();
            try {
                if (token.isIdentifier()) {
                    String sym = token.getValue();
                    if (TokenLib.isNormalUnaryOperator(sym)) {
                        ast.addUnaryOperator(pos, sym, extraPrecedence);
                    } else if (TokenLib.isBinaryOperator(sym)) {
                        if (sym.equals("-") && (i == 0 || isUnary(tokens.get(i - 1)))) {
                            ast.addUnaryOperator(pos, "-", extraPrecedence);
                        } else {
                            ast.addBinaryOperator(pos, sym, extraPrecedence);
                        }
                    } else if (TokenLib.isOpEq(sym)) {
                        ast.addBinaryOperatorWithAssignment(pos, sym, extraPrecedence);
                    } else {
                        switch (sym) {
                            case "{":
                                braceCount++;
                                ast.newAst();
                                break;
                            case "}":
                                braceCount--;
                                ast.buildLine();
                                ast.buildAst();
                                if (!classBraces.isEmpty() && classBraces.peek() == braceCount) {
                                    ast.buildClass();
                                    classBraces.pop();
                                } else {
                                    next = tokens.get(i + 1);
                                    if (!next.isIdentifier() || !TokenLib.inNoBuildLine(next.getValue())) {
                                        ast.buildExpr();
                                        ast.buildLine();
                                    }
                                }
                                break;
                            case "(":
                                extraPrecedence++;
                                break;
                            case ")":
                                if (extraPrecedence == 0) {
                                    if (callNest > 0) {
                                        ast.buildLine();
                                        ast.buildCall();
                                        callNest--;
                                    } else if (inCondition) {
                                        ast.buildExpr();
                                        ast.buildCondition();
                                        inCondition = false;
                                    } else if (inFunctionParams) {
//                                        ast.buildExpr();
                                        ast.buildFunctionParams();
                                        inFunctionParams = false;
                                    } else {
                                        throw new ParseException("Unexpected back parenthesis");
                                    }
                                } else {
                                    extraPrecedence--;
                                }
                                break;
                            case "=":
                                ast.buildExpr();
                                ast.addAssignment(pos, varLevel);
                                varLevel = ASSIGN;
                                break;
                            case ":":
                                break;
                            case ",":
//                                ast.buildExpr();
                                if (callNest > 0 || inFunctionParams) ast.buildLine();
                                break;
                            case ".":
                                break;
                            case "=>":
                                break;
                            case "var":
                                varLevel = VAR;
                                break;
                            case "const":
                                varLevel = CONST;
                                break;
//                            case "let":
//                                varLevel = LET;
//                                break;
                            case "true":
                                ast.addBoolean(pos, true);
                                break;
                            case "false":
                                ast.addBoolean(pos, false);
                                break;
                            case "null":
                                ast.addNull(pos);
                                break;
                            case "if":
                                inCondition = true;
                                ast.addIf(pos);
                                i += 1;
                                break;
                            case "else":
                                // Do nothing
                                break;
                            case "while":
                                inCondition = true;
                                ast.addWhileLoop(pos);
                                i += 1;
                                break;
                            case "for":
                                inCondition = true;
                                ast.addForLoop(pos);
                                i += 1;
                                break;
                            case "def":
                            case "function":
                                i += 1;
                                next = tokens.get(i);
                                assert next.isIdentifier();
                                String fName = next.getValue();
                                int pushBack = 1;
                                if (fName.equals("(")) {
                                    fName = "af-" + afCount;
                                    pushBack = 0;
                                    afCount += 1;
                                }
                                ast.addDef(pos, fName);
                                i += pushBack;
                                inFunctionParams = true;
                                break;
                            case "class":
                                classBraces.push(braceCount);
                                break;
                            case "return":
                                ast.addUnaryOperator(pos, "return", 0);
                                break;
                            case "break":
                                ast.addBreak(pos);
                                break;
                            case "continue":
                                ast.addContinue(pos);
                                break;
                            case "abstract":
                                ast.addAbstract(pos);
                                break;
                            case Token.EOL:

                                ast.buildExpr();
                                ast.buildLine();
                                break;
                            default:
                                next = tokens.get(i + 1);
                                if (next.isIdentifier()) {
                                    String nextIdentifier = next.getValue();
                                    if (nextIdentifier.equals("(")) {
                                        ast.addCall(pos, sym);
                                        callNest++;
                                        i++;
                                    } else if (nextIdentifier.equals("[")) {

                                    } else {
                                        ast.addName(pos, sym);
                                    }
                                } else {
                                    ast.addName(pos, sym);
                                }
                                break;
                        }
                    }
                } else if (token.isNumber()) {
                    if (token.isFloat()) {
                        ast.addFloat(pos, token.getValue());
                    } else {
                        ast.addInteger(pos, token.getValue());
                    }
                } else if (token.isLiteral()) {
                    ast.addLiteral(pos, token.getValue());
                } else if (token.isEof()) {
                    break;
                }
                i++;
            } catch (Exception e) {
                e.printStackTrace();
                throw new ParseException("Parse error in");
            }
        }

        BlockStmt blockStmt = ast.getBlock();
        blockStmt.lookUp(new EnvOptimizer(Environment.GLOBAL_SCOPE, null));
        return blockStmt;
    }

    private static boolean isUnary(Token token) {
        if (token.isIdentifier()) {
            String sym = token.getValue();
            if (sym.equals(Token.EOL)) {
                return true;
            } else {
                if (TokenLib.isBinaryOperator(sym)) return true;
                else if (TokenLib.isSymbol(sym)) return true;
                else if (TokenLib.isReserved(sym)) return true;
                else if (sym.equals("(")) return true;
                else return sym.equals("=");
            }
        } else return !token.isNumber();
    }
}
