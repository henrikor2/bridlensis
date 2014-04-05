package bridlensis;

import java.util.Scanner;

public class InputReader {

	public static final String NEW_LINE = "\r\n";

	private static final String TAIL_CHARS = "=+(,)!<>";
	private static final String SEPARATORS = " \t=(,)+!<>;#";
	private static final String BLOCKCOMMENT_END = "*/";
	private static final String BLOCKCOMMENT_START = "/*";
	private static final String BACKSLASH_MASK = "$\\";

	private Scanner input;
	private String text;
	private String indent;
	private StringBuilder tail;
	private int endOfPreviousWord;
	private int startOfNextWord;
	private int linesRead;

	public InputReader(Scanner input) {
		this.input = input;
		linesRead = 0;
	}

	@Override
	public String toString() {
		return text;
	}

	public int getLinesRead() {
		return linesRead;
	}

	public String getIndent() {
		return indent;
	}

	public boolean goToNextStatement() throws InvalidSyntaxException {
		if (input.hasNextLine()) {
			text = nextLine();
			endOfPreviousWord = 0;
			char c;
			while (text.length() > endOfPreviousWord
					&& ((c = text.charAt(endOfPreviousWord)) == ' '
							|| c == '\t' || c == '\uFEFF' || c == '\uFFFE')) {
				endOfPreviousWord++;
			}
			indent = text.substring(0, endOfPreviousWord);
			if (endOfPreviousWord > 0
					&& (indent.charAt(0) == '\uFEFF' || indent.charAt(0) == '\uFFFE')) {
				indent = indent.substring(1);
			}
			tail = new StringBuilder();
			findStartOfNextWord();
			return true;
		}
		return false;
	}

	public String getCurrentStatement() throws InvalidSyntaxException {
		if (!hasNextWord()) {
			return text;
		}

		int commentBlockStart = text.indexOf(BLOCKCOMMENT_START);
		if (commentBlockStart != -1) {
			int commentBlockEnd;
			while ((commentBlockEnd = text.indexOf(BLOCKCOMMENT_END,
					commentBlockStart + 2)) == -1) {
				if (!input.hasNextLine()) {
					startOfNextWord = text.length();
					return text;
				}
				text += NEW_LINE + nextLine();
			}
			return text.substring(0, commentBlockEnd + 2);
		}

		do {
			int lastCharIndex = text.length() - 1;
			char lastChar;
			while ((lastChar = text.charAt(lastCharIndex)) == ' '
					|| lastChar == '\t') {
				lastCharIndex--;
			}
			if (lastChar != '\\') {
				break;
			}
			if (!input.hasNextLine()) {
				startOfNextWord = text.length();
				return text.substring(startOfNextWord);
			}
			text += NEW_LINE + nextLine();
		} while (true);
		return text;
	}

	public boolean hasNextWord() {
		return startOfNextWord < text.length();
	}

	public String nextWord() throws InvalidSyntaxException {
		tail = new StringBuilder();
		char firstChar = text.charAt(startOfNextWord);
		if (firstChar == '"' || firstChar == '\'') {
			return parseStringWord(firstChar);
		}
		endOfPreviousWord = startOfNextWord + 1;
		for (; endOfPreviousWord < text.length(); endOfPreviousWord++) {
			if ((isWordSeparator(endOfPreviousWord) && !isLangStringStart(endOfPreviousWord))
					|| isCommentBlockStart(endOfPreviousWord)) {
				break;
			}
		}
		if (isLangStringStart(startOfNextWord + 1)) {
			// Add missing LangString closing parenthesis
			endOfPreviousWord++;
		}
		String word = text.substring(startOfNextWord, endOfPreviousWord);
		findStartOfNextWord();
		return word;
	}

	private boolean isWordSeparator(int index) {
		return SEPARATORS.indexOf(text.charAt(index)) != -1;
	}

	private boolean isLangStringStart(int index) {
		return index < text.length() && text.charAt(index) == '(' && index > 0
				&& text.charAt(index - 1) == '$';
	}

	private boolean isCommentBlockStart(int index) {
		return text.charAt(index) == '/' && (index + 1) < text.length()
				&& text.substring(index, index + 2).equals(BLOCKCOMMENT_START);
	}

	public String getWordTail() {
		return tail.toString();
	}

