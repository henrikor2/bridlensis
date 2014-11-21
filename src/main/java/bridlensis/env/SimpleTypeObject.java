package bridlensis.env;

public class SimpleTypeObject implements TypeObject {

	private static final char STRING_MARKER = '"';

	private final Type type;
	private final String value;

	private SimpleTypeObject(Type type, String value) {
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
			return String.format("%1$c%2$s%1$c", STRING_MARKER,
					strEncode(value, STRING_MARKER));
		}
		return value;
	}

	@Override
	public String toString() {
		return type + "[" + value + "]";
	}

	public static TypeObject string(String value) {
		return new SimpleTypeObject(Type.STRING, value);
	}

	public static TypeObject special(String value) {
		return new SimpleTypeObject(Type.SPECIAL, value);
	}

	public static TypeObject integer(int value) {
		return new SimpleTypeObject(Type.INTEGER, Integer.toString(value));
	}

	public static String stripString(TypeObject obj) {
		if (obj.getType() == Type.STRING) {
			return obj.getValue().substring(1, obj.getValue().length() - 1);
		}
		return obj.getValue();
	}

	private static String strEncode(String text, char cencodedCharacter) {
		return text.replaceAll("(?<!\\$\\\\)\\" + cencodedCharacter,
				"\\$\\\\\\" + cencodedCharacter);
	}

}
