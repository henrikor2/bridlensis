package bridlensis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class InputReader {

	private static final char LINE_CONTINUE = '\\';

	private static final String COMMENT_MARKERS = ";#";
	private static final String COMMENTBLOCK_START = "/*";
	private static final String COMMENTBLOCK_END = "*/";
	static final String STRING_MARKERS = "\"'";
	private static final String STRING_CHARMASK = "$\\";

	// Ignored characters
	private static final String SPACE_MARKERS = " \t\r\n" + LINE_CONTINUE;

	// Collect these characters between the words
	private static final String TAIL_MARKERS = "=+(,)!<>";

	// Word begins when cursor is not at any of these
	private static final String WORD_START_MARKERS = TAIL_MARKERS
			+ SPACE_MARKERS + COMMENT_MARKERS;

	// Word ends when cursor is at any of these
	private static final String WORD_END_MARKERS = WORD_START_MARKERS
			+ COMMENTBLOCK_START.charAt(0);

	// Special case words that must be checked separately
	private static final String LANGSTRING_START = "$(";
	private static final String LANGSTRING_END = ")";

	private static final WordTail EMPTY_TAIL = new WordTail("");

	private File file;
	private Scanner input;
	private InputText text;
	private String indent;
	private WordTail tail;
	private int linesRead;

	public InputReader(File file, String encoding) throws FileNotFoundException {
		this(new Scanner(file, encoding));
		this.file = file;
	}

	protected InputReader(Scanner scanner) {
		this.input = scanner;
		this.linesRead = 0;
		this.text = new InputText();
	}

	@Override
	public String toString() {
		return "InputReader[" + text.get() + "]";
	}

	public File getFile() {
		return file;
	}

	public int getLinesRead() {
		return linesRead;
	}

	public String getIndent() {
		return indent;
	}

	public void close() {
		input.close();
	}

	public boolean goToNextStatement() throws InvalidSyntaxException {
		if (input.hasNextLine()) {
			tail = EMPTY_TAIL;
			String statement = getStatement();
			int startPos = indexOfNextNonSpace(statement);
			indent = statement.substring(0, startPos);
			text.set(statement, startPos);
			while (text.endsWith(LINE_CONTINUE, SPACE_MARKERS)) {
				// Ensure line continuation
				text.append(NSISStatements.NEWLINE_MARKER + pullNextLine());
			}
			skipCommentsAtCursor();
			return true;
		}
		return false;
	}

	public String getCurrentStatement() throws InvalidSyntaxException {
		if (text.seekString(COMMENTBLOCK_START)) {
			// Ensure line continuation
			skipCommentBlockAtCursor();
		}
		return text.get();
	}

	public WordTail getWordTail() {
		return tail;
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
		findCurrentWordEnd();

		// Save the word before moving cursor any further
		String word = text.get().substring(start, text.cursorPos());

		// Move cursor to start of next word and collect tail chars
		tail = collectWordTail();

		return new Word(word);
	}

	private String getStatement() throws InvalidSyntaxException {
		String line = pullNextLine();
		if (line.length() > 0
				&& (line.charAt(0) == Parser.UTF16LE_BOM || line.charAt(0) == Parser.UTF16BE_BOM)) {
			// Skip UTF-16 BOM
			line = line.substring(1);
		}
		return line;
	}

	private WordTail collectWordTail() throws InvalidSyntaxException {
		StringBuilder tailPattern = new StringBuilder();
		while (!text.isAtEnd()) {
			skipCommentsAtCursor();
			if (text.charAtCursorIn(TAIL_MARKERS)) {
				// Collect tail characters
				tailPattern.append(text.charAtCursor());
			} else if (!text.charAtCursorIn(WORD_START_MARKERS)) {
				break;
			}
			text.cursorForward(1);
		}
		return new WordTail(tailPattern.toString());
	}

	private void findCurrentWordEnd() throws InvalidSyntaxException {
		if (text.charAtCursorIn(STRING_MARKERS)) {
			// Parse string
			String strOpenChar = Character.toString(text.charAtCursor());
			do {
				text.cursorForward(1);
				if (!text.seekString(strOpenChar)) {
					throw new InvalidSyntaxException("Unterminated string");
				}
			} while (text.cursorPrecededBy(STRING_CHARMASK));
			text.cursorForward(1);
		} else if (text.cursorFollowedBy(LANGSTRING_START)) {
			if (!text.seekString(LANGSTRING_END)) {
				throw new InvalidSyntaxException("Unterminated LangString");
			}
			text.cursorForward(1);
		} else {
			text.seekChars(WORD_END_MARKERS);
			// Continue search if cursor is at "/"
			// without being block comment start "/*"
			while (!text.isAtEnd()
					&& text.charAtCursor() == COMMENTBLOCK_START.charAt(0)
					&& !text.cursorFollowedBy(COMMENTBLOCK_START)) {
				text.cursorForward(1);
				text.seekChars(WORD_END_MARKERS);
			}
		}
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
		text.cursorForward(COMMENTBLOCK_START.length());

		// Pull new lines until comment block end is found
		while (!text.seekString(COMMENTBLOCK_END)) {
			text.goToEnd();
			String nextLine = pullNextLine();
			text.append(NSISStatements.NEWLINE_MARKER + nextLine);
			text.cursorForward(NSISStatements.NEWLINE_MARKER.length()
					+ indexOfNextNonSpace(nextLine));
		}

		// Move cursor beyond comment block
		text.cursorForward(COMMENTBLOCK_END.length());
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
