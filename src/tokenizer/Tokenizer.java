package tokenizer;

import util.TypeChecker;
import util.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;

public class Tokenizer {

    private ArrayList<Token> tokens;

    private TypeChecker typeChecker = new TypeChecker();

    private String fileName;

    public Tokenizer() {
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void tokenize(final BufferedReader inputStream) throws IOException, LexerException {
        tokens = new ArrayList<>();

        int lineNumber = 1;
        String line;
        boolean inDoc = false;
        while ((line = inputStream.readLine()) != null) {
            int lastIndex = tokens.size();
            inDoc = proceedLine(line, new Position(lineNumber, fileName), inDoc);
            findImport(lastIndex, tokens.size());
            lineNumber++;
        }
        tokens.add(Token.eofToken(fileName));
    }

    private boolean proceedLine(final String line, Position position, boolean inDoc) throws LexerException {
        boolean inSingle = false, inDouble = false;
        StringBuilder literal = new StringBuilder();
        StringBuilder nonLiteral = new StringBuilder();

        int length = line.length();
        for (int i = 0; i < length; i++) {
            char ch = line.charAt(i);
            if (!(inSingle || inDouble)) {
                if (inDoc) {
                    if (ch == '*' && i < length - 1 && line.charAt(i + 1) == '/') {
                        inDoc = false;
                        i += 2;
                        continue;
                    }
                } else {
                    if (ch == '/' && i < length - 1 && line.charAt(i + 1) == '*') {
                        inDoc = true;
                        i += 1;
                    }
                }
            }

            if (!inDoc) {
                if (inDouble) {
                    if (ch == '"') {
                        inDouble = false;
                        tokens.add(new LiteralToken(position, literal.toString()));
                        literal.setLength(0);
                        continue;
                    }
                } else if (inSingle) {
                    if (ch == '\'') {
                        inSingle = false;
                        tokens.add(new LiteralToken(position, literal.toString()));
                        literal.setLength(0);
                        continue;
                    }
                } else {
                    if (ch == '"') {
                        inDouble = true;
                        tokenizeLine(nonLiteral.toString(), position);
                        nonLiteral.setLength(0);
                        continue;
                    } else if (ch == '\'') {
                        inSingle = true;
                        tokenizeLine(nonLiteral.toString(), position);
                        nonLiteral.setLength(0);
                        continue;
                    }
                }

                if (inSingle || inDouble) {
                    literal.append(ch);
                } else {
                    nonLiteral.append(ch);
                    int nonLiteralLength = nonLiteral.length();
                    if (nonLiteralLength > 1 && nonLiteral.substring(nonLiteralLength - 2).equals("//")) {
                        // Line comments
                        tokenizeLine(nonLiteral.substring(nonLiteralLength - 2), position);
                        nonLiteral.setLength(0);
                        break;
                    }
                }
            }
        }

        if (nonLiteral.length() > 0) {
            tokenizeLine(nonLiteral.toString(), position);
            nonLiteral.setLength(0);
        }

        return inDoc;
    }

    private void tokenizeLine(final String nonLiteral, final Position pos) throws LexerException {
        ArrayList<String> lst = normalize(nonLiteral);
        for (String part : lst) {
            if (TypeChecker.isIdentifier(part)) {
                tokens.add(new IdToken(pos, part));
            } else if (TypeChecker.isDigits(part)) {
                tokens.add(new NumToken(pos, part, false));
            } else if (TypeChecker.isFloat(part)) {
                tokens.add(new NumToken(pos, part, true));
            } else if (TokenLib.inAll(part)) {
                tokens.add(new IdToken(pos, part));
            } else if (TokenLib.inOpEq(part)) {
                tokens.add(new IdToken(pos, part));
            } else if (part.equals(Token.EOL)) {
                tokens.add(new IdToken(pos, part));
            } else if (part.equals("=>")) {

            } else if (TokenLib.inOmits(part)) {

            } else {
                throw new LexerException(String.format("Unknown symbol: %s, in file %s, at line %d",
                        part, pos.getFileName(), pos.getLineNumber()));
            }
        }
    }

    private ArrayList<String> normalize(final String line) {
        ArrayList<String> lst = new ArrayList<>();
        if (line.length() > 0) {
            char lastChar = line.charAt(0);
            char thisChar;
            StringBuilder sb = new StringBuilder();
            sb.append(lastChar);
            for (int i = 1; i < line.length(); i++) {
                thisChar = line.charAt(i);
                if (typeChecker.concatenateAble(lastChar, thisChar)) {
                    sb.append(thisChar);
                } else {
                    addString(lst, sb.toString());
                    sb.setLength(0);
                    sb.append(thisChar);
                }
                lastChar = thisChar;
            }
            addString(lst, sb.toString());
        }
        return lst;
    }

    private void addString(ArrayList<String> list, final String s) {
        if (s.length() > 1 && s.charAt(s.length() - 1) == '.') {
            list.add(s.substring(0, s.length() - 1));
            list.add(String.valueOf(s.charAt(s.length() - 1)));
        } else {
            list.add(s);
        }
    }

    private void findImport(int lastIndex, int currentIndex) {

    }

    public void printTokens() {
        System.out.println(tokens);
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }
}
