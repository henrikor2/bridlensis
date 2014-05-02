package bridlensis.env;

public interface TypeObject {

	public static enum Type {
		STRING, INTEGER, NAME, VARIABLE, SPECIAL
	}

	Type getType();

	String getValue();

}
