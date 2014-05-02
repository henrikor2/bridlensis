package bridlensis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Scanner;

import org.junit.Test;

import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.SimpleNameGenerator;

public class ParserTest {

	private InputReader readerFor(String string) throws InvalidSyntaxException {
		InputReader reader = new InputReader(new Scanner(string));
		reader.goToNextStatement();
		return reader;
	}

	private Parser createParser() {
		Environment environment = new Environment(new SimpleNameGenerator());
		environment.loadBuiltinVariables();
		environment.loadBuiltinFunctions();
		Parser parser = new Parser(environment, null, null, null, null);
		return parser;
	}

	@Test
	public void testParseExpression() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		InputReader reader;
		StringBuilder buffer;
		StringBuilder expected;

		parser.parseStatement(readerFor("Var a"));
		parser.parseStatement(readerFor("Var b"));
		parser.parseStatement(readerFor("Var c"));

		reader = readerFor("a + b + c");
		assertEquals("\"$a$b$c\"",
				parser.parseExpression(reader.nextWord(), null, reader)
						.getValue());

		reader = readerFor("a + b + c");
		assertEquals("\"$a$b$c\"",
				parser.parseExpression(reader.nextWord(), null, reader)
						.getValue());

		reader = readerFor("a + \"b\" + c");
		assertEquals("\"$ab$c\"",
				parser.parseExpression(reader.nextWord(), null, reader)
						.getValue());

		reader = readerFor("\"a + b\" + c");
		assertEquals("\"a + b$c\"",
				parser.parseExpression(reader.nextWord(), null, reader)
						.getValue());

		parser.parseStatement(readerFor("FUnction foo(a)"));
		try {
			buffer = new StringBuilder();
			reader = readerFor("a + foo(b)");
			parser.parseExpression(reader.nextWord(), buffer, reader);
			fail();
		} catch (InvalidSyntaxException e) {
			// All good
			System.err.println(e.getMessage());
		}
		parser.parseStatement(readerFor("  Var b"));
		parser.parseStatement(readerFor("  Return a"));

		buffer = new StringBuilder();
		reader = readerFor("foo(b)");
		assertEquals("$foo.s02", // readerFor("a + foo(b)") eat one s0
				parser.parseExpression(reader.nextWord(), buffer, reader)
						.getValue());
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.s02\r\n");
		expected.append("Push $foo.b\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $foo.s02\r\n");
		assertEquals(expected.toString(), buffer.toString());

		buffer = new StringBuilder();
		reader = readerFor("a + foo(1)");
		assertEquals("\"$foo.a$foo.s03\"",
				parser.parseExpression(reader.nextWord(), buffer, reader)
						.getValue());
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.s03\r\n");
		expected.append("Push 1\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $foo.s03\r\n");
		assertEquals(expected.toString(), buffer.toString());

		buffer = new StringBuilder();
		reader = readerFor("foo(1) + a");
		assertEquals("\"$foo.s04$foo.a\"",
				parser.parseExpression(reader.nextWord(), buffer, reader)
						.getValue());
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.s04\r\n");
		expected.append("Push 1\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $foo.s04\r\n");
		assertEquals(expected.toString(), buffer.toString());

		parser.parseStatement(readerFor("FunctionEnd"));

