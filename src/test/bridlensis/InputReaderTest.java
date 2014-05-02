package bridlensis;

import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Test;

import bridlensis.InputReader;
import bridlensis.InvalidSyntaxException;

public class InputReaderTest {

	private InputReader readerFor(String text) {
		return new InputReader(new Scanner(text));
	}

	@Test
	public void testNextWord() throws InvalidSyntaxException {
		InputReader reader;

		reader = readerFor("");
		assertFalse(reader.goToNextStatement());

		reader = readerFor(" ");
		assertTrue(reader.goToNextStatement());
		assertFalse(reader.hasNextWord());

		reader = readerFor("     ");
		assertTrue(reader.goToNextStatement());
		assertEquals("     ", reader.getIndent());
		assertFalse(reader.hasNextWord());

		reader = readerFor("  ; hello world!");
		assertTrue(reader.goToNextStatement());
		assertEquals("  ", reader.getIndent());
		assertFalse(reader.hasNextWord());

		reader = readerFor("  # \"hello world!\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("  ", reader.getIndent());
		assertFalse(reader.hasNextWord());

		reader = readerFor("    StrCpy $0 \"Hello $\\\"world$\\\"!\" ; hello \"world\"!  ");
		assertTrue(reader.goToNextStatement());
		assertEquals("    ", reader.getIndent());
		assertEquals("StrCpy", reader.nextWord().getValue());
		assertEquals("$0", reader.nextWord().getValue());
		assertEquals("\"Hello $\\\"world$\\\"!\"", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("\ta=\"hello world!\"  ");
		assertTrue(reader.goToNextStatement());
		assertEquals("\t", reader.getIndent());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("=", reader.getWordTail().getPattern());
		assertEquals("\"hello world!\"", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a = ${HELLO}");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("=", reader.getWordTail().getPattern());
		assertEquals("${HELLO}", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("Function foo()");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("Function", reader.nextWord().getValue());
		assertEquals("foo", reader.nextWord().getValue());
		assertEquals("()", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("Function foo (a, b)  ; hi there!");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("Function", reader.nextWord().getValue());
		assertEquals("foo", reader.nextWord().getValue());
		assertEquals("(", reader.getWordTail().getPattern());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("b", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());
	}

	@Test
	public void testWordTail() throws InvalidSyntaxException {
		InputReader reader = readerFor("a=${HELLO}");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("=", reader.getWordTail().getPattern());
		assertEquals("${HELLO}", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("DetailPrint foo()");
		assertTrue(reader.goToNextStatement());
		assertEquals("DetailPrint", reader.nextWord().getValue());
		assertEquals("foo", reader.nextWord().getValue());
		assertEquals("()", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a \\ \r\n = \"\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("=", reader.getWordTail().getPattern());
		assertEquals("\"\"", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a = \\ \r\n \"\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("=", reader.getWordTail().getPattern());
		assertEquals("\"\"", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a = ${HELLO} + \" world!\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("a", reader.nextWord().getValue());
		assertTrue(reader.hasNextWord());
		assertEquals("=", reader.getWordTail().getPattern());
		assertEquals("${HELLO}", reader.nextWord().getValue());
		assertTrue(reader.hasNextWord());
		assertEquals("+", reader.getWordTail().getPattern());
		assertEquals("\" world!\"", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("foo (  ) ; \\o/");
		assertTrue(reader.goToNextStatement());
		assertEquals("foo", reader.nextWord().getValue());
		assertEquals("()", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

	}

	@Test
	public void testMultiLineClauses() throws InvalidSyntaxException {
		InputReader reader = readerFor("DetailPrint foo(a, \\\r\n    b)\r\nStrCpy $a \\\r\n    $b");
		assertTrue(reader.goToNextStatement());
		assertEquals("DetailPrint", reader.nextWord().getValue());
		assertEquals("foo", reader.nextWord().getValue());
		assertEquals("(", reader.getWordTail().getPattern());
		assertTrue(reader.hasNextWord());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals(",", reader.getWordTail().getPattern());
		assertTrue(reader.hasNextWord());
		assertEquals("b", reader.nextWord().getValue());
		assertEquals(")", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());
		assertTrue(reader.goToNextStatement());
		assertTrue(reader.hasNextWord());
		assertEquals("StrCpy", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertTrue(reader.hasNextWord());
		assertEquals("$a", reader.nextWord().getValue());
		assertTrue(reader.hasNextWord());
		assertEquals("$b", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("DetailPrint \\\r\n\"foo\\\r\n    bar\\\r\n    sanfu! \"");
		assertTrue(reader.goToNextStatement());
		assertTrue(reader.hasNextWord());
		assertEquals("DetailPrint", reader.nextWord().getValue());
		assertTrue(reader.hasNextWord());
		assertEquals("\"foo\\\r\n    bar\\\r\n    sanfu! \"", reader.nextWord()
				.getValue());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());
	}

	@Test
	public void testCurrentStatement() throws InvalidSyntaxException {
		InputReader reader;

		reader = readerFor("InstallDir \"$PROGRAMFILES\\BridleNSIS Example\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("InstallDir \"$PROGRAMFILES\\BridleNSIS Example\"",
				reader.getCurrentStatement());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("StrCpy $a\\\r\n    $b");
		assertTrue(reader.goToNextStatement());
		assertEquals("StrCpy $a\\\r\n    $b", reader.getCurrentStatement());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("StrCpy $a \\\r\n    $b");
		assertTrue(reader.goToNextStatement());
		assertEquals("StrCpy $a \\\r\n    $b", reader.getCurrentStatement());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("DetailPrint \" \\\r\n    in-string stuff\\\r\n    \" ; eos");
		assertTrue(reader.goToNextStatement());
		assertEquals(
				"DetailPrint \" \\\r\n    in-string stuff\\\r\n    \" ; eos",
				reader.getCurrentStatement());

		reader = readerFor("Var a /*\r\nComment\r\nComment\r\n*/\r\nVar b");
		assertTrue(reader.goToNextStatement());
		assertEquals("Var a /*\r\nComment\r\nComment\r\n*/",
				reader.getCurrentStatement());
		assertTrue(reader.goToNextStatement());
		assertEquals("Var b", reader.getCurrentStatement());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("StrCpy \\\r\n    $a \\\r\n    $b ; <-- \"hello world\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("StrCpy \\\r\n    $a \\\r\n    $b ; <-- \"hello world\"",
				reader.getCurrentStatement());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("DetailPrint \\\r\n    (a \\\r\n    + \" hello\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("DetailPrint \\\r\n    (a \\\r\n    + \" hello\"",
				reader.getCurrentStatement());
		assertFalse(reader.goToNextStatement());
	}

	@Test
	public void testComments() throws InvalidSyntaxException {
		InputReader reader;

		reader = readerFor("StrCpy $0 1 # Comment \\\r\n    Another comment line (see `Long commands` section below)");
		assertTrue(reader.goToNextStatement());
		assertEquals("StrCpy", reader.nextWord().getValue());
		assertEquals("$0", reader.nextWord().getValue());
		assertEquals("1", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("a = b;How about this?");
		assertTrue(reader.goToNextStatement());
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("b", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("Name/* comment */mysetup");
		assertTrue(reader.goToNextStatement());
		assertEquals("Name", reader.nextWord().getValue());
		assertEquals("mysetup", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());

		reader = readerFor("Var a /*\r\nComment\r\nComment\r\n*/\r\nVar b");
		assertTrue(reader.goToNextStatement());
		assertEquals("Var", reader.nextWord().getValue());
		assertEquals("a", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());
		assertTrue(reader.goToNextStatement());
		assertEquals("Var", reader.nextWord().getValue());
		assertEquals("b", reader.nextWord().getValue());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());
	}

	@Test
	public void testExpressionsInFunctionCall() throws InvalidSyntaxException {
		InputReader reader;
		reader = readerFor("fa (va +fb( ), fc( vb, fd(vc)) , vd)");
		assertTrue(reader.goToNextStatement());
		assertTrue(reader.hasNextWord());
		assertEquals("fa", reader.nextWord().getValue());
		assertEquals("(", reader.getWordTail().getPattern());
		assertEquals("va", reader.nextWord().getValue());
		assertEquals("+", reader.getWordTail().getPattern());
		assertEquals("fb", reader.nextWord().getValue());
		assertEquals("(),", reader.getWordTail().getPattern());
		assertEquals("fc", reader.nextWord().getValue());
		assertEquals("(", reader.getWordTail().getPattern());
		assertEquals("vb", reader.nextWord().getValue());
		assertEquals(",", reader.getWordTail().getPattern());
		assertEquals("fd", reader.nextWord().getValue());
		assertEquals("(", reader.getWordTail().getPattern());
		assertEquals("vc", reader.nextWord().getValue());
		assertEquals(")),", reader.getWordTail().getPattern());
		assertEquals("vd", reader.nextWord().getValue());
		assertEquals(")", reader.getWordTail().getPattern());
	}

	@Test
	public void testComparisons() throws InvalidSyntaxException {
		InputReader reader;

		reader = readerFor("a == b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("==", reader.getWordTail().getPattern());
		assertEquals("b", reader.nextWord().getValue());

		reader = readerFor("a != b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("!=", reader.getWordTail().getPattern());
		assertEquals("b", reader.nextWord().getValue());

		reader = readerFor("a >= b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord().getValue());
		assertEquals(">=", reader.getWordTail().getPattern());
		assertEquals("b", reader.nextWord().getValue());

		reader = readerFor("a <= b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("<=", reader.getWordTail().getPattern());
		assertEquals("b", reader.nextWord().getValue());

		reader = readerFor("a > b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord().getValue());
		assertEquals(">", reader.getWordTail().getPattern());
		assertEquals("b", reader.nextWord().getValue());

		reader = readerFor("a < b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord().getValue());
		assertEquals("<", reader.getWordTail().getPattern());
		assertEquals("b", reader.nextWord().getValue());
	}

	@Test
	public void testNSISNames() throws InvalidSyntaxException {
		InputReader reader;
		reader = readerFor("!include foo.nsh");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("", reader.nextWord().getValue());
		assertEquals("!", reader.getWordTail().getPattern());
		assertEquals("include", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("foo.nsh", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("  !include foo.nsh");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("", reader.nextWord().getValue());
		assertEquals("!", reader.getWordTail().getPattern());
		assertEquals("include", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("foo.nsh", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("SetOutPath ${TEMP}");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("SetOutPath", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("${TEMP}", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("SetOutPath $%TEMP%");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("SetOutPath", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("$%TEMP%", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("LangString TEXT ${LANG_ENGLISH} \"Hello\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("LangString", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("TEXT", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("${LANG_ENGLISH}", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("\"Hello\"", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("DetailPrint $(TEXT)");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("DetailPrint", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("$(TEXT)", reader.nextWord().getValue());
		assertEquals("", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());

		reader = readerFor("DetailPrint($(TEXT))");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail().getPattern());
		assertEquals("DetailPrint", reader.nextWord().getValue());
		assertEquals("(", reader.getWordTail().getPattern());
		assertEquals("$(TEXT)", reader.nextWord().getValue());
		assertEquals(")", reader.getWordTail().getPattern());
		assertFalse(reader.hasNextWord());
	}

}
