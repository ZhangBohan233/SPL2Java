package tokenizer;

public class Position {

    private int lineNumber;
    private String fileName;

    public Position(final int lineNumber, final String fileName) {
        this.lineNumber = lineNumber;
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFileName() {
        return fileName;
    }
}
