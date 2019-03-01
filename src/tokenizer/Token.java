package tokenizer;

public class Token {

    public final static String EOL = ";";

    private Position position;

    Token(Position pos) {
        this.position = pos;
    }

    static Token eofToken(String fileName) {
        return new Token(new Position(-1, fileName));
    }

    @Override
    public String toString() {
        return "Token";
    }

    public boolean isIdentifier() {
        return false;
    }

    public boolean isLiteral() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isFloat() {
        throw new TokenException();
    }

    public String getValue() {
        throw new TokenException();
    }

    public boolean isEof() {
        return position.getLineNumber() == -1;
    }

    public Position getPosition() {
        return position;
    }
}


class IdToken extends Token {

    private String identifier;

    IdToken(Position pos, String identifier) {
        super(pos);

        this.identifier = identifier;
    }

    public String getValue() {
        return identifier;
    }

    @Override
    public boolean isIdentifier() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("IdToken(%s)", identifier);
    }
}


class NumToken extends Token {

    private String numString;
    private boolean isFloat;

    NumToken(Position pos, final String numString, final boolean isFloat) {
        super(pos);

        this.numString = numString;
        this.isFloat = isFloat;
    }

    @Override
    public String getValue() {
        return numString;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("NumToken(%s)", numString);
    }

    public boolean isFloat() {
        return isFloat;
    }
}

class LiteralToken extends Token {

    private String literal;

    LiteralToken(Position pos, String literal) {
        super(pos);

        this.literal = literal;
    }

    @Override
    public boolean isLiteral() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("LiteralToken(%s)", literal);
    }
}
