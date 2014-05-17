package bridlensis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.SimpleNameGenerator;
import bridlensis.env.SimpleTypeObject;
import bridlensis.env.TypeObject;
import bridlensis.env.TypeObject.Type;
import bridlensis.env.UserFunction;

public class StatementParserTest {

	private InputReader readerFor(String string) throws InvalidSyntaxException {
		InputReader reader = new InputReader(new Scanner(string));
		reader.goToNextStatement();
		return reader;
	}

	private StatementParser createParser() {
		Environment environment = new Environment(new SimpleNameGenerator());
		environment.loadBuiltinVariables();
		environment.loadBuiltinFunctions();
		return new StatementParser(environment);
	}

	@Test
	public void testParseExpression() throws InvalidSyntaxException,
			ParserException, EnvironmentException {
		StatementParser parser = createParser();
		InputReader reader;
		StringBuilder buffer;
		StringBuilder expected;

		parser.parseVarDeclare(readerFor("a"));
		parser.parseVarDeclare(readerFor("b"));
		parser.parseVarDeclare(readerFor("c"));

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

		parser.parseFunctionBegin(readerFor("foo(a)"));
		try {
			buffer = new StringBuilder();
			reader = readerFor("a + foo(b)");
			parser.parseExpression(reader.nextWord(), buffer, reader);
			fail();
		} catch (InvalidSyntaxException e) {
			// All good
			System.err.println(e.getMessage());
		}
		parser.parseVarDeclare(readerFor("b"));
		parser.parseFunctionReturn(readerFor("a"));

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

		parser.parseFunctionEnd(readerFor(""));

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
	public void testCall() throws InvalidSyntaxException, EnvironmentException {
		StatementParser parser = createParser();
		Environment env = parser.getEnvironment();
		StringBuilder expected;

		expected = new StringBuilder();
		expected.append("  DetailPrint \"hello\" ");
		assertEquals(expected.toString(), parser.call("  ", env
				.getCallable("detailprint"),
				asFunctionArgumentList(new SimpleTypeObject(Type.STRING,
						"hello")), null));

		UserFunction foo = env.registerUserFunction("Foo");

		expected = new StringBuilder();
		expected.append("\tCall foo");
		assertEquals(expected.toString(), parser.call("\t",
				env.getCallable("foo"), asFunctionArgumentList(), null));

		expected = new StringBuilder();
		expected.append("");
		assertEquals(" Push $c\r\n Push $b\r\n Push $a\r\n Call foo",
				parser.call(
						" ",
						env.getCallable("Foo"),
						asFunctionArgumentList(new SimpleTypeObject(
								Type.SPECIAL, "$a"), new SimpleTypeObject(
								Type.SPECIAL, "$b"), new SimpleTypeObject(
								Type.SPECIAL, "$c")), env.registerVariable(
								"ret", null)));

		// Function MsgBox
		expected = new StringBuilder();
		expected.append("MessageBox MB_OKCANCEL|MB_ICONINFORMATION \"hello\" /SD IDOK IDOK msgbox_s02 IDCANCEL msgbox_s03\r\n");
		expected.append("    msgbox_s02:\r\n");
		expected.append("        StrCpy $foo.ret \"OK\"\r\n");
		expected.append("        GoTo msgbox_s01\r\n");
		expected.append("    msgbox_s03:\r\n");
		expected.append("        StrCpy $foo.ret \"CANCEL\"\r\n");
		expected.append("        GoTo msgbox_s01\r\n");
		expected.append("    msgbox_s01:");
		assertEquals(expected.toString(), parser.call(
				"",
				env.getCallable("MsgBox"),
				asFunctionArgumentList(new SimpleTypeObject(Type.STRING,
						"OKCANCEL"),
						new SimpleTypeObject(Type.STRING, "hello"),
						new SimpleTypeObject(Type.STRING, "ICONINFORMATION"),
						new SimpleTypeObject(Type.STRING, "OK")), env
						.registerVariable("ret", foo)));

		expected = new StringBuilder();
		expected.append("MessageBox MB_OKCANCEL \"hello\" /SD IDCANCEL");
		assertEquals(expected.toString(), parser.call("", env
				.getCallable("MsgBox"), Arrays.asList(new SimpleTypeObject(
				Type.STRING, "OKCANCEL"), new SimpleTypeObject(Type.STRING,
				"hello"), NSISStatements.NULL, new SimpleTypeObject(
				Type.STRING, "CANCEL")), null));

		// Function CopyFiles
		expected = new StringBuilder();
		expected.append("StrCpy $s01 1\r\n");
		expected.append("ClearErrors\r\n");
		expected.append("CopyFiles /SILENT \"C:\\autoexec.bat\" $%TEMP%\r\n");
		expected.append("IfErrors +2\r\n");
		expected.append("    StrCpy $s01 0");
		assertEquals(expected.toString(), parser.call(
				"",
				env.getCallable("FileCopy"),
				asFunctionArgumentList(new SimpleTypeObject(Type.STRING,
						"C:\\autoexec.bat"), new SimpleTypeObject(Type.SPECIAL,
						"$%TEMP%")), env.registerVariable("s01", null)));

		expected = new StringBuilder();
		expected.append("CopyFiles /SILENT \"C:\\autoexec.bat\" $%TEMP%");
		assertEquals(expected.toString(), parser.call(
				"",
				env.getCallable("FileCopy"),
				asFunctionArgumentList(new SimpleTypeObject(Type.STRING,
						"C:\\autoexec.bat"), new SimpleTypeObject(Type.SPECIAL,
						"$%TEMP%")), null));

		// Function Delete
		expected = new StringBuilder();
		expected.append("StrCpy $s02 1\r\n");
		expected.append("ClearErrors\r\n");
		expected.append("Delete \"C:\\autoexec.bat\"\r\n");
		expected.append("IfErrors +2\r\n");
		expected.append("    StrCpy $s02 0");
		assertEquals(expected.toString(), parser.call(
				"",
				env.getCallable("Delete"),
				asFunctionArgumentList(new SimpleTypeObject(Type.STRING,
						"C:\\autoexec.bat"), NSISStatements.NULL), env
						.registerVariable("s02", null)));

		expected = new StringBuilder();
		expected.append("StrCpy $s03 1\r\n");
		expected.append("ClearErrors\r\n");
		expected.append("Delete /REBOOTOK \"C:\\autoexec.bat\"\r\n");
		expected.append("IfErrors +2\r\n");
		expected.append("    StrCpy $s03 0");
		assertEquals(expected.toString(), parser.call(
				"",
				env.getCallable("Delete"),
				asFunctionArgumentList(new SimpleTypeObject(Type.STRING,
						"C:\\autoexec.bat"), new SimpleTypeObject(Type.SPECIAL,
						"/REBOOTOK")), env.registerVariable("s03", null)));
	}

	private List<TypeObject> asFunctionArgumentList(TypeObject... objects) {
		return Arrays.asList(objects);
	}

}
