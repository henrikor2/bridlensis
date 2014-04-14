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
	public void testGetExpression() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		InputReader reader;
		StringBuilder buffer;

		parser.parseStatement(readerFor("Var a"));
		parser.parseStatement(readerFor("Var b"));
		parser.parseStatement(readerFor("Var c"));

		reader = readerFor("a + b + c");
		assertEquals("\"$a$b$c\"",
				parser.getExpression(reader.nextWord(), null, reader));

		reader = readerFor("a + b + c");
		assertEquals("\"$a$b$c\"",
				parser.getExpression(reader.nextWord(), null, reader));

		reader = readerFor("a + \"b\" + c");
		assertEquals("\"$ab$c\"",
				parser.getExpression(reader.nextWord(), null, reader));

		reader = readerFor("\"a + b\" + c");
		assertEquals("\"a + b$c\"",
				parser.getExpression(reader.nextWord(), null, reader));

		parser.parseStatement(readerFor("FUnction foo(a)"));
		try {
			buffer = new StringBuilder();
			reader = readerFor("a + foo(b)");
			parser.getExpression(reader.nextWord(), buffer, reader);
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
				parser.getExpression(reader.nextWord(), buffer, reader));
		assertEquals(
				"Var /GLOBAL foo.s02\r\nPush $foo.b\r\nCall foo\r\nPop $foo.s02\r\n",
				buffer.toString());

		buffer = new StringBuilder();
		reader = readerFor("a + foo(1)");
		assertEquals("\"$foo.a$foo.s03\"",
				parser.getExpression(reader.nextWord(), buffer, reader));
		assertEquals(
				"Var /GLOBAL foo.s03\r\nPush 1\r\nCall foo\r\nPop $foo.s03\r\n",
				buffer.toString());

		buffer = new StringBuilder();
		reader = readerFor("foo(1) + a");
		assertEquals("\"$foo.s04$foo.a\"",
				parser.getExpression(reader.nextWord(), buffer, reader));
		assertEquals(
				"Var /GLOBAL foo.s04\r\nPush 1\r\nCall foo\r\nPop $foo.s04\r\n",
				buffer.toString());

		parser.parseStatement(readerFor("FunctionEnd"));

		buffer = new StringBuilder();
		reader = readerFor("foo(\"hello\") + \" \" + foo(\"world!\")");
		assertEquals("\"$s05 $s06\"",
				parser.getExpression(reader.nextWord(), buffer, reader));
		assertEquals(
				"Var /GLOBAL s05\r\nPush \"hello\"\r\nCall foo\r\nPop $s05\r\nVar /GLOBAL s06\r\nPush \"world!\"\r\nCall foo\r\nPop $s06\r\n",
				buffer.toString());

	}

	@Test
	public void testPlainNSIS() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();

		assertEquals("    ", parser.parseStatement(readerFor("    ")));

		assertEquals("\t", parser.parseStatement(readerFor("\t")));

		assertEquals(
				"InstallDir \"$PROGRAMFILES\\BridleNSIS Example\"",
				parser.parseStatement(readerFor("InstallDir \"$PROGRAMFILES\\BridleNSIS Example\"")));

		assertEquals(
				"\t\t  DeleteRegKey HKLM SOFTWARE\\BridleNSIS_Example",
				parser.parseStatement(readerFor("\t\t  DeleteRegKey HKLM SOFTWARE\\BridleNSIS_Example")));

		assertEquals("Delete $INSTDIR\\bridlensis.nsi",
				parser.parseStatement(readerFor("Delete $INSTDIR\\bridlensis.nsi")));
	}

	@Test
	public void testVarAssign() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();

		assertEquals("Var /GLOBAL a\r\nStrCpy $a \"Hello\"",
				parser.parseStatement(readerFor("a = \"Hello\" ; wow!")));

		assertEquals("Var /GLOBAL b\r\nStrCpy $b $a",
				parser.parseStatement(readerFor("B = a#it is \"same\"")));

		assertEquals("StrCpy $a \"wo$\\\"rl$\\\"d!'\"",
				parser.parseStatement(readerFor("a=\"wo$\\\"rl$\\\"d!'\"")));

		assertEquals("StrCpy $instdir \"C:\\BridleNSIS\"",
				parser.parseStatement(readerFor("INSTDIR = \"C:\\BridleNSIS\"")));

		assertEquals("StrCpy $instdir $a",
				parser.parseStatement(readerFor("INSTDIR = a")));

		parser.parseStatement(readerFor("Function foo()"));
		assertEquals("  Var /GLOBAL foo.a\r\n  StrCpy $foo.a $%TEMP%",
				parser.parseStatement(readerFor("  a = $%TEMP%")));
		assertEquals("  StrCpy $instdir $foo.a",
				parser.parseStatement(readerFor("  global.instdir = a")));
		assertEquals("  StrCpy $a $foo.a",
				parser.parseStatement(readerFor("  global.a = a")));

	}

	@Test
	public void testVarDeclare() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		assertEquals("Var /GLOBAL a", parser.parseStatement(readerFor("Var A")));
		assertEquals("Var /GLOBAL b\r\nStrCpy $b $a",
				parser.parseStatement(readerFor("B = A")));
	}

	@Test
	public void testFunctionBegin() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();

		assertEquals("Function oldschool",
				parser.parseStatement(readerFor("Function OldSchool")));

		try {
			parser.parseStatement(readerFor("Function Another ; Without FunctionEnd!"));
			fail();
		} catch (InvalidSyntaxException e) {
			// All good!
			System.err.println(e.getMessage());
		}

		assertEquals("FunctionEnd", parser.parseStatement(readerFor("functionend")));

		assertEquals(" Function foo",
				parser.parseStatement(readerFor(" Function foo() ; Yoyou mama!")));
		assertEquals(" FunctionEnd", parser.parseStatement(readerFor(" FunctionEnd")));

		try {
			parser.parseStatement(readerFor("Function OldSchool() ; Yet again!"));
			fail();
		} catch (EnvironmentException e) {
			// All good!
			System.err.println(e.getMessage());
		}

		assertEquals(
				"Var /GLOBAL bar.a\r\nVar /GLOBAL bar.b\r\nFunction bar\r\n    Pop $bar.a\r\n    Pop $bar.b",
				parser.parseStatement(readerFor("Function bar(a, b)")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));
	}

	@Test
	public void testCallable() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();

		// Function OldSchool define
		assertEquals("Function oldschool",
				parser.parseStatement(readerFor("Function OldSchool")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

		// Function OldSchool call
		assertEquals(
				"Call oldschool",
				parser.parseStatement(readerFor("OldSchool() ; BridleNSIS style function call")));

		// Function foo define
		assertEquals(
				"Var /GLOBAL foo.a\r\nVar /GLOBAL foo.b\r\nFunction foo\r\n    Pop $foo.a\r\n    Pop $foo.b",
				parser.parseStatement(readerFor("Function foo(a, b)")));
		assertEquals("    StrCpy $foo.a $foo.b",
				parser.parseStatement(readerFor("    a = b")));
		assertEquals("    Var /GLOBAL foo.c\r\n    StrCpy $foo.c $foo.a",
				parser.parseStatement(readerFor("    c = a")));
		assertEquals("    DetailPrint $r0 ",
				parser.parseStatement(readerFor("    DetailPrint(global.R0)")));
		assertEquals("    DetailPrint \"$c\" ",
				parser.parseStatement(readerFor("    DetailPrint(\"$c\")")));
		assertEquals("    DetailPrint $foo.c ",
				parser.parseStatement(readerFor("    DetailPrint(c)")));
		assertEquals("    StrCpy $r0 $foo.c",
				parser.parseStatement(readerFor("    global.R0 = c")));
		assertEquals("    Var /GLOBAL foo.foo\r\n    StrCpy $foo.foo $foo.c",
				parser.parseStatement(readerFor("    foo = c")));

		try {
			parser.parseStatement(readerFor("    r0 = f"));
			fail();
		} catch (EnvironmentException e) {
			System.err.println(e.getMessage());
			// We're good
		}

		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

		// Function foo call
		assertEquals("Var /GLOBAL a", parser.parseStatement(readerFor("Var a")));
		assertEquals("Push \"hello world!\"\r\nPush $a\r\nCall foo",
				parser.parseStatement(readerFor("foo(a, \"hello world!\")")));

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

		assertEquals("Var /GLOBAL foo.a\r\nFunction foo\r\n    Pop $foo.a",
				parser.parseStatement(readerFor("Function foo(a)")));
		assertEquals("  Push $foo.a\r\n  Return",
				parser.parseStatement(readerFor("  Return a")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

		// Empty stack after function call
		assertEquals(
				"Var /GLOBAL bridlensis_nullvar\r\nPush 1\r\nCall foo\r\nPop $bridlensis_nullvar",
				parser.parseStatement(readerFor("foo(1)")));

		// Reuse null variable to dump stack to
		assertEquals("Push $r1\r\nCall foo\r\nPop $bridlensis_nullvar",
				parser.parseStatement(readerFor("foo(R1)")));

		assertEquals("Var /GLOBAL a\r\nPush $r1\r\nCall foo\r\nPop $a",
				parser.parseStatement(readerFor("a = foo(r1)")));

		assertEquals("Function bar", parser.parseStatement(readerFor("Function bar()")));
		assertEquals("  Push \"\"\r\n  Return",
				parser.parseStatement(readerFor("  Return \"\"")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

		assertEquals("Function empty",
				parser.parseStatement(readerFor("Function empty()")));
		assertEquals("  Return", parser.parseStatement(readerFor("  Return")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));
	}

	@Test
	public void testFunctionAssign() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();

		assertEquals("Var /GLOBAL foo.a\r\nFunction foo\r\n    Pop $foo.a",
				parser.parseStatement(readerFor("Function foo(a)")));
		assertEquals("    Push $foo.a\r\n    Return",
				parser.parseStatement(readerFor("    Return a")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

		assertEquals("Var /GLOBAL a\r\nPush \"1\"\r\nCall foo\r\nPop $a",
				parser.parseStatement(readerFor("a = foo(\"1\")")));

		assertEquals(
				"StrCpy $a \"$a world$R0\" \"\" \"\" ",
				parser.parseStatement(readerFor("a = StrCpy(\"$a world$R0\", \"\", \"\") ; <-- \"hello world!\"")));

		assertEquals("StrCpy $a \"foobar\" ",
				parser.parseStatement(readerFor("a = StrCpy(\"foobar\")")));

		assertEquals("Var /GLOBAL b\r\nIntOp $b $a \"+\" 1 ",
				parser.parseStatement(readerFor("b = IntOp(a, \"+\", 1)")));

		assertEquals(
				"Var /GLOBAL c\r\nGetTempFileName $c $%TEMP% \r\nVar /GLOBAL s01\r\nPush 1\r\nCall foo\r\nPop $s01\r\nStrCpy $c \"$c$b$s01$a\"",
				parser.parseStatement(readerFor("c = GetTempFileName($%TEMP%) + b + foo(1) + a")));
	}

	@Test
	public void testFunctionCallInsideFunction() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();

		assertEquals(
				"Var /GLOBAL join.a\r\nVar /GLOBAL join.b\r\nFunction join\r\n    Pop $join.a\r\n    Pop $join.b",
				parser.parseStatement(readerFor("Function join(a, b)")));
		assertEquals("  Push \"$join.a$join.b\"\r\n  Return",
				parser.parseStatement(readerFor("  Return a + b")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

		assertEquals(
				"Var /GLOBAL len.a\r\nVar /GLOBAL len.b\r\nFunction len\r\n    Pop $len.a\r\n    Pop $len.b",
				parser.parseStatement(readerFor("Function len(a, b)")));
		assertEquals(
				"  Var /GLOBAL len.s01\r\n  Var /GLOBAL len.s02\r\n  Push $len.b\r\n  Push $len.a\r\n  Call join\r\n  Pop $len.s02\r\n  StrLen $len.s01 $len.s02 \r\n  Push $len.s01\r\n  Return",
				parser.parseStatement(readerFor("  Return StrLen(join(a, b))")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));
	}

	@Test
	public void testInstructions() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		assertEquals(
				"ExecShell \"open\" \"http://nsis.sf.net/\" ",
				parser.parseStatement(readerFor("ExecShell(\"open\", \"http://nsis.sf.net/\")")));
		assertEquals(
				"ExecShell \"open\" \"http://nsis.sf.net/\" \"SW_SHOWNORMAL\" ",
				parser.parseStatement(readerFor("ExecShell(\"open\", \"http://nsis.sf.net/\", \"SW_SHOWNORMAL\")")));
		assertEquals(
				"ExecShell \"open\" \"http://nsis.sf.net/\" ",
				parser.parseStatement(readerFor("ExecShell(\"open\", \\\r\n    \"http://nsis.sf.net/\")")));
		assertEquals(
				"Var /GLOBAL ret\r\nExecWait '\"$INSTDIR\\someprogram.exe\"' $ret ",
				parser.parseStatement(readerFor("ret = ExecWait('\"$INSTDIR\\someprogram.exe\"')")));
		assertEquals("StrCpy $r0 \"hello world!\" 5 ",
				parser.parseStatement(readerFor("r0 = StrCpy(\"hello world!\", 5)")));
		assertEquals(
				"FileOpen $r1 \"C:\\temp\\makensis.log\" \"r\" ",
				parser.parseStatement(readerFor("R1 = FileOpen(\"C:\\temp\\makensis.log\", \"r\")")));
	}

	@Test
	public void testSringConcatenation() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();

		assertEquals("Var /GLOBAL a\r\nStrCpy $a \"hello\"",
				parser.parseStatement(readerFor("a = \"hello\"")));
		assertEquals("StrCpy $a \"$a world, oh my!\"",
				parser.parseStatement(readerFor("a = a + \" world\" + \", oh my!\"")));

		assertEquals("Var /GLOBAL foo.a\r\nFunction foo\r\n    Pop $foo.a",
				parser.parseStatement(readerFor("Function foo(a)")));
		assertEquals("    Push \"$foo.a times\"\r\n    Return",
				parser.parseStatement(readerFor("    Return a + \" times\"")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

		assertEquals(
				"Var /GLOBAL s01\r\nPush 1\r\nCall foo\r\nPop $s01\r\nStrCpy $a \"foo: $s01\"",
				parser.parseStatement(readerFor("a = \"foo: \" + foo(1)")));

		assertEquals("StrCpy $a \"hello${HELLO}\"",
				parser.parseStatement(readerFor("a = \"hello\" + ${HELLO}")));
	}

	@Test
	public void testVariableScope() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		Parser parser = createParser();
		assertEquals("Var /GLOBAL a", parser.parseStatement(readerFor("Var a")));
		assertEquals("Var /GLOBAL foo.a\r\nFunction foo\r\n    Pop $foo.a",
				parser.parseStatement(readerFor("Function foo(a)")));
		assertEquals("DetailPrint $foo.a ",
				parser.parseStatement(readerFor("DetailPrint(a)")));
		assertEquals("DetailPrint $a ",
				parser.parseStatement(readerFor("DetailPrint(global.a)")));
		assertEquals("DetailPrint $r0 ",
				parser.parseStatement(readerFor("DetailPrint(global.r0)")));
		assertEquals("FunctionEnd", parser.parseStatement(readerFor("FunctionEnd")));

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
		assertEquals(
				"Var /GLOBAL a\r\nStrCpy $a \"hello... world!\"",
				parser.parseStatement(readerFor("a = \"hello... \" /*world?*/ + \"world!\"")));
		assertEquals(
				"Var /GLOBAL foo.a\r\nVar /GLOBAL foo.b\r\nFunction foo\r\n    Pop $foo.a\r\n    Pop $foo.b",
				parser.parseStatement(readerFor("Function foo( /*a?*/ a, /*b?*/ b)")));
		assertEquals("DetailPrint $foo.a ",
				parser.parseStatement(readerFor("DetailPrint(/*a?*/a/*a?*/)")));
		assertEquals("Push $foo.a\r\nReturn",
				parser.parseStatement(readerFor("Return /*a?*/a/*a?*/")));
	}

	@Test
	public void testIf() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();

		parser.parseStatement(readerFor("world = \"world\""));
		parser.parseStatement(readerFor("function hello()"));
		parser.parseStatement(readerFor("  return \"hello \""));
		parser.parseStatement(readerFor("functionend"));

		assertEquals("${If} $world == \"hello world\"",
				parser.parseStatement(readerFor("if world == \"hello world\"")));

		assertEquals(
				"${IfNot} \"${FileExists}\" \"$instdir\\foo.txt\"",
				parser.parseStatement(readerFor("if not \"${FileExists}\" \"$instdir\\foo.txt\"")));

		assertEquals(
				"${If} \"hello $world\" == \"hello world\"",
				parser.parseStatement(readerFor("if \"hello \" + world == \"hello world\"")));

		assertEquals(
				"Var /GLOBAL s01\r\nCall hello\r\nPop $s01\r\n${If} $world == \"world\"\r\n${AndIf} $s01 != \"hello \"",
				parser.parseStatement(readerFor("if world == \"world\" \\\r\n    and hello() != \"hello \"")));

		assertEquals("${ElseIf} $world != \"hello world\"",
				parser.parseStatement(readerFor("elseif world != \"hello world\"")));

		assertEquals("${ElseIf} $world > 1\r\n${AndIf} \"${Errors}\"",
				parser.parseStatement(readerFor("elseif world > 1 and \"${Errors}\"")));

		assertEquals("${Else}", parser.parseStatement(readerFor("else")));

		assertEquals("${EndIf}", parser.parseStatement(readerFor("EndIf")));
	}

	@Test
	public void testDo() throws InvalidSyntaxException, ParserException,
			EnvironmentException {
		Parser parser = createParser();

		parser.parseStatement(readerFor("a = 1"));
		parser.parseStatement(readerFor("Function Inc(i)"));
		parser.parseStatement(readerFor("  Return IntOp(i, \"+\", 1)"));
		parser.parseStatement(readerFor("FunctionEnd"));

		assertEquals("${Do}", parser.parseStatement(readerFor("do")));

		assertEquals("${DoUntil} $a < 10",
				parser.parseStatement(readerFor("Do Until a < 10")));

		assertEquals(
				"${DoWhile} ${FileExists} \"$instdir\\foo.txt\"",
				parser.parseStatement(readerFor("Do While ${FileExists} \"$instdir\\foo.txt\"")));

		assertEquals("${Continue}", parser.parseStatement(readerFor("Continue")));
		assertEquals("${Break}", parser.parseStatement(readerFor("Break")));

		assertEquals("${Loop}", parser.parseStatement(readerFor("Loop")));

		assertEquals("${LoopWhile} ${Errors}",
				parser.parseStatement(readerFor("Loop While ${Errors}")));

		// Function value
		assertEquals(
				"Var /GLOBAL s02\r\nPush $a\r\nCall inc\r\nPop $s02\r\n${LoopUntil} $a > $s02",
				parser.parseStatement(readerFor("Loop Until a > Inc(a)")));

	}

}
