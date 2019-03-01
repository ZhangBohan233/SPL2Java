import interpreter.Interpreter;
import parser.BlockStmt;
import parser.Node;
import parser.ParseException;
import parser.Parser;
import tokenizer.LexerException;
import tokenizer.TokenLib;
import tokenizer.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Spl {

    private static String fileName, dir;

    private static ArrayList<String> splArgs;

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (fileName != null) {
                splArgs.add(arg);
            } else {
                if (arg.charAt(0) == '-') {

                } else if (arg.toLowerCase().equals("help")) {

                    System.exit(0);
                } else {
                    fileName = arg;
                    dir = getDir(fileName);
                    splArgs = new ArrayList<>();
                    splArgs.add(fileName);
                }
            }
        }
    }

    private static String getDir(String file) {
        File f = new File(file);
        return f.getParent();
    }

    public static void main(String[] args) {
        parseArgs(args);

        executeSpl();
    }

    private static void executeSpl() {

        try {
            TokenLib.init();

            BufferedReader br = new BufferedReader(new FileReader(fileName));

            Tokenizer tokenizer = new Tokenizer();
            tokenizer.setFileName(fileName);
            tokenizer.tokenize(br);

            tokenizer.printTokens();

            Parser parser = new Parser(tokenizer);

            BlockStmt ast = parser.parse();
            System.out.println("========== Abstract Syntax Tree ==========");
            System.out.println(ast);
            System.out.println("========== End of Abstract Syntax Tree ==========");

            Interpreter interpreter = new Interpreter();
            Object result = interpreter.interpret(ast);

            System.out.println(result);

        } catch (IOException e) {
            //
        } catch (LexerException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Parse exception");
            e.printStackTrace();
        }
    }
}
