package interpreter;

import parser.Parser;

import java.util.HashMap;

public class EnvOptimizer {

    private static int envCounter = 0;

    private int envId;

    private int scopeType;

    private int constCounter;

    private int variableCounter;

    private HashMap<String, Integer> varMap = new HashMap<>();

    private HashMap<String, Integer> constMap = new HashMap<>();

    private EnvOptimizer outer;

    public EnvOptimizer(final int type, final EnvOptimizer outer) {
        envId = envCounter++;
        this.outer = outer;
        this.scopeType = type;
    }

    public Variable addVar(final String name) {
        int temp = variableCounter;
        varMap.put(name, variableCounter++);
        return new Variable(temp, 0, Parser.VAR);
    }

    public void addConst(final String name) {
        constMap.put(name, constCounter++);
    }

    public Variable get(final String name) {
        return innerGet(name, 0);
    }

    private Variable innerGet(final String name, int depth) {
        Integer rep = varMap.get(name);
        if (rep != null) {
            return new Variable(rep, depth, Parser.ASSIGN);
        }
        rep = constMap.get(name);
        if (rep != null) {
            return new Variable(rep, depth, Parser.ASSIGN);
        }
        if (outer != null) {
            return outer.innerGet(name, depth + 1);
        } else {
            return new HeapVariable(name);
        }
    }

    public int getConstCounter() {
        return constCounter;
    }

    public int getVariableCounter() {
        return variableCounter;
    }

    public VariableCount getVariableCount() {
        return new VariableCount(variableCounter, constCounter);
    }
}
