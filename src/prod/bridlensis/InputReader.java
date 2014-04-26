package bridlensis;

import java.util.Scanner;

public class InputReader {

	private static final char LINE_CONTINUE = '\\';
	private static final String SPACE_MARKERS = " \t\r\n" + LINE_CONTINUE;
	private static final String COMMENT_MARKERS = ";#";
	private static final String COMMENTBLOCK_START = "/*";
	private static final String COMMENTBLOCK_END = "*/";
	private static final String TAIL_MARKERS = "=+(,)!<>";
	private static final String WORD_MARKERS = TAIL_MARKERS + SPACE_MARKERS
			+ COMMENT_MARKERS + COMMENTBLOCK_START.charAt(0);
	private static final String STRING_MARKERS = "\"'";
	private static final String CHAR_MASK = "$\\";
	private static final String LANGSTRING_START = "$(";
	private static final String LANGSTRING_END = ")";

	private static final WordTail EMPTY_TAIL = new WordTail();

	static class Word {

		private final String value;

		public Word(String value) {
			this.value = value;
		}

		public boolean isString() {
			return (STRING_MARKERS.indexOf(value.charAt(0)) != -1);
		}

		public boolean isUntouchable() {
			if (value.charAt(0) == '$') {
				return true; // It's NSIS constant or lang string
			}
			// Check for numeric value
			char[] charArray = value.toCharArray();
			for (int i = charArray[0] == '-' ? 1 : 0; i < charArray.length; i++) {
				if (!Character.isDigit(charArray[i]))
					return false; // It's not numeric
			}
			return true;// It's numeric
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

		public String getValue() {
			return value;
		}
	}

	static class WordTail {

		private String pattern = "";

		private void add(char c) {
			pattern += c;
		}

		@Override
		public String toString() {
			return pattern;
		}

		public boolean isCompilerCommand() {
			return pattern.equals("!");
		}

		public boolean isAssignment() {
			return pattern.startsWith("=");
		}

		public boolean isFunctionArgsOpen() {
			return pattern.startsWith("(");
		}

		public boolean isConcatenation() {
			return pattern.endsWith("+");
		}

		public boolean containsFunctionArgsClose() {
			return pattern.matches(".*\\).*");
		}

		public boolean isFunctionArgsClose() {
			return pattern.startsWith(")");
		}

		public boolean isComparison() {
			return pattern.matches(".*(==|!=|[<>]).*");
		}

		public String getComparison() {
			return pattern.replaceAll("[^=!\\<\\>]", "");
		}
	}

	private Scanner input;
	private InputText text;
	private String indent;
	private WordTail tail;
	private int linesRead;

	public InputReader(Scanner input) {
		this.input = input;
		this.linesRead = 0;
		this.text = new InputText();
	}

	@Override
	public String toString() {
		return text.toString();
	}

	public int getLinesRead() {
		return linesRead;
	}

	public String getIndent() {
		return indent;
	}

	public boolean goToNextStatement() throws InvalidSyntaxException {
		if (input.hasNextLine()) {
			String str = pullNextLine();
			if (str.length() > 0
					&& (str.charAt(0) == Parser.UTF16LE_BOM || str.charAt(0) == Parser.UTF16BE_BOM)) {
				// Skip UTF-16 BOM
				str = str.substring(1);
			}
			int startPos = indexOfNextNonSpace(str);
			indent = str.substring(0, startPos);
			text.set(str, startPos);
			while (text.endsWith(LINE_CONTINUE, SPACE_MARKERS)) {
				// Ensure line continuation
				text.append(Parser.NEWLINE_MARKER + pullNextLine());
			}
			skipCommentsAtCursor();
			tail = EMPTY_TAIL;
			return true;
		}
		return false;
	}

	public String getCurrentStatement() throws InvalidSyntaxException {
		if (text.seekString(COMMENTBLOCK_START)) {
			skipCommentBlockAtCursor();
		}
		return text.toString();
	}

	public boolean hasNextWord() {
		return !text.isAtEnd();
	}

	public Word nextWord() throws InvalidSyntaxException {
		if (text.isAtEnd()) {
			throw new InvalidSyntaxException("Unexpected end of statement");
		}

		// Cursor is at the start of the next word
		int start = text.cursorPos();

		// Move cursor to the end of the word
		if (text.charAtCursorIn(STRING_MARKERS)) {
			// Parse string
			String strOpenChar = Character.toString(text.charAtCursor());
			do {
				text.skip(1);
				if (!text.seekString(strOpenChar)) {
					throw new InvalidSyntaxException("Unterminated string");
				}
			} while (text.cursorPrecededBy(CHAR_MASK));
			text.skip(1);
		} else if (text.seekChars(WORD_MARKERS) && isCursorAtLangString()) {
			// Parse LangString
			if (!text.seekString(LANGSTRING_END)) {
				throw new InvalidSyntaxException("Unterminated LangString");
			}
			text.skip(1);
		}

		// Save the word before moving cursor any further
		String word = text.toString().substring(start, text.cursorPos());

		// Collect tail characters
		tail = new WordTail();
		while (!text.isAtEnd()) {
			skipCommentsAtCursor();
			if (text.charAtCursorIn(TAIL_MARKERS)) {
				tail.add(text.charAtCursor());
			} else if (!text.charAtCursorIn(WORD_MARKERS)) {
				break;
			}
			text.skip(1);
		}

		return new Word(word);
	}

	public WordTail getWordTail() {
		return tail;
	}

	private void skipCommentsAtCursor() throws InvalidSyntaxException {
		if (text.charAtCursorIn(COMMENT_MARKERS)) {
			// Line comment will always end the statement
			text.goToEnd();
		} else if (text.cursorFollowedBy(COMMENTBLOCK_START)) {
			skipCommentBlockAtCursor();
		}
	}

	private void skipCommentBlockAtCursor() throws InvalidSyntaxException {
		// Cursor is at the start of comment block
		text.skip(COMMENTBLOCK_START.length());

		// Pull new lines until comment block end is found
		while (!text.seekString(COMMENTBLOCK_END)) {
			text.goToEnd();
			String nextLine = pullNextLine();
			text.append(Parser.NEWLINE_MARKER + nextLine);
			text.skip(Parser.NEWLINE_MARKER.length()
					+ indexOfNextNonSpace(nextLine));
		}

		// Move cursor beyond comment block
		text.skip(COMMENTBLOCK_END.length());
	}

	private boolean isCursorAtLangString() {
		return text.charAtCursor() == LANGSTRING_START.charAt(LANGSTRING_START
				.length() - 1)
				&& text.cursorPrecededBy(LANGSTRING_START.substring(0,
						LANGSTRING_START.length() - 1));
	}

	private String pullNextLine() throws InvalidSyntaxException {
		if (!input.hasNextLine()) {
			throw new InvalidSyntaxException("Unexpected end of file");
		}
		linesRead++;
		return input.nextLine();
	}

	private static int indexOfNextNonSpace(String text) {
		int index = 0;
		while (index < text.length()
				&& SPACE_MARKERS.indexOf(text.charAt(index)) != -1) {
			index++;
		}
		return index;
	}

}