		buffer = new StringBuilder();
		reader = readerFor("foo(\"hello\") + \" \" + foo(\"world!\")");
		assertEquals("\"$s05 $s06\"",
				parser.parseExpression(reader.nextWord(), buffer, reader)
						.getValue());
		expected = new StringBuilder();
		expected.append("Var /GLOBAL s05\r\n");
		expected.append("Push \"hello\"\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $s05\r\n");
		expected.append("Var /GLOBAL s06\r\n");
		expected.append("Push \"world!\"\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $s06\r\n");
		assertEquals(expected.toString(), buffer.toString());

	}

	@Test
	public void testPlainNSIS() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("    ");
		expected = new StringBuilder();
		expected.append("    ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("\t");
		expected = new StringBuilder();
		expected.append("\t");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement
				.append("InstallDir \"$PROGRAMFILES\\BridleNSIS Example\"");
		expected = new StringBuilder();
		expected.append("InstallDir \"$PROGRAMFILES\\BridleNSIS Example\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement
				.append("\t\t  DeleteRegKey HKLM SOFTWARE\\BridleNSIS_Example");
		expected = new StringBuilder();
		expected.append("\t\t  DeleteRegKey HKLM SOFTWARE\\BridleNSIS_Example");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Delete $INSTDIR\\bridlensis.nsi");
		expected = new StringBuilder();
		expected.append("Delete $INSTDIR\\bridlensis.nsi");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testVarAssign() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("a = \"Hello\" ; wow!");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a\r\n");
		expected.append("StrCpy $a \"Hello\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("B = a#it is \"same\"");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL b\r\n");
		expected.append("StrCpy $b $a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("a=\"wo$\\\"rl$\\\"d!'\"");
		expected = new StringBuilder();
		expected.append("StrCpy $a \"wo$\\\"rl$\\\"d!'\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("INSTDIR = \"C:\\BridleNSIS\"");
		expected = new StringBuilder();
		expected.append("StrCpy $instdir \"C:\\BridleNSIS\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("INSTDIR = a");
		expected = new StringBuilder();
		expected.append("StrCpy $instdir $a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		parser.parseStatement(readerFor("Function foo()"));

		inputStatement = new StringBuilder();
		inputStatement.append("  a = $%TEMP%");
		expected = new StringBuilder();
		expected.append("  Var /GLOBAL foo.a\r\n");
		expected.append("  StrCpy $foo.a $%TEMP%");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("  global.instdir = a");
		expected = new StringBuilder();
		expected.append("  StrCpy $instdir $foo.a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("  global.a = a");
		expected = new StringBuilder();
		expected.append("  StrCpy $a $foo.a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

	}

	@Test
	public void testVarDeclare() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("Var A");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("B = A");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL b\r\n");
		expected.append("StrCpy $b $a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testFunctionBegin() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("Function OldSchool");
		expected = new StringBuilder();
		expected.append("Function oldschool");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		try {
			parser.parseStatement(readerFor("Function Another ; Without FunctionEnd!"));
			fail();
		} catch (InvalidSyntaxException e) {
			// All good!
			System.err.println(e.getMessage());
		}

		inputStatement = new StringBuilder();
		inputStatement.append("functionend");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append(" Function foo() ; Yoyo mama!");
		expected = new StringBuilder();
		expected.append(" Function foo");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append(" FunctionEnd");
		expected = new StringBuilder();
		expected.append(" FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		try {
			parser.parseStatement(readerFor("Function OldSchool() ; Yet again!"));
			fail();
		} catch (EnvironmentException e) {
			// All good!
			System.err.println(e.getMessage());
		}

		inputStatement = new StringBuilder();
		inputStatement.append("Function bar(a, b)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL bar.a\r\n");
		expected.append("Var /GLOBAL bar.b\r\n");
		expected.append("Function bar\r\n");
		expected.append("    Pop $bar.a\r\n");
		expected.append("    Pop $bar.b");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testCallable() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		// Function OldSchool define
		inputStatement = new StringBuilder();
		inputStatement.append("Function OldSchool");
		expected = new StringBuilder();
		expected.append("Function oldschool");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		// Function OldSchool call
		inputStatement = new StringBuilder();
		inputStatement.append("OldSchool() ; BridleNSIS style function call");
		expected = new StringBuilder();
		expected.append("Call oldschool");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		// Function foo define
		inputStatement = new StringBuilder();
		inputStatement.append("Function foo(a, b)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.a\r\n");
		expected.append("Var /GLOBAL foo.b\r\n");
		expected.append("Function foo\r\n");
		expected.append("    Pop $foo.a\r\n");
		expected.append("    Pop $foo.b");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    a = b");
		expected = new StringBuilder();
		expected.append("    StrCpy $foo.a $foo.b");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    c = a");
		expected = new StringBuilder();
		expected.append("    Var /GLOBAL foo.c\r\n");
		expected.append("    StrCpy $foo.c $foo.a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    DetailPrint(global.R0)");
		expected = new StringBuilder();
		expected.append("    DetailPrint $r0 ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    DetailPrint(\"$c\")");
		expected = new StringBuilder();
		expected.append("    DetailPrint \"$c\" ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    DetailPrint(c)");
		expected = new StringBuilder();
		expected.append("    DetailPrint $foo.c ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    global.R0 = c");
		expected = new StringBuilder();
		expected.append("    StrCpy $r0 $foo.c");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    foo = c");
		expected = new StringBuilder();
		expected.append("    Var /GLOBAL foo.foo\r\n");
		expected.append("    StrCpy $foo.foo $foo.c");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		try {
			parser.parseStatement(readerFor("    r0 = f"));
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
			// We're good
		}

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		// Function foo call

		inputStatement = new StringBuilder();
		inputStatement.append("Var a");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("foo(a, \"hello world!\")");
		expected = new StringBuilder();
		expected.append("Push \"hello world!\"\r\n");
		expected.append("Push $a\r\n");
		expected.append("Call foo");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		try {
			parser.parseStatement(readerFor("foo()"));
			fail();
		} catch (InvalidSyntaxException e) {
			// all good
			System.err.println(e.getMessage());
		}

		try {
			parser.parseStatement(readerFor("foo(1, 2, 3)"));
			fail();
		} catch (InvalidSyntaxException e) {
			// all good
			System.err.println(e.getMessage());
		}

		try {
			parser.parseStatement(readerFor("bar()"));
			fail();
		} catch (EnvironmentException e) {
			// it's ok baby
			System.err.println(e.getMessage());
		}

		try {
			parser.parseStatement(readerFor("Function foo(foo)"));
			fail();
		} catch (EnvironmentException e) {
			// all good
			System.err.println(e.getMessage());
		}

		try {
			parser.parseStatement(readerFor("Function DetailPrint(text)"));
			fail();
		} catch (EnvironmentException e) {
			// it's ok baby
			System.err.println(e.getMessage());
		}
	}

	@Test
	public void testFunctionReturn() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("Function foo(a)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.a\r\n");
		expected.append("Function foo\r\n");
		expected.append("    Pop $foo.a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("  Return a");
		expected = new StringBuilder();
		expected.append("  Push $foo.a\r\n");
		expected.append("  Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		// Empty stack after function call
		inputStatement = new StringBuilder();
		inputStatement.append("foo(1)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL bridlensis_nullvar\r\n");
		expected.append("Push 1\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $bridlensis_nullvar");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		// Reuse null variable to dump stack to
		inputStatement = new StringBuilder();
		inputStatement.append("foo(R1)");
		expected = new StringBuilder();
		expected.append("Push $r1\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $bridlensis_nullvar");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("a = foo(r1)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a\r\n");
		expected.append("Push $r1\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Function bar()");
		expected = new StringBuilder();
		expected.append("Function bar");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("  Return \"\"");
		expected = new StringBuilder();
		expected.append("  Push \"\"\r\n");
		expected.append("  Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Function empty()");
		expected = new StringBuilder();
		expected.append("Function empty");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("  Return");
		expected = new StringBuilder();
		expected.append("  Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testFunctionAssign() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("Function foo(a)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.a\r\n");
		expected.append("Function foo\r\n");
		expected.append("    Pop $foo.a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    Return a");
		expected = new StringBuilder();
		expected.append("    Push $foo.a\r\n");
		expected.append("    Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("a = foo(\"1\")");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a\r\n");
		expected.append("Push \"1\"\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement
				.append("a = StrCpy(\"$a world$R0\", \"\", \"\") ; <-- \"hello world!\"");
		expected = new StringBuilder();
		expected.append("StrCpy $a \"$a world$R0\" \"\" \"\" ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("a = StrCpy(\"foobar\")");
		expected = new StringBuilder();
		expected.append("StrCpy $a \"foobar\" ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("b = IntOp(a, \"+\", 1)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL b\r\n");
		expected.append("IntOp $b $a \"+\" 1 ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("c = GetTempFileName($%TEMP%) + b + foo(1) + a");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL c\r\n");
		expected.append("GetTempFileName $c $%TEMP% \r\n");
		expected.append("Var /GLOBAL s01\r\n");
		expected.append("Push 1\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $s01\r\n");
		expected.append("StrCpy $c \"$c$b$s01$a\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testFunctionCallInsideFunction() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("Function join(a, b)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL join.a\r\n");
		expected.append("Var /GLOBAL join.b\r\n");
		expected.append("Function join\r\n");
		expected.append("    Pop $join.a\r\n");
		expected.append("    Pop $join.b");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("  Return a + b");
		expected = new StringBuilder();
		expected.append("  Push \"$join.a$join.b\"\r\n");
		expected.append("  Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Function len(a, b)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL len.a\r\n");
		expected.append("Var /GLOBAL len.b\r\n");
		expected.append("Function len\r\n");
		expected.append("    Pop $len.a\r\n");
		expected.append("    Pop $len.b");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("  Return StrLen(join(a, b))");
		expected = new StringBuilder();
		expected.append("  Var /GLOBAL len.s01\r\n");
		expected.append("  Var /GLOBAL len.s02\r\n");
		expected.append("  Push $len.b\r\n");
		expected.append("  Push $len.a\r\n");
		expected.append("  Call join\r\n");
		expected.append("  Pop $len.s02\r\n");
		expected.append("  StrLen $len.s01 $len.s02 \r\n");
		expected.append("  Push $len.s01\r\n");
		expected.append("  Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testInstructions() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("ExecShell(\"open\", \"http://nsis.sf.net/\")");
		expected = new StringBuilder();
		expected.append("ExecShell \"open\" \"http://nsis.sf.net/\" ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement
				.append("ExecShell(\"open\", \"http://nsis.sf.net/\", \"SW_SHOWNORMAL\")");
		expected = new StringBuilder();
		expected.append("ExecShell \"open\" \"http://nsis.sf.net/\" \"SW_SHOWNORMAL\" ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement
				.append("ExecShell(\"open\", \\\r\n    \"http://nsis.sf.net/\")");
		expected = new StringBuilder();
		expected.append("ExecShell \"open\" \"http://nsis.sf.net/\" ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement
				.append("ret = ExecWait('\"$INSTDIR\\someprogram.exe\"')");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL ret\r\n");
		expected.append("ExecWait '\"$INSTDIR\\someprogram.exe\"' $ret ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("r0 = StrCpy(\"hello world!\", 5)");
		expected = new StringBuilder();
		expected.append("StrCpy $r0 \"hello world!\" 5 ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement
				.append("R1 = FileOpen(\"C:\\temp\\makensis.log\", \"r\")");
		expected = new StringBuilder();
		expected.append("FileOpen $r1 \"C:\\temp\\makensis.log\" \"r\" ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testSringConcatenation() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("a = \"hello\"");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a\r\n");
		expected.append("StrCpy $a \"hello\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("a = a + \" world\" + \", oh my!\"");
		expected = new StringBuilder();
		expected.append("StrCpy $a \"$a world, oh my!\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Function foo(a)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.a\r\n");
		expected.append("Function foo\r\n");
		expected.append("    Pop $foo.a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("    Return a + \" times\"");
		expected = new StringBuilder();
		expected.append("    Push \"$foo.a times\"\r\n");
		expected.append("    Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("a = \"foo: \" + foo(1)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL s01\r\n");
		expected.append("Push 1\r\n");
		expected.append("Call foo\r\n");
		expected.append("Pop $s01\r\n");
		expected.append("StrCpy $a \"foo: $s01\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("a = \"hello\" + ${HELLO}");
		expected = new StringBuilder();
		expected.append("StrCpy $a \"hello${HELLO}\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testVariableScope() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("Var a");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Function foo(a)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.a\r\n");
		expected.append("Function foo\r\n");
		expected.append("    Pop $foo.a");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("DetailPrint(a)");
		expected = new StringBuilder();
		expected.append("DetailPrint $foo.a ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("DetailPrint(global.a)");
		expected = new StringBuilder();
		expected.append("DetailPrint $a ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("DetailPrint(global.r0)");
		expected = new StringBuilder();
		expected.append("DetailPrint $r0 ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("FunctionEnd");
		expected = new StringBuilder();
		expected.append("FunctionEnd");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		try {
			parser.parseStatement(readerFor("Var foo.a"));
			fail();
		} catch (EnvironmentException e) {
			// all good
			System.err.println(e.getMessage());
		}
	}

	@Test
	public void testBlockCommentsInStatement() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		inputStatement = new StringBuilder();
		inputStatement.append("a = \"hello... \" /*world?*/ + \"world!\"");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL a\r\n");
		expected.append("StrCpy $a \"hello... world!\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Function foo( /*a?*/ a, /*b?*/ b)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL foo.a\r\n");
		expected.append("Var /GLOBAL foo.b\r\n");
		expected.append("Function foo\r\n");
		expected.append("    Pop $foo.a\r\n");
		expected.append("    Pop $foo.b");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("DetailPrint(/*a?*/a/*a?*/)");
		expected = new StringBuilder();
		expected.append("DetailPrint $foo.a ");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Return /*a?*/a/*a?*/");
		expected = new StringBuilder();
		expected.append("Push $foo.a\r\n");
		expected.append("Return");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testIf() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		// Something to refer to in test cases
		parser.parseStatement(readerFor("world = \"world\""));
		parser.parseStatement(readerFor("function hello()"));
		parser.parseStatement(readerFor("  return \"hello \""));
		parser.parseStatement(readerFor("functionend"));

		inputStatement = new StringBuilder();
		inputStatement.append("If world == \"hello world\"");
		expected = new StringBuilder();
		expected.append("${If} $world == \"hello world\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("If not \"${FileExists}\" \"$instdir\\foo.txt\"");
		expected = new StringBuilder();
		expected.append("${IfNot} \"${FileExists}\" \"$instdir\\foo.txt\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("If \"hello \" + world == \"hello world\"");
		expected = new StringBuilder();
		expected.append("${If} \"hello $world\" == \"hello world\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("If world == \"world\" \\\r\n");
		inputStatement.append("    And hello() != \"hello \"");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL s01\r\n");
		expected.append("Call hello\r\n");
		expected.append("Pop $s01\r\n");
		expected.append("${If} $world == \"world\"\r\n");
		expected.append("${AndIf} $s01 != \"hello \"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("ElseIf world != \"hello world\"");
		expected = new StringBuilder();
		expected.append("${ElseIf} $world != \"hello world\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("ElseIf world > 1 and \"${Errors}\"");
		expected = new StringBuilder();
		expected.append("${ElseIf} $world > 1\r\n");
		expected.append("${andIf} \"${Errors}\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("else");
		expected = new StringBuilder();
		expected.append("${Else}");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("EndIf");
		expected = new StringBuilder();
		expected.append("${EndIf}");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));
	}

	@Test
	public void testDo() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();
		StringBuilder inputStatement;
		StringBuilder expected;

		// Something to refer to in test cases
		parser.parseStatement(readerFor("a = 1"));
		parser.parseStatement(readerFor("Function Inc(i)"));
		parser.parseStatement(readerFor("  Return IntOp(i, \"+\", 1)"));
		parser.parseStatement(readerFor("FunctionEnd"));

		inputStatement = new StringBuilder();
		inputStatement.append("do");
		expected = new StringBuilder();
		expected.append("${Do}");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Do Until a < 10");
		expected = new StringBuilder();
		expected.append("${DoUntil} $a < 10");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Do While ${FileExists} \"$instdir\\foo.txt\"");
		expected = new StringBuilder();
		expected.append("${DoWhile} ${FileExists} \"$instdir\\foo.txt\"");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Continue");
		expected = new StringBuilder();
		expected.append("${Continue}");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Break");
		expected = new StringBuilder();
		expected.append("${Break}");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Loop");
		expected = new StringBuilder();
		expected.append("${Loop}");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		inputStatement = new StringBuilder();
		inputStatement.append("Loop While ${Errors}");
		expected = new StringBuilder();
		expected.append("${LoopWhile} ${Errors}");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

		// Function call in comparison statement
		inputStatement = new StringBuilder();
		inputStatement.append("Loop Until a > Inc(a)");
		expected = new StringBuilder();
		expected.append("Var /GLOBAL s02\r\n");
		expected.append("Push $a\r\n");
		expected.append("Call inc\r\n");
		expected.append("Pop $s02\r\n");
		expected.append("${LoopUntil} $a > $s02");
		assertEquals(expected.toString(),
				parser.parseStatement(readerFor(inputStatement.toString())));

	}

}
