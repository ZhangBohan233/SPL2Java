package interpreter;

import parser.BlockStmt;

public class Interpreter {

    public Interpreter() {

    }

    public Object interpret(BlockStmt root) {
        Environment env = new Environment(Environment.GLOBAL_SCOPE, null);

        long st = System.currentTimeMillis();
        Object obj = root.evaluate(env);
        long end = System.currentTimeMillis();
        System.out.println("Time used: " + (end - st) + " ms");
        return obj;
    }
}
