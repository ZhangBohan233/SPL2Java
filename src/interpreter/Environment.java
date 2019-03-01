package interpreter;

import parser.Node;
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

    private final static NotFoundToken NOT_FOUND_TOKEN = new NotFoundToken();

    public final static NullPointer NULL_POINTER = new NullPointer();

//    private final static int[] LOCAL_SCOPES = {LOOP_SCOPE, LOOP_INNER_SCOPE, IF_ELSE_SCOPE, TRY_CATCH_SCOPE};

    private Environment outer;

    private int scopeType;

    private HashMap<String, Object> heap;

    private HashMap<String, Object> variables;

    private HashMap<String, Object> constants;

    private HashMap<String, Object> locals;

    public boolean broken, paused, terminated;

    private Object returnValue;

    public Environment(final int scopeType, final Environment outer) {
        this.scopeType = scopeType;
        this.outer = outer;
        if (outer == null) {
            heap = new HashMap<>();
        } else {
            heap = outer.heap;
        }

        variables = new HashMap<>();
        constants = new HashMap<>();
        locals = new HashMap<>();

    }

    public void defineFunction(final String name, Object value, Position pos) {
        variables.put(name, value);
    }

    public void defineVar(final String name, Object value, Position pos) {
        if (containsKey(name)) {
            throw new SplException(String.format(
                    "Variable name '%s' is already defined in this scope, in '%s', at line %d",
                    name, pos.getFileName(), pos.getLineNumber()));
        }
        variables.put(name, value);
    }

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

    public void defineLocal(final String name, Object value, Position pos) {
        if (localGet(name) == NOT_FOUND_TOKEN) {
            locals.put(name, value);
            return;
        }
        throw new SplException(String.format(
                "Local variable name '%s' is already defined in this scope, in '%s', at line %d",
                name, pos.getFileName(), pos.getLineNumber()));
    }

    public void assign(final String name, Object value, Position pos) {
        if (locals.containsKey(name)) locals.put(name, value);
        else if (variables.containsKey(name)) variables.put(name, value);
        else if (constants.containsKey(name)) throw new SplException(
                String.format(
                        "Cannot assign constant values, in '%s', at line '%d'",
                        pos.getFileName(), pos.getLineNumber()));
        else {
            Environment out = outer;
            boolean sub = isSub();
            while (out != null) {
                if (sub && out.locals.containsKey(name)) {
                    out.locals.put(name, value);
                    return;
                }
                if (out.variables.containsKey(name)) {
                    out.variables.put(name, value);
                    return;
                }
                if (out.constants.containsKey(name)) {
                    throw new SplException(String.format(
                            "Cannot assign constant values, in '%s', at line '%d'",
                            pos.getFileName(), pos.getLineNumber()));
                }
                if (!out.isSub()) sub = false;
                out = out.outer;
            }
            throw new SplException(String.format("Name '%s' is not defined", name));
        }
    }

    public Object get(final String name, Position pos) {
        Object value = innerGet(name);
        if (value == NOT_FOUND_TOKEN) throw new SplException(
                String.format("Name '%s' is not defined, in '%s', at line %d",
                        name, pos.getFileName(), pos.getLineNumber()));
        else return value;
    }

    public boolean containsKey(final String name) {
        Object value = innerGet(name);
        return value == NOT_FOUND_TOKEN;
    }

    private Object localGet(final String name) {
        Object obj = locals.get(name);
        if (obj == null)
            if (isSub()) return outer.localGet(name);
            else return NOT_FOUND_TOKEN;
        else return obj;
    }

    private Object innerGet(final String name) {
        Object obj;
        obj = locals.get(name);
        if (obj != null) return obj;
        obj = constants.get(name);
        if (obj != null) return obj;
        obj = variables.get(name);
        if (obj != null) return obj;

        Environment out = outer;
        boolean sub = isSub();
        while (out != null) {
            if (sub) {
                obj = out.locals.get(name);
                if (obj != null) return obj;
            }
            obj = out.constants.get(name);
            if (obj != null) return obj;
            obj = out.variables.get(name);
            if (obj != null) return obj;

            if (!out.isSub()) sub = false;
            out = out.outer;
        }

        obj = heap.get(name);
        if (obj != null) return obj;
        return NOT_FOUND_TOKEN;
    }

    public void invalidate() {
        locals.clear();
        variables.clear();
        constants.clear();
    }

    public boolean isSub() {
        return scopeType == SUB_SCOPE || scopeType == LOOP_SCOPE;
    }
}


class NotFoundToken {
//    @Override
//    public boolean equals(Object obj) {
//        return obj instanceof NotFoundToken;
//    }
}
