package bridlensis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import bridlensis.env.Environment;
import bridlensis.env.EnvironmentException;
import bridlensis.env.SimpleNameGenerator;
import bridlensis.env.SimpleTypeObject;
import bridlensis.env.TypeObject.Type;
import bridlensis.env.UserFunction;

public class StatementFactoryTest {

	private Environment env;

	public StatementFactoryTest() {
		env = new Environment(new SimpleNameGenerator());
		env.loadBuiltinVariables();
		env.loadBuiltinFunctions();
	}

	@Test
	public void testVariableDeclare() throws InvalidSyntaxException,
			EnvironmentException {
		assertEquals(
				"Var /GLOBAL a",
				StatementFactory.variableDeclare("",
						env.registerVariable("a", null)));
		assertEquals(
				"  Var /GLOBAL foo",
				StatementFactory.variableDeclare("  ",
						env.registerVariable("Foo", null)));
	}

	@Test
	public void testVariableAssign() throws InvalidSyntaxException,
			EnvironmentException {
		assertEquals("  StrCpy $a 1", StatementFactory.variableAssign("  ", env
				.registerVariable("a", null), new SimpleTypeObject(
				Type.INTEGER, "1")));
		assertEquals("StrCpy $foo $a", StatementFactory.variableAssign("", env
				.registerVariable("foo", null), new SimpleTypeObject(
				Type.SPECIAL, "$a")));
	}

	@Test
	public void testFunctionBegin() throws InvalidSyntaxException,
			EnvironmentException {
		UserFunction foo = env.registerUserFunction("Foo");
		assertEquals("    Function foo",
				StatementFactory.functionBegin("    ", foo));

		UserFunction bar = env.registerUserFunction("bar");
		bar.addArgument(env.registerVariable("a", bar));
		bar.addArgument(env.registerVariable("b", bar));
		bar.addArgument(env.registerVariable("c", bar));
		bar.setHasReturn(true);
		assertEquals(
				"  Function bar\r\n    Pop $bar.a\r\n    Pop $bar.b\r\n    Pop $bar.c",
				StatementFactory.functionBegin("  ", bar));
	}

	@Test
	public void testFunctionReturn() throws InvalidSyntaxException,
			EnvironmentException {
		UserFunction foo = env.registerUserFunction("Foo");
		assertEquals("    Return",
				StatementFactory.functionReturn("    ", foo, null));

		UserFunction bar = env.registerUserFunction("bar");
		bar.setHasReturn(true);
		assertEquals("Push \"hello world!\"\r\nReturn",
				StatementFactory.functionReturn("", bar, new SimpleTypeObject(
						Type.STRING, "hello world!")));
	}
}
