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
		assertEquals("StrCpy", reader.nextWord());
		assertEquals("$0", reader.nextWord());
		assertEquals("\"Hello $\\\"world$\\\"!\"", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("\ta=\"hello world!\"  ");
		assertTrue(reader.goToNextStatement());
		assertEquals("\t", reader.getIndent());
		assertEquals("a", reader.nextWord());
		assertEquals("=", reader.getWordTail());
		assertEquals("\"hello world!\"", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a = ${HELLO}");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("a", reader.nextWord());
		assertEquals("=", reader.getWordTail());
		assertEquals("${HELLO}", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("Function foo()");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("Function", reader.nextWord());
		assertEquals("foo", reader.nextWord());
		assertEquals("()", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("Function foo (a, b)  ; hi there!");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("Function", reader.nextWord());
		assertEquals("foo", reader.nextWord());
		assertEquals("(", reader.getWordTail());
		assertEquals("a", reader.nextWord());
		assertEquals("b", reader.nextWord());
		assertFalse(reader.hasNextWord());
	}

	@Test
	public void testWordTail() throws InvalidSyntaxException {
		InputReader reader = readerFor("a=${HELLO}");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("a", reader.nextWord());
		assertEquals("=", reader.getWordTail());
		assertEquals("${HELLO}", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("DetailPrint foo()");
		assertTrue(reader.goToNextStatement());
		assertEquals("DetailPrint", reader.nextWord());
		assertEquals("foo", reader.nextWord());
		assertEquals("()", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a \\ \r\n = \"\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("a", reader.nextWord());
		assertEquals("=", reader.getWordTail());
		assertEquals("\"\"", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a = \\ \r\n \"\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("a", reader.nextWord());
		assertEquals("=", reader.getWordTail());
		assertEquals("\"\"", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("a = ${HELLO} + \" world!\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getIndent());
		assertEquals("a", reader.nextWord());
		assertTrue(reader.hasNextWord());
		assertEquals("=", reader.getWordTail());
		assertEquals("${HELLO}", reader.nextWord());
		assertTrue(reader.hasNextWord());
		assertEquals("+", reader.getWordTail());
		assertEquals("\" world!\"", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("foo (  ) ; \\o/");
		assertTrue(reader.goToNextStatement());
		assertEquals("foo", reader.nextWord());
		assertEquals("()", reader.getWordTail());
		assertFalse(reader.hasNextWord());

	}

	@Test
	public void testMultiLineClauses() throws InvalidSyntaxException {
		InputReader reader = readerFor("DetailPrint foo(a, \\\r\n    b)\r\nStrCpy $a \\\r\n    $b");
		assertTrue(reader.goToNextStatement());
		assertEquals("DetailPrint", reader.nextWord());
		assertEquals("foo", reader.nextWord());
		assertEquals("(", reader.getWordTail());
		assertTrue(reader.hasNextWord());
		assertEquals("a", reader.nextWord());
		assertEquals(",", reader.getWordTail());
		assertTrue(reader.hasNextWord());
		assertEquals("b", reader.nextWord());
		assertEquals(")", reader.getWordTail());
		assertFalse(reader.hasNextWord());
		assertTrue(reader.goToNextStatement());
		assertTrue(reader.hasNextWord());
		assertEquals("StrCpy", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertTrue(reader.hasNextWord());
		assertEquals("$a", reader.nextWord());
		assertTrue(reader.hasNextWord());
		assertEquals("$b", reader.nextWord());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("DetailPrint \\\r\n\"foo\\\r\n    bar\\\r\n    sanfu! \"");
		assertTrue(reader.goToNextStatement());
		assertTrue(reader.hasNextWord());
		assertEquals("DetailPrint", reader.nextWord());
		assertTrue(reader.hasNextWord());
		assertEquals("\"foo\\\r\n    bar\\\r\n    sanfu! \"", reader.nextWord());
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
		assertEquals("StrCpy", reader.nextWord());
		assertEquals("$0", reader.nextWord());
		assertEquals("1", reader.nextWord());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("a = b;How about this?");
		assertTrue(reader.goToNextStatement());
		assertEquals("a", reader.nextWord());
		assertEquals("b", reader.nextWord());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());

		reader = readerFor("Name/* comment */mysetup");
		assertTrue(reader.goToNextStatement());
		assertEquals("Name", reader.nextWord());
		assertEquals("mysetup", reader.nextWord());
		assertFalse(reader.hasNextWord());

		reader = readerFor("Var a /*\r\nComment\r\nComment\r\n*/\r\nVar b");
		assertTrue(reader.goToNextStatement());
		assertEquals("Var", reader.nextWord());
		assertEquals("a", reader.nextWord());
		assertFalse(reader.hasNextWord());
		assertTrue(reader.goToNextStatement());
		assertEquals("Var", reader.nextWord());
		assertEquals("b", reader.nextWord());
		assertFalse(reader.hasNextWord());
		assertFalse(reader.goToNextStatement());
	}

	@Test
	public void testExpressionsInFunctionCall() throws InvalidSyntaxException {
		InputReader reader;
		reader = readerFor("fa (va +fb( ), fc( vb, fd(vc)) , vd)");
		assertTrue(reader.goToNextStatement());
		assertTrue(reader.hasNextWord());
		assertEquals("fa", reader.nextWord());
		assertEquals("(", reader.getWordTail());
		assertEquals("va", reader.nextWord());
		assertEquals("+", reader.getWordTail());
		assertEquals("fb", reader.nextWord());
		assertEquals("(),", reader.getWordTail());
		assertEquals("fc", reader.nextWord());
		assertEquals("(", reader.getWordTail());
		assertEquals("vb", reader.nextWord());
		assertEquals(",", reader.getWordTail());
		assertEquals("fd", reader.nextWord());
		assertEquals("(", reader.getWordTail());
		assertEquals("vc", reader.nextWord());
		assertEquals(")),", reader.getWordTail());
		assertEquals("vd", reader.nextWord());
		assertEquals(")", reader.getWordTail());
	}

	@Test
	public void testComparisons() throws InvalidSyntaxException {
		InputReader reader;

		reader = readerFor("a == b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord());
		assertEquals("==", reader.getWordTail());
		assertEquals("b", reader.nextWord());

		reader = readerFor("a != b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord());
		assertEquals("!=", reader.getWordTail());
		assertEquals("b", reader.nextWord());

		reader = readerFor("a >= b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord());
		assertEquals(">=", reader.getWordTail());
		assertEquals("b", reader.nextWord());

		reader = readerFor("a <= b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord());
		assertEquals("<=", reader.getWordTail());
		assertEquals("b", reader.nextWord());

		reader = readerFor("a > b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord());
		assertEquals(">", reader.getWordTail());
		assertEquals("b", reader.nextWord());

		reader = readerFor("a < b");
		reader.goToNextStatement();
		assertEquals("a", reader.nextWord());
		assertEquals("<", reader.getWordTail());
		assertEquals("b", reader.nextWord());
	}

	@Test
	public void testNSISNames() throws InvalidSyntaxException {
		InputReader reader;
		reader = readerFor("!include foo.nsh");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail());
		assertEquals("", reader.nextWord());
		assertEquals("!", reader.getWordTail());
		assertEquals("include", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("foo.nsh", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("  !include foo.nsh");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail());
		assertEquals("", reader.nextWord());
		assertEquals("!", reader.getWordTail());
		assertEquals("include", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("foo.nsh", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("SetOutPath ${TEMP}");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail());
		assertEquals("SetOutPath", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("${TEMP}", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("SetOutPath $%TEMP%");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail());
		assertEquals("SetOutPath", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("$%TEMP%", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("LangString TEXT ${LANG_ENGLISH} \"Hello\"");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail());
		assertEquals("LangString", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("TEXT", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("${LANG_ENGLISH}", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("\"Hello\"", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("DetailPrint $(TEXT)");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail());
		assertEquals("DetailPrint", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertEquals("$(TEXT)", reader.nextWord());
		assertEquals("", reader.getWordTail());
		assertFalse(reader.hasNextWord());

		reader = readerFor("DetailPrint($(TEXT))");
		assertTrue(reader.goToNextStatement());
		assertEquals("", reader.getWordTail());
		assertEquals("DetailPrint", reader.nextWord());
		assertEquals("(", reader.getWordTail());
		assertEquals("$(TEXT)", reader.nextWord());
		assertEquals(")", reader.getWordTail());
		assertFalse(reader.hasNextWord());
	}

}
