package bridlensis;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.SimpleNameGenerator;
import bridlensis.env.SimpleTypeObject;
import bridlensis.env.TypeObject;
import bridlensis.env.TypeObject.Type;
import bridlensis.env.UserFunction;

public class StatementFactoryTest {

	private Environment env;
	private StatementFactory sf;

	public StatementFactoryTest() {
		env = new Environment(new SimpleNameGenerator());
		env.loadBuiltinVariables();
		env.loadBuiltinFunctions();
		sf = new StatementFactory(env);
	}

	private List<TypeObject> asList(TypeObject... objects) {
		return Arrays.asList(objects);
	}

	@Test
	public void testVariableDeclare() throws InvalidSyntaxException,
			EnvironmentException {
		assertEquals("Var /GLOBAL a",
				sf.variableDeclare("", env.registerVariable("a", null)));
		assertEquals("  Var /GLOBAL foo",
				sf.variableDeclare("  ", env.registerVariable("Foo", null)));
	}

	@Test
	public void testVariableAssign() throws InvalidSyntaxException,
			EnvironmentException {
		assertEquals("  StrCpy $a 1", sf.variableAssign("  ", env
				.registerVariable("a", null), new SimpleTypeObject(
				Type.INTEGER, "1")));
		assertEquals("StrCpy $foo $a", sf.variableAssign("", env
				.registerVariable("foo", null), new SimpleTypeObject(
				Type.SPECIAL, "$a")));
	}

	@Test
	public void testFunctionBegin() throws InvalidSyntaxException,
			EnvironmentException {
		UserFunction foo = env.registerUserFunction("Foo");
		assertEquals("    Function foo", sf.functionBegin("    ", foo));

		UserFunction bar = env.registerUserFunction("bar");
		bar.addArgument(env.registerVariable("a", bar));
		bar.addArgument(env.registerVariable("b", bar));
		bar.addArgument(env.registerVariable("c", bar));
		bar.setHasReturn(true);
		assertEquals(
				"  Function bar\r\n    Pop $bar.a\r\n    Pop $bar.b\r\n    Pop $bar.c",
				sf.functionBegin("  ", bar));
	}

	@Test
	public void testFunctionReturn() throws InvalidSyntaxException,
			EnvironmentException {
		UserFunction foo = env.registerUserFunction("Foo");
		assertEquals("    Return", sf.functionReturn("    ", foo, null));

		UserFunction bar = env.registerUserFunction("bar");
		bar.setHasReturn(true);
		assertEquals("Push \"hello world!\"\r\nReturn", sf.functionReturn("",
				bar, new SimpleTypeObject(Type.STRING, "hello world!")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCall() throws InvalidSyntaxException, EnvironmentException {
		assertEquals("  DetailPrint \"hello\" ", sf.call("  ", env
				.getCallable("detailprint"), asList(new SimpleTypeObject(
				Type.STRING, "hello")), null));

		UserFunction foo = env.registerUserFunction("Foo");

		assertEquals("\tCall foo", sf.call("\t", env.getCallable("foo"),
				Collections.EMPTY_LIST, null));

		assertEquals(" Push $c\r\n Push $b\r\n Push $a\r\n Call foo", sf.call(
				" ",
				env.getCallable("Foo"),
				asList(new SimpleTypeObject(Type.SPECIAL, "$a"),
						new SimpleTypeObject(Type.SPECIAL, "$b"),
						new SimpleTypeObject(Type.SPECIAL, "$c")), env
						.registerVariable("ret", null)));

		// Function MsgBox
		assertEquals(
				"MessageBox MB_OKCANCEL|MB_ICONINFORMATION \"hello\" /SD IDOK IDOK msgbox_s02 IDCANCEL msgbox_s03\r\n    msgbox_s02:\r\n        StrCpy $foo.ret \"OK\"\r\n        GoTo msgbox_s01\r\n    msgbox_s03:\r\n        StrCpy $foo.ret \"CANCEL\"\r\n        GoTo msgbox_s01\r\n    msgbox_s01:",
				sf.call("",
						env.getCallable("MsgBox"),
						asList(new SimpleTypeObject(Type.STRING, "OKCANCEL"),
								new SimpleTypeObject(Type.STRING, "hello"),
								new SimpleTypeObject(Type.STRING,
										"ICONINFORMATION"),
								new SimpleTypeObject(Type.STRING, "OK")), env
								.registerVariable("ret", foo)));
		assertEquals("MessageBox MB_OKCANCEL \"hello\" /SD IDCANCEL", sf.call(
				"", env.getCallable("MsgBox"), Arrays.asList(
						new SimpleTypeObject(Type.STRING, "OKCANCEL"),
						new SimpleTypeObject(Type.STRING, "hello"),
						StatementFactory.NULL, new SimpleTypeObject(
								Type.STRING, "CANCEL")), null));

		// Function CopyFiles
		assertEquals(
				"StrCpy $s01 1\r\nClearErrors\r\nCopyFiles /SILENT \"C:\\autoexec.bat\" $%TEMP%\r\nIfErrors +2\r\n    StrCpy $s01 0",
				sf.call("",
						env.getCallable("FileCopy"),
						asList(new SimpleTypeObject(Type.STRING,
								"C:\\autoexec.bat"), new SimpleTypeObject(
								Type.SPECIAL, "$%TEMP%")), env
								.registerVariable("s01", null)));
		assertEquals("CopyFiles /SILENT \"C:\\autoexec.bat\" $%TEMP%", sf.call(
				"",
				env.getCallable("FileCopy"),
				asList(new SimpleTypeObject(Type.STRING, "C:\\autoexec.bat"),
						new SimpleTypeObject(Type.SPECIAL, "$%TEMP%")), null));

		// Function Delete
		assertEquals(
				"StrCpy $s02 1\r\nClearErrors\r\nDelete \"C:\\autoexec.bat\"\r\nIfErrors +2\r\n    StrCpy $s02 0",
				sf.call("",
						env.getCallable("Delete"),
						asList(new SimpleTypeObject(Type.STRING,
								"C:\\autoexec.bat"), StatementFactory.NULL),
						env.registerVariable("s02", null)));
		assertEquals(
				"StrCpy $s03 1\r\nClearErrors\r\nDelete /REBOOTOK \"C:\\autoexec.bat\"\r\nIfErrors +2\r\n    StrCpy $s03 0",
				sf.call("",
						env.getCallable("Delete"),
						asList(new SimpleTypeObject(Type.STRING,
								"C:\\autoexec.bat"), new SimpleTypeObject(
								Type.SPECIAL, "/REBOOTOK")), env
								.registerVariable("s03", null)));
	}
}
