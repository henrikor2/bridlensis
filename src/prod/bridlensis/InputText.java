package bridlensis;

class InputText {

	private String text;
	private int cursor;

	@Override
	public String toString() {
		return text;
	}

	protected void set(String line, int startPos) {
		text = line;
		cursor = startPos;
	}

	protected boolean endsWith(char c, String ignorePattern) {
		for (int i = text.length() - 1; i >= cursor; i--) {
			if (text.charAt(i) == c) {
				return true;
			} else if (ignorePattern.indexOf(text.charAt(i)) == -1) {
				break;
			}
		}
		return false;
	}

	protected void append(String nextLine) {
		if (!nextLine.startsWith(InputReader.NEW_LINE)) {
			throw new IllegalArgumentException(
					"Expecting line feed at the beginning of next line");
		}
		text += nextLine;
	}

	protected boolean isAtEnd() {
		return cursor >= text.length();
	}

	protected void goToEnd() {
		cursor = text.length();
	}

	protected void skip(int numberOfChars) {
		cursor = Math.min(cursor + numberOfChars, text.length());
	}

	protected boolean charAtCursorIn(String pattern) {
		if (cursor >= text.length()) {
			return false;
		}
		return pattern.indexOf(text.charAt(cursor)) != -1;
	}

	protected boolean cursorPrecededBy(String str) {
		return text.substring(cursor - str.length(), cursor).equals(str);
	}

	protected boolean cursorFollowedBy(String str) {
		if (text.length() < cursor + str.length()) {
			return false;
		}
		return text.substring(cursor, cursor + str.length()).equals(str);
	}

	protected boolean seekString(String str) {
		int index = text.indexOf(str, cursor);
		if (index != -1) {
			cursor = index;
			return true;
		} else {
			cursor = text.length();
			return false;
		}
	}

	protected boolean seekChars(String pattern) {
		while (cursor < text.length()) {
			if (pattern.indexOf(text.charAt(cursor)) != -1) {
				return true;
			}
			cursor++;
		}
		return false;
	}

	protected char charAtCursor() {
		return text.charAt(cursor);
	}

	protected int cursorPos() {
		return cursor;
	}
}
