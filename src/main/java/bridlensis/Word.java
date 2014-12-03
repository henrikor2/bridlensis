package bridlensis;

import bridlensis.env.TypeObject;

class Word implements TypeObject {

	private final String value;
	private final Type type;

	public Word(String value) {
		this.value = value;
		if (value.isEmpty()) {
			type = Type.SPECIAL;
		} else if (isString()) {
			type = Type.STRING;
		} else if (value.charAt(0) == '$' || value.charAt(0) == '{'
				|| value.charAt(0) == '/') {
			type = Type.SPECIAL;
		} else if (isNumeric()) {
			type = Type.INTEGER;
		} else {
			type = Type.NAME;
		}
	}

	private boolean isString() {
		return InputReader.STRING_MARKERS.indexOf(value.charAt(0)) != -1;
	}

	private boolean isNumeric() {
		char[] charArray = value.toCharArray();
		for (int i = charArray[0] == '-' ? 1 : 0; i < charArray.length; i++) {
			if (!Character.isDigit(charArray[i]))
				return false;
		}
		return true;
	}

	public String asName() {
		return value.toLowerCase();
	}

	public String asBareString() {
		if (isString()) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Word[" + value + "]";
	}

}