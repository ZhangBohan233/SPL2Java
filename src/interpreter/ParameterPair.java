package interpreter;

public class ParameterPair {

//    public String name;

    public Variable variable;

    public Object defaultValue;

    public ParameterPair(Variable nameVar, Object defaultValue) {
        this.variable = nameVar;
        this.defaultValue = defaultValue;
    }
}
