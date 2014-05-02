package bridlensis.env;

public class SimpleTypeObject implements TypeObject {

	private final Type type;
	private final String value;

	public SimpleTypeObject(Type type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getValue() {
		if (type == Type.STRING) {
			return "\"" + value + "\"";
		}
		return value;
	}

	@Override
	public String toString() {
		return type + "[" + value + "]";
	}

}
