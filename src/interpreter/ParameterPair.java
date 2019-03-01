package interpreter;

public class ParameterPair {

    public String name;

    public Object defaultValue;

    public ParameterPair(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }
}
