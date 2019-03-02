package interpreter;

import parser.Node;
import parser.Parser;
import tokenizer.Position;
import util.Utility;

import java.util.Arrays;
import java.util.HashMap;

public class Environment {

    public final static int GLOBAL_SCOPE = 0;
    public final static int CLASS_SCOPE = 1;
    public final static int FUNCTION_SCOPE = 2;
    public final static int LOOP_SCOPE = 3;
    public final static int SUB_SCOPE = 4;
//    public final static int CLASS_SCOPE = 1;
//    public final static int FUNCTION_SCOPE = 2;
//    public final static int LOOP_SCOPE = 3;
//    public final static int IF_ELSE_SCOPE = 4;
//    public final static int TRY_CATCH_SCOPE = 5;
//    public final static int LOOP_INNER_SCOPE = 6;

    public final static NullPointer NULL_POINTER = new NullPointer();

//    private final static int[] LOCAL_SCOPES = {LOOP_SCOPE, LOOP_INNER_SCOPE, IF_ELSE_SCOPE, TRY_CATCH_SCOPE};

    private Environment outer;

    private int scopeType;

    private HashMap<String, Object> heap;

//    private HashMap<String, Object> variables;
//
//    private HashMap<String, Object> constants;

    private Object[] variables;

    private Object[] constants;

//    private HashMap<String, Object> locals;

    public boolean broken, paused, terminated;

    private Object returnValue;

    private static int environmentCounter = 0;

    private int environmentId;

    public Environment(final int scopeType, final Environment outer, final VariableCount variableCount) {
        this.scopeType = scopeType;
        this.outer = outer;
        environmentId = environmentCounter++;
        if (outer == null) {
            heap = new HashMap<>();
        } else {
            heap = outer.heap;
        }

        variables = new Object[variableCount.getVarCount()];
        constants = new Object[variableCount.getConstCount()];
//        locals = new HashMap<>();

    }

//    public void defineFunction(final Variable variable, Object value, Position pos) {
//
//    }

    public void defineVar(final Variable variable, Object value, Position pos) {
        variables[variable.index] = value;
    }

//    public void defineVar(final String name, Object value, Position pos) {
//        if (containsKey(name)) {
//            throw new SplException(String.format(
//                    "Variable name '%s' is already defined in this scope, in '%s', at line %d",
//                    name, pos.getFileName(), pos.getLineNumber()));
//        }
//        variables.put(name, value);
//    }

    public void pause() {
        if (scopeType == LOOP_SCOPE) {
            paused = true;
        } else {
            outer.pause();
        }
    }

    public void resume() {
        if (scopeType == LOOP_SCOPE) {
            paused = false;
        } else {
            outer.resume();
        }
    }

    public void breakLoop() {
        if (scopeType == LOOP_SCOPE) {
            broken = true;
        } else {
            outer.breakLoop();
        }
    }

    public void terminate(Object returnValue) {
        if (scopeType == FUNCTION_SCOPE) {
            terminated = true;
            this.returnValue = returnValue;
        } else {
            outer.terminate(returnValue);
        }
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void defineConst(final String name, Object value, Position pos) {

    }

//    public void defineLocal(final String name, Object value, Position pos) {
//        if (localGet(name) == NOT_FOUND_TOKEN) {
//            locals.put(name, value);
//            return;
//        }
//        throw new SplException(String.format(
//                "Local variable name '%s' is already defined in this scope, in '%s', at line %d",
//                name, pos.getFileName(), pos.getLineNumber()));
//    }

    public void assign(final Variable variable, Object value, Position pos) {
        Environment env = this;
        for (int i = 0; i < variable.scopeDistance; i++) {
            env = env.outer;
        }
        env.variables[variable.index] = value;
    }

//    public void assign(final String name, Object value, Position pos) {
////        if (locals.containsKey(name)) locals.put(name, value);
//        if (variables.containsKey(name)) variables.put(name, value);
//        else if (constants.containsKey(name)) throw new SplException(
//                String.format(
//                        "Cannot assign constant values, in '%s', at line '%d'",
//                        pos.getFileName(), pos.getLineNumber()));
//        else {
//            Environment out = outer;
////            boolean sub = isSub();
//            while (out != null) {
////                if (sub && out.locals.containsKey(name)) {
////                    out.locals.put(name, value);
////                    return;
////                }
//                if (out.variables.containsKey(name)) {
//                    out.variables.put(name, value);
//                    return;
//                }
//                if (out.constants.containsKey(name)) {
//                    throw new SplException(String.format(
//                            "Cannot assign constant values, in '%s', at line '%d'",
//                            pos.getFileName(), pos.getLineNumber()));
//                }
////                if (!out.isSub()) sub = false;
//                out = out.outer;
//            }
//            throw new SplException(String.format("Name '%s' is not defined", name));
//        }
//    }

    public Object get(final Variable variable, Position pos) {
        return innerGet(variable, pos);
    }

    private Object innerGet(final Variable variable, Position pos) {
        if (variable == null) {
            throw new NullPointerException("at line " + pos.getLineNumber());
        }
        if (variable.scopeDistance == -1) {
            // global variable
            String name = ((HeapVariable) variable).name;
            Object res = heap.get(name);
            if (res == null) {
                throw new SplException(String.format("Name '%s' is not defined, in '%s', at line %d",
                        name, pos.getFileName(), pos.getLineNumber()));
            } else {
                return res;
            }
        } else {
            Environment env = this;
            for (int i = 0; i < variable.scopeDistance; i++) {
                env = env.outer;
            }
            return env.variables[variable.index];
        }
    }

    public void invalidate() {
//        locals.clear();
//        variables.clear();
//        constants.clear();
    }

    public boolean isSub() {
        return scopeType == SUB_SCOPE || scopeType == LOOP_SCOPE;
    }
}