	private String parseStringWord(char strOpenChar)
			throws InvalidSyntaxException {
		endOfPreviousWord = goToStringEnd(strOpenChar, startOfNextWord + 1);
		do {
			int lastCharIndex = endOfPreviousWord - 1;
			char lastChar;
			while ((lastChar = text.charAt(lastCharIndex)) == ' '
					|| lastChar == '\t') {
				lastCharIndex--;
			}
			if (lastChar != '\\') {
				break;
			}
			if (!input.hasNextLine()) {
				startOfNextWord = endOfPreviousWord;
				return text.substring(startOfNextWord);
			}
			String nextWord = nextLine().replaceAll("^\\s+", "");
			text = text.substring(0, lastCharIndex) + " " + nextWord;
			endOfPreviousWord = goToStringEnd(strOpenChar, endOfPreviousWord);
		} while (true);
		String word = text.substring(startOfNextWord, endOfPreviousWord);
		findStartOfNextWord();
		return word;
	}

	private int goToStringEnd(char strOpenChar, int startIndex)
			throws InvalidSyntaxException {
		int endIndex = startIndex;
		for (; endIndex < text.length(); endIndex++) {
			char charAtWordEnd = text.charAt(endIndex);
			if ((charAtWordEnd == strOpenChar)
					&& (endIndex < 2 || (!text
							.substring(endIndex - 2, endIndex).equals(
									BACKSLASH_MASK)))) {
				endIndex++;
				return endIndex;
			}
		}
		for (endIndex--; endIndex > startOfNextWord; endIndex--) {
			char charAtWordEnd = text.charAt(endIndex);
			if (charAtWordEnd == ' ' || charAtWordEnd == '\t') {
				continue;
			}
			if (charAtWordEnd == '\\'
					&& !text.substring(endIndex - 2, endIndex).equals(
							BACKSLASH_MASK)) {
				endIndex = pullNextLine();
				return goToStringEnd(strOpenChar, endIndex);
			}
		}
		throw new InvalidSyntaxException("Unexpected EOF");
	}

	private void findStartOfNextWord() throws InvalidSyntaxException {
		startOfNextWord = endOfPreviousWord;
		while (startOfNextWord < text.length()) {
			char c = text.charAt(startOfNextWord);
			if (TAIL_CHARS.indexOf(c) != -1) {
				if (c != '!'
						|| (c == '!' && startOfNextWord + 1 < text.length() && TAIL_CHARS
								.indexOf(text.charAt(startOfNextWord + 1)) != -1)) {
					tail.append(c);
				} else {
					break;
				}
			} else if (c == ';' || c == '#') {
				goToLineCommentEnd();
				startOfNextWord = text.length() - 1;
			} else if (c == '\\'
					&& !text.substring(startOfNextWord - 2, startOfNextWord)
							.equals(BACKSLASH_MASK)) {
				startOfNextWord = pullNextLine() - 1;
			} else if (c == '/'
					&& startOfNextWord <= text.length() - 2
					&& text.substring(startOfNextWord, startOfNextWord + 2)
							.equals(BLOCKCOMMENT_START)) {
				startOfNextWord += 2;
				if (startOfNextWord >= text.length()) {
					startOfNextWord = pullNextLine();
				}
				while ((startOfNextWord = text.indexOf(BLOCKCOMMENT_END,
						startOfNextWord)) == -1) {
					startOfNextWord = pullNextLine();
				}
				startOfNextWord++;
			} else if (c != ' ' && c != '\t') {
				break;
			}
			startOfNextWord++;
		}
	}

	private void goToLineCommentEnd() throws InvalidSyntaxException {
		do {
			int lastCharIndex = text.length() - 1;
			char lastChar = '_';
			while (lastCharIndex > -1
					&& ((lastChar = text.charAt(lastCharIndex)) == ' ' || lastChar == '\t')) {
				lastCharIndex--;
			}
			if (lastChar == '\\') {
				pullNextLine();
			} else {
				break;
			}
		} while (true);
	}

	private int pullNextLine() throws InvalidSyntaxException {
		if (!input.hasNextLine())
			throw new InvalidSyntaxException(
					"Line ends with '\\' but no further lines available");
		int endIndex = text.length();
		StringBuilder sb = new StringBuilder(text.substring(0, endIndex));
		sb.append(NEW_LINE);
		sb.append(nextLine());
		text = sb.toString();
		return endIndex + 2;
	}

	private String nextLine() {
		linesRead++;
		return input.nextLine();
	}

}
